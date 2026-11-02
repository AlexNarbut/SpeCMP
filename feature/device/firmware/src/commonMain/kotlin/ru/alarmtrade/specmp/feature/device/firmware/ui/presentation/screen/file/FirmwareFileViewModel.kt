package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

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
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.BootEntryResult
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootEntry
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel

/**
 * «Обновить из файла» (приложение прошивки, BLE/USB): блок в рабочем режиме, одна кнопка — переход основной программы
 * в загрузчик. Дальше хост устройства показывает экран загрузчика (`FirmwareBootGraph` с `FirmwareSources.FileOnly`),
 * где выбирается файл.
 *
 * Модуль — основная программа блока (подсистема с целью [BootModeTarget.MainProgram]); список подсистем берётся из базы
 * устройств SDK (`deviceInformationInteractor`), сервер прошивок не нужен. Вход в загрузчик и PIN — как на экране
 * проверки ([FirmwareBootEntry]).
 */
class FirmwareFileViewModel(
    deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    bootRequester: FirmwareBootRequester,
    pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val bootEntry = FirmwareBootEntry(deviceSession, bootRequester, pinCooldown, dispatchers)

    private val _state = MutableStateFlow(FirmwareFileState())

    // Цель входа в загрузчик: в state — только id модуля.
    private val module = MutableStateFlow<FirmwareModule?>(null)

    // Остаток блокировки PIN для открытого диалога PIN (тикает, пока диалог на экране).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val pinLock: Flow<Duration?> = _state
        .map { state -> state.deviceId.takeIf { state.dialog is CheckDialog.Pin } }
        .distinctUntilChanged()
        .flatMapLatest { systemId ->
            if (systemId == null) {
                flowOf(null)
            } else {
                pinCooldown.remaining(systemId)
                    .map { left -> left.takeIf { it > Duration.ZERO }?.ceilToSeconds() }
                    .distinctUntilChanged()
            }
        }

    val state: StateFlow<FirmwareFileState> = combine(_state, pinLock) { state, lock ->
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
        _state.update {
            it.copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                firmwareVersion = device.displayFirmwareVersion
            )
        }
        if (previous.deviceId != device.systemId || previous.target !is LCEState.Content) load(device)
    }

    private suspend fun load(device: AlarmDevice) = executeWithProceedLock {
        val transport = deviceSession.transport.value ?: return@executeWithProceedLock
        _state.update { it.copy(target = LCEState.Loading, dialog = null, bootFailed = false) }
        val response = withContext(dispatchers.io) { repository.modules(device.systemName, transport) }
        when (response) {
            is Response.Error -> _state.update {
                it.copy(target = LCEState.Error(FirmwareError.from(response.exception)))
            }

            is Response.Success -> {
                val main = response.value.firstOrNull { it.bootTarget == BootModeTarget.MainProgram }
                module.update { main }
                val target = main?.let { FileTarget.Supported(it.id) } ?: FileTarget.NotSupported
                _state.update { it.copy(target = LCEState.Content(target)) }
            }
        }
    }

    fun onRetry() {
        val device = currentDevice ?: return
        launchOnDevice { load(device) }
    }

    fun onUpdateClick() {
        val moduleId = module.value?.id ?: return
        _state.update { it.copy(dialog = CheckDialog.ConfirmBoot(moduleId), bootFailed = false) }
    }

    fun onDialogDismiss() {
        _state.update { state -> if (state.dialog.isSending()) state else state.copy(dialog = null) }
    }

    /** «Перейти» в диалоге подтверждения: без PIN — SDK сам пробует сохранённый или `1111`. */
    fun onConfirmBoot() {
        if (_state.value.dialog !is CheckDialog.ConfirmBoot) return
        startBoot(pin = null)
    }

    /** «Продолжить» в диалоге PIN; значение живёт только в вызове — в state и логи оно не попадает. */
    fun onPinSubmit(pin: String) {
        if (_state.value.dialog !is CheckDialog.Pin) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        startBoot(servicePin)
    }

    private fun startBoot(pin: ServicePin?) {
        safeLaunch { executeWithProceedLock { enterBootMode(pin) } }
    }

    private suspend fun enterBootMode(pin: ServicePin?) {
        val target = module.value ?: return
        val bootTarget = target.bootTarget ?: return
        val systemId = _state.value.deviceId ?: return
        if (bootEntry.isPinLocked(systemId)) {
            showPin(target.id)
            return
        }
        _state.update { it.copy(dialog = it.dialog.sending(target.id, pin != null), bootFailed = false) }
        // Канал на экране загрузчика с файлом не используется — запрос несёт только модуль.
        val request = FirmwareBootRequest(target.id, FirmwareChannel.RELEASE)
        when (bootEntry.enter(request, bootTarget, systemId, pin)) {
            BootEntryResult.Entered -> _state.update { it.copy(dialog = null) }
            BootEntryResult.Failed -> _state.update { it.copy(dialog = null, bootFailed = true) }
            BootEntryResult.PinLocked -> showPin(target.id)
            BootEntryResult.WrongPin -> showPin(target.id, wrongPin = pin != null)
        }
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

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun Duration.ceilToSeconds(): Duration =
    ((inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND).seconds

private const val MILLIS_IN_SECOND = 1_000L
