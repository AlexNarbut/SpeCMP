package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.InstalledVersionSource
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.canFilter
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.displayTitle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.installedVersionSource
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.isUpdateFor
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.BootEntryResult
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootEntry
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/** Серверная версия запрашивается только после текущей: результат сравнения считается в момент ответа. */
class FirmwareCheckViewModel(
    deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    bootRequester: FirmwareBootRequester,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    private val autoBoot: FirmwareAutoBoot? = null,
) : BaseDeviceViewModel<FirmwareCheckState, Nothing>(deviceSession, FirmwareCheckState()) {

    private val bootEntry = FirmwareBootEntry(deviceSession, bootRequester, pinCooldown, dispatchers)

    override suspend fun onStart() {
        onEachDeviceReady(::onDeviceReady)
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CheckDialog.Pin } })
            .collectIntoState { lock -> copy(dialog = dialog.withPinLock(lock)) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = currentState
        reduce {
            copy(deviceName = device.systemName, deviceId = device.systemId, deviceCan = device.can.toFirmwareCan())
        }
        val sameDevice = previous.deviceId == device.systemId
        if (!sameDevice || previous.content !is LCEState.Content) load(device)
    }

    private suspend fun load(device: AlarmDevice) = executeWithProceedLock {
        val transport = deviceSession.transport.value ?: return@executeWithProceedLock
        reduce { copy(content = LCEState.Loading, dialog = null, bootFailed = false) }
        when (val response = withContext(dispatchers.io) { repository.modules(device.systemName, transport) }) {
            is Response.Error -> reduce { copy(content = LCEState.Error(FirmwareError.from(response.exception))) }

            is Response.Success -> {
                val list = response.value
                reduce {
                    copy(
                        modules = list.associateBy { module -> module.id },
                        content = LCEState.Content(list.map(::initialCard)),
                    )
                }
                // Через launchOnDevice: чтение модулей и запросы версий отменяются при потере связи
                // и не пишут в карточки нового блока.
                list.forEach { module -> launchOnDevice { resolveModule(device, module) } }
                offerAutoBoot()
            }
        }
    }

    // Экран открыт ради прошивки конкретного модуля: подтверждение входа в загрузчик — сразу, один раз.
    private fun offerAutoBoot() {
        val boot = autoBoot ?: return
        if (currentState.autoBootOffered) return
        reduce { copy(autoBootOffered = true) }
        onUpdateClick(boot.moduleId)
    }

    private fun initialCard(module: FirmwareModule): ModuleCardUi = ModuleCardUi(
        id = module.id,
        kind = module.kind,
        title = module.displayTitle,
        currentVersion = CurrentVersionUi.Reading,
        status = if (module.isBootSupported) CardStatusUi.Searching else CardStatusUi.NotSupported,
    )

    private suspend fun resolveModule(device: AlarmDevice, module: FirmwareModule) {
        val current = readCurrentVersion(device, module)
        updateCard(module.id) { it.copy(currentVersion = current) }
        if (module.isBootSupported) lookup(module.id)
    }

    private suspend fun readCurrentVersion(device: AlarmDevice, module: FirmwareModule): CurrentVersionUi =
        when (module.kind.installedVersionSource) {
            InstalledVersionSource.DEVICE -> CurrentVersionUi.Known(device.displayFirmwareVersion)

            InstalledVersionSource.BLUETOOTH_MODULE -> {
                when (val response = withContext(dispatchers.io) { deviceSession.readBluetoothModule() }) {
                    is Response.Success -> CurrentVersionUi.Known(response.value.manufacturer.mainFirmwareVersion)
                    is Response.Error -> CurrentVersionUi.Absent
                }
            }

            InstalledVersionSource.NONE -> CurrentVersionUi.Absent
        }

    /** Серверная версия модуля в текущем канале; ответ прежнего канала отбрасывается. */
    private suspend fun lookup(moduleId: Long) {
        val start = currentState
        val channel = start.channel
        updateCard(moduleId) { it.copy(status = CardStatusUi.Searching) }
        val can = start.modules[moduleId]?.kind?.canFilter(start.deviceCan)
        val response = withContext(dispatchers.io) { repository.latest(moduleId, channel, can) }
        reduce {
            if (this.channel != channel) {
                this
            } else {
                withCard(moduleId) { card -> card.copy(status = statusOf(response, card.currentVersion)) }
            }
        }
    }

    private fun statusOf(response: Response<FirmwareVersion>, current: CurrentVersionUi): CardStatusUi =
        when (response) {
            is Response.Error -> CardStatusUi.Failed(FirmwareError.from(response.exception))

            is Response.Success -> {
                val server = response.value
                val installed = (current as? CurrentVersionUi.Known)?.version
                if (installed != null && !server.isUpdateFor(installed)) {
                    CardStatusUi.Actual(installed)
                } else {
                    CardStatusUi.UpdateAvailable(server.version, server.description)
                }
            }
        }

    fun onRetry() {
        val device = currentDevice ?: return
        launchOnDevice { load(device) }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        if (currentState.channel == channel) return
        reduce { copy(channel = channel, bootFailed = false) }
        // Модули, у которых версия ещё читается, сами запросят сервер уже по новому каналу.
        cards().filter { it.status !is CardStatusUi.NotSupported && it.currentVersion !is CurrentVersionUi.Reading }
            .forEach { card -> launchOnDevice { lookup(card.id) } }
    }

    fun onRefreshModule(moduleId: Long) {
        val card = cards().firstOrNull { it.id == moduleId } ?: return
        if (card.status is CardStatusUi.NotSupported || card.status is CardStatusUi.Searching) return
        if (card.currentVersion is CurrentVersionUi.Reading) return
        launchOnDevice { lookup(moduleId) }
    }

    fun onDescriptionClick(moduleId: Long) {
        val status = cards().firstOrNull { it.id == moduleId }?.status as? CardStatusUi.UpdateAvailable ?: return
        reduce { copy(dialog = CheckDialog.Description(moduleId, status.version, status.description)) }
    }

    fun onUpdateClick(moduleId: Long) {
        if (currentState.modules[moduleId]?.isBootSupported != true) return
        reduce { copy(dialog = dialog.replacedBy(CheckDialog.ConfirmBoot(moduleId)), bootFailed = false) }
    }

    fun onDialogDismiss() {
        reduce { if (dialog.isSending()) this else copy(dialog = null) }
    }

    /** Без PIN — SDK сам пробует сохранённый или `1111`. */
    fun onConfirmBoot() {
        val dialog = currentState.dialog as? CheckDialog.ConfirmBoot ?: return
        startBoot(dialog.moduleId, pin = null)
    }

    /** PIN живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        val dialog = currentState.dialog as? CheckDialog.Pin ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        startBoot(dialog.moduleId, servicePin)
    }

    private fun startBoot(moduleId: Long, pin: ServicePin?) {
        intent { executeWithProceedLock { enterBootMode(moduleId, pin) } }
    }

    private suspend fun enterBootMode(moduleId: Long, pin: ServicePin?) {
        val target = currentState.modules[moduleId]?.bootTarget ?: return
        val systemId = currentState.deviceId ?: return
        // Пока блок не принимает ввод, ничего не шлём: иначе он продлит блокировку.
        if (bootEntry.isPinLocked(systemId)) {
            showPin(moduleId)
            return
        }
        reduce { copy(dialog = dialog.replacedBy(dialog.sending(moduleId, pin != null)), bootFailed = false) }
        val preselect = autoBoot?.takeIf { it.moduleId == moduleId }?.preselect
        val request = FirmwareBootRequest(moduleId, currentState.channel, preselect)
        when (bootEntry.enter(request, target, systemId, pin)) {
            BootEntryResult.Entered -> reduce { copy(dialog = null) }
            BootEntryResult.Failed -> reduce { copy(dialog = null, bootFailed = true) }
            BootEntryResult.PinLocked -> showPin(moduleId)
            BootEntryResult.WrongPin -> showPin(moduleId, wrongPin = pin != null)
        }
    }

    private fun showPin(moduleId: Long, wrongPin: Boolean = false) {
        reduce { copy(dialog = dialog.replacedBy(dialog.nextPin(moduleId, wrongPin))) }
    }

    private fun cards(): List<ModuleCardUi> = currentState.content.content.orEmpty()

    private fun updateCard(moduleId: Long, transform: (ModuleCardUi) -> ModuleCardUi) {
        reduce { withCard(moduleId, transform) }
    }

    private fun FirmwareCheckState.withCard(
        moduleId: Long,
        transform: (ModuleCardUi) -> ModuleCardUi,
    ): FirmwareCheckState {
        val ready = content as? LCEState.Content ?: return this
        return copy(content = ready.copy(content = ready.content.map { if (it.id == moduleId) transform(it) else it }))
    }
}
