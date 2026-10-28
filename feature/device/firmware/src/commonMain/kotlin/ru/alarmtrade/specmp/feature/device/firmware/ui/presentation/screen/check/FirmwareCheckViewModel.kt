package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel

/**
 * «Проверка прошивки»: модули блока, их версии и версии на сервере, переход в режим обновления.
 *
 * На каждом подключении блока: для нового блока (или после ошибки) — список модулей; текущие версии читаются у
 * каждого модуля, затем (по готовности) запрашивается серверная версия выбранного канала. Результат сравнения
 * считается в момент ответа — поэтому серверный запрос идёт только после того, как известна текущая версия модуля.
 * Смена канала и «обновить» на карточке — повторный запрос; устаревший ответ (канал успел смениться) отбрасывается.
 *
 * Переход в загрузчик: на BLE команда сама пробует сохранённый PIN или `1111`; неверный PIN → диалог PIN
 * с блокировкой ввода ([ServicePinCooldown]). Ожидаемая перезагрузка ставится до команды (блок может перезагрузиться
 * раньше ответа) и снимается при любом исходе, кроме принятой команды. После принятой команды хост показывает
 * экран загрузчика.
 */
class FirmwareCheckViewModel(
    deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    private val bootRequester: FirmwareBootRequester,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(FirmwareCheckState())

    // Модули блока по id: из них берётся цель входа в загрузчик. Карточки в state — только примитивы.
    private val modules = MutableStateFlow<Map<Long, FirmwareModule>>(emptyMap())

    // Остаток блокировки PIN для открытого диалога PIN (тикает, пока диалог на экране).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val pinLock: Flow<Duration?> = _state
        .map { state -> state.deviceId.takeIf { state.dialog is CheckDialog.Pin } }
        .distinctUntilChanged()
        .flatMapLatest { systemId ->
            if (systemId == null) {
                flowOf(null)
            } else {
                // Тик кулдауна — 250 мс, а диалог показывает целые секунды: округляем вверх и отсекаем повторы,
                // чтобы state экрана менялся раз в секунду, а не 4 раза.
                pinCooldown.remaining(systemId)
                    .map { left -> left.takeIf { it > Duration.ZERO }?.ceilToSeconds() }
                    .distinctUntilChanged()
            }
        }

    val state: StateFlow<FirmwareCheckState> = combine(_state, pinLock) { state, lock ->
        val dialog = state.dialog
        if (dialog is CheckDialog.Pin) state.copy(dialog = dialog.copy(lockedFor = lock)) else state
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    init {
        onEachDeviceReady(::onDeviceReady)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = _state.value
        _state.update { it.copy(deviceName = device.systemName, deviceId = device.systemId) }
        val sameDevice = previous.deviceId == device.systemId
        if (!sameDevice || previous.content !is LCEState.Content) load(device)
    }

    private suspend fun load(device: AlarmDevice) = executeWithProceedLock {
        val transport = deviceSession.transport.value ?: return@executeWithProceedLock
        _state.update { it.copy(content = LCEState.Loading, dialog = null, bootFailed = false) }
        val response = withContext(dispatchers.io) { repository.modules(device.systemName, transport) }
        when (response) {
            is Response.Error -> _state.update {
                it.copy(content = LCEState.Error(FirmwareError.from(response.exception)))
            }

            is Response.Success -> {
                val list = response.value
                modules.update { list.associateBy { module -> module.id } }
                _state.update { it.copy(content = LCEState.Content(list.map(::initialCard))) }
                // Через launchOnDevice: чтение модулей и запросы версий отменяются при потере связи
                // и не пишут в карточки нового блока.
                list.forEach { module -> launchOnDevice { resolveModule(device, module) } }
            }
        }
    }

    private fun initialCard(module: FirmwareModule): ModuleCardUi = ModuleCardUi(
        id = module.id,
        kind = module.kind,
        title = "${module.title} (${module.systemName})",
        currentVersion = CurrentVersionUi.Reading,
        status = if (module.isBootSupported) CardStatusUi.Searching else CardStatusUi.NotSupported
    )

    /** Текущая версия модуля, затем — серверная (если модуль можно обновить). */
    private suspend fun resolveModule(device: AlarmDevice, module: FirmwareModule) {
        val current = readCurrentVersion(device, module)
        updateCard(module.id) { it.copy(currentVersion = current) }
        if (module.isBootSupported) lookup(module.id)
    }

    private suspend fun readCurrentVersion(device: AlarmDevice, module: FirmwareModule): CurrentVersionUi =
        when (module.kind) {
            FirmwareModuleKind.AlarmMain,
            FirmwareModuleKind.WatchMain,
            FirmwareModuleKind.GpsTracker,
            FirmwareModuleKind.BmwBypass,
            FirmwareModuleKind.RemoteControlMain -> CurrentVersionUi.Known(device.displayFirmwareVersion)

            FirmwareModuleKind.BluetoothModule -> {
                val response = withContext(dispatchers.io) { deviceSession.readBluetoothModule() }
                when (response) {
                    is Response.Success -> CurrentVersionUi.Known(response.value.manufacturer.mainFirmwareVersion)
                    is Response.Error -> CurrentVersionUi.Absent
                }
            }

            FirmwareModuleKind.BatteryCharger,
            FirmwareModuleKind.WatchInterface,
            FirmwareModuleKind.WatchBluetooth,
            FirmwareModuleKind.FuelSensor,
            FirmwareModuleKind.ChargingStationTester,
            FirmwareModuleKind.CanModule,
            FirmwareModuleKind.Microbot,
            FirmwareModuleKind.Unknown -> CurrentVersionUi.Absent
        }

    /** Серверная версия модуля в текущем канале; ответ прежнего канала отбрасывается. */
    private suspend fun lookup(moduleId: Long) {
        val channel = _state.value.channel
        updateCard(moduleId) { it.copy(status = CardStatusUi.Searching) }
        val response = withContext(dispatchers.io) { repository.latest(moduleId, channel) }
        _state.update { state ->
            if (state.channel != channel) {
                state
            } else {
                state.withCard(moduleId) { card -> card.copy(status = statusOf(response, card.currentVersion)) }
            }
        }
    }

    private fun statusOf(response: Response<FirmwareVersion>, current: CurrentVersionUi): CardStatusUi =
        when (response) {
            is Response.Error -> CardStatusUi.Failed(FirmwareError.from(response.exception))

            is Response.Success -> {
                val server = response.value
                val installed = (current as? CurrentVersionUi.Known)?.version
                if (installed == null || server.version.relationTo(installed) == VersionRelation.Newer) {
                    CardStatusUi.UpdateAvailable(server.version, server.description)
                } else {
                    CardStatusUi.Actual(installed)
                }
            }
        }

    fun onRetry() {
        val device = currentDevice ?: return
        launchOnDevice { load(device) }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        if (_state.value.channel == channel) return
        _state.update { it.copy(channel = channel, bootFailed = false) }
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
        _state.update { it.copy(dialog = CheckDialog.Description(moduleId, status.version, status.description)) }
    }

    fun onUpdateClick(moduleId: Long) {
        if (modules.value[moduleId]?.isBootSupported != true) return
        _state.update { it.copy(dialog = CheckDialog.ConfirmBoot(moduleId), bootFailed = false) }
    }

    fun onDialogDismiss() {
        _state.update { state -> if (state.dialog.isSending()) state else state.copy(dialog = null) }
    }

    /** «Перейти» в диалоге подтверждения: без PIN — SDK сам пробует сохранённый или `1111`. */
    fun onConfirmBoot() {
        val dialog = _state.value.dialog as? CheckDialog.ConfirmBoot ?: return
        startBoot(dialog.moduleId, pin = null)
    }

    /** «Продолжить» в диалоге PIN; значение живёт только в вызове — в state и логи оно не попадает. */
    fun onPinSubmit(pin: String) {
        val dialog = _state.value.dialog as? CheckDialog.Pin ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        startBoot(dialog.moduleId, servicePin)
    }

    private fun startBoot(moduleId: Long, pin: ServicePin?) {
        safeLaunch { executeWithProceedLock { enterBootMode(moduleId, pin) } }
    }

    private suspend fun enterBootMode(moduleId: Long, pin: ServicePin?) {
        val target = modules.value[moduleId]?.bootTarget ?: return
        val systemId = _state.value.deviceId ?: return
        val needsPin = deviceSession.transport.value == ConnectionTransport.BLE
        // Пока блок не принимает ввод, ничего не шлём: иначе он продлит блокировку.
        if (needsPin && pinCooldown.remainingNow(systemId) > Duration.ZERO) {
            showPin(moduleId)
            return
        }
        _state.update { it.copy(dialog = it.dialog.sending(moduleId, pin != null), bootFailed = false) }
        bootRequester.request(FirmwareBootRequest(moduleId, _state.value.channel))
        deviceSession.expectReboot(DeviceReboot.ToBoot)

        val saved = if (pin != null) withContext(dispatchers.io) { deviceSession.setServicePin(pin) } else null
        val result = when (saved) {
            is Response.Error -> saved
            else -> withContext(dispatchers.io) { deviceSession.setBootMode(target) }
        }
        when (result) {
            is Response.Error -> {
                logger.d { "Enter boot mode failed: ${result.exception::class.simpleName}" }
                abandonBootAttempt()
                _state.update { it.copy(dialog = null, bootFailed = true) }
            }

            is Response.Success -> when (result.value) {
                BootModeResult.Entered -> _state.update { it.copy(dialog = null) }

                BootModeResult.WrongPin -> {
                    abandonBootAttempt()
                    pinCooldown.start(systemId)
                    showPin(moduleId, wrongPin = pin != null)
                }
            }
        }
    }

    // Запрос и ожидание перезагрузки больше не нужны; ожидание снимаем, только если блок остался на связи.
    private fun abandonBootAttempt() {
        bootRequester.take()
        if (currentDevice != null) deviceSession.cancelReboot()
    }

    private fun showPin(moduleId: Long, wrongPin: Boolean = false) {
        _state.update { state ->
            val previous = state.dialog as? CheckDialog.Pin
            val wrongCount = (previous?.wrongCount ?: 0) + if (wrongPin) 1 else 0
            state.copy(dialog = CheckDialog.Pin(moduleId, wrong = wrongPin, wrongCount = wrongCount))
        }
    }

    private fun CheckDialog?.sending(moduleId: Long, withPin: Boolean): CheckDialog = if (withPin) {
        val previous = this as? CheckDialog.Pin
        CheckDialog.Pin(moduleId, sending = true, wrongCount = previous?.wrongCount ?: 0)
    } else {
        CheckDialog.ConfirmBoot(moduleId, sending = true)
    }

    private fun CheckDialog?.isSending(): Boolean = (this as? CheckDialog.ConfirmBoot)?.sending == true ||
        (this as? CheckDialog.Pin)?.sending == true

    private fun cards(): List<ModuleCardUi> = _state.value.content.content.orEmpty()

    private fun updateCard(moduleId: Long, transform: (ModuleCardUi) -> ModuleCardUi) {
        _state.update { it.withCard(moduleId, transform) }
    }

    private fun FirmwareCheckState.withCard(
        moduleId: Long,
        transform: (ModuleCardUi) -> ModuleCardUi
    ): FirmwareCheckState {
        val ready = content as? LCEState.Content ?: return this
        return copy(
            content = ready.copy(
                content = ready.content.map { card ->
                    if (card.id ==
                        moduleId
                    ) {
                        transform(card)
                    } else {
                        card
                    }
                }
            )
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun Duration.ceilToSeconds(): Duration =
    ((inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND).seconds

private const val MILLIS_IN_SECOND = 1_000L
