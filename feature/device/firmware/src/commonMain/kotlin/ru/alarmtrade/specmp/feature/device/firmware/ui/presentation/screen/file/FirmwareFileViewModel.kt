package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.BootEntryResult
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootEntry
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.isSending
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.nextPin
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.replacedBy
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.sending
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.withPinLock
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/** Список подсистем — из базы устройств SDK (`deviceInformationInteractor`), сервер прошивок не нужен. */
class FirmwareFileViewModel(
    deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    bootRequester: FirmwareBootRequester,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<FirmwareFileState, Nothing>(deviceSession, FirmwareFileState()) {

    private val bootEntry = FirmwareBootEntry(deviceSession, bootRequester, pinCooldown, dispatchers)

    override suspend fun onStart() {
        onEachDeviceReady(::onDeviceReady)
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CheckDialog.Pin } })
            .collectIntoState { lock -> copy(dialog = dialog.withPinLock(lock)) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val previous = currentState
        reduce {
            copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                firmwareVersion = device.displayFirmwareVersion,
            )
        }
        if (previous.deviceId != device.systemId || previous.target !is LCEState.Content) load(device)
    }

    private suspend fun load(device: AlarmDevice) = executeWithProceedLock {
        val transport = deviceSession.transport.value ?: return@executeWithProceedLock
        reduce { copy(target = LCEState.Loading, dialog = null, bootFailed = false) }
        when (val response = withContext(dispatchers.io) { repository.modules(device.systemName, transport) }) {
            is Response.Error -> reduce { copy(target = LCEState.Error(FirmwareError.from(response.exception))) }

            is Response.Success -> {
                val main = response.value.firstOrNull { it.bootTarget == BootModeTarget.MainProgram }
                val target = main?.let { FileTarget.Supported(it.id) } ?: FileTarget.NotSupported
                reduce { copy(module = main, target = LCEState.Content(target)) }
            }
        }
    }

    fun onRetry() {
        val device = currentDevice ?: return
        launchOnDevice { load(device) }
    }

    fun onUpdateClick() {
        val moduleId = currentState.module?.id ?: return
        reduce { copy(dialog = dialog.replacedBy(CheckDialog.ConfirmBoot(moduleId)), bootFailed = false) }
    }

    fun onDialogDismiss() {
        reduce { if (dialog.isSending()) this else copy(dialog = null) }
    }

    /** Без PIN — SDK сам пробует сохранённый или `1111`. */
    fun onConfirmBoot() {
        if (currentState.dialog !is CheckDialog.ConfirmBoot) return
        startBoot(pin = null)
    }

    /** PIN живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        if (currentState.dialog !is CheckDialog.Pin) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        startBoot(servicePin)
    }

    private fun startBoot(pin: ServicePin?) {
        intent { executeWithProceedLock { enterBootMode(pin) } }
    }

    private suspend fun enterBootMode(pin: ServicePin?) {
        val target = currentState.module ?: return
        val bootTarget = target.bootTarget ?: return
        val systemId = currentState.deviceId ?: return
        if (bootEntry.isPinLocked(systemId)) {
            showPin(target.id)
            return
        }
        reduce { copy(dialog = dialog.replacedBy(dialog.sending(target.id, pin != null)), bootFailed = false) }
        // Канал на экране загрузчика с файлом не используется — запрос несёт только модуль.
        val request = FirmwareBootRequest(target.id, FirmwareChannel.RELEASE)
        when (bootEntry.enter(request, bootTarget, systemId, pin)) {
            BootEntryResult.Entered -> reduce { copy(dialog = null) }
            BootEntryResult.Failed -> reduce { copy(dialog = null, bootFailed = true) }
            BootEntryResult.PinLocked -> showPin(target.id)
            BootEntryResult.WrongPin -> showPin(target.id, wrongPin = pin != null)
        }
    }

    private fun showPin(moduleId: Long, wrongPin: Boolean = false) {
        reduce { copy(dialog = dialog.replacedBy(dialog.nextPin(moduleId, wrongPin))) }
    }
}
