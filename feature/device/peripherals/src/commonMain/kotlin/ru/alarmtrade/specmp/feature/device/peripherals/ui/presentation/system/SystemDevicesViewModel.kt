package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.systemPeripheralTypesFor
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.toPeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.SystemPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.needsAttention
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/** Опрос — в `repeatOnSubscription`: экран в фоне блок не опрашивает; потеря связи отменяет опрос. */
@OptIn(ExperimentalCoroutinesApi::class)
class SystemDevicesViewModel(
    deviceSession: DeviceSession,
    private val repository: SystemPeripheralsRepository,
    private val programMode: ProgramModeEntry,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<SystemDevicesState, SystemDevicesEffect>(deviceSession, SystemDevicesState()) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
        intent { repeatOnSubscription { poll() } }
        pinCooldown.lockedFor(select { systemId.takeIf { pin != null } })
            .collectIntoState { lock -> copy(pin = pin?.copy(lockedFor = lock)) }
    }

    private suspend fun poll() = subIntent {
        // SDK — на IO; combine и пересборка групп/сводки на каждый шаг опроса — на Default; на Main — только ошибки.
        combine(connectedDevice, select { pollGeneration }) { device, _ -> device }
            .flatMapLatest { device -> if (device == null) emptyFlow() else pollFlow().flowOn(dispatchers.io) }
            .onEach { response -> if (response is Response.Success) reduce { withStep(response.value) } }
            .flowOn(dispatchers.default)
            .collect { response -> if (response is Response.Error) onPollError(response) }
    }

    private fun pollFlow(): Flow<Response<PeripheralPollStep>> = flow {
        subIntent {
            val bluetoothVersion = (deviceSession.readBluetoothModule() as? Response.Success)?.value?.manufacturer
                ?.mainFirmwareVersion
            val types = systemPeripheralTypesFor(bluetoothVersion)
            reduce {
                copy(
                    bluetoothVersion = bluetoothVersion,
                    types = types,
                    pollingType = if (firstPassDone) null else types.firstOrNull(),
                    pollFailed = false,
                )
            }
            emitAll(repository.poll(types))
        }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) = subIntent {
        reduce {
            copy(
                deviceName = device.systemName,
                systemId = device.systemId,
                firmwareVersion = device.displayFirmwareVersion,
                canOpenPairing = device.features?.contains(DeviceFeature.DYNAMIC_DEVICES) == true,
            )
        }
    }

    // Работа с блоком уже отменена (launchOnDevice); «Закрываем сессию…» не сбрасываем — к поиску уходим всё равно.
    private suspend fun onConnectionLost() = subIntent {
        reduce {
            copy(
                blinking = null,
                pin = null,
                firmware = firmware.takeIf { it is FirmwareStep.Closing },
            )
        }
    }

    // Без связи ошибку покажет хост («Потеря связи»); при связи — «Повторить», если данных ещё нет.
    private suspend fun onPollError(error: Response.Error) = subIntent {
        logger.w(error.exception) { "Peripheral poll failed" }
        reduce { if (firstPassDone) this else copy(pollFailed = true, pollingType = null) }
    }

    fun onRetry() {
        intent { reduce { copy(pollFailed = false, pollGeneration = pollGeneration + 1) } }
    }

    fun onSelect(key: DeviceKey) {
        intent { reduce { copy(selected = key, detailsOpen = true) } }
    }

    fun onDetailsDismiss() {
        intent { reduce { copy(detailsOpen = false) } }
    }

    /** «Показать» в сводке: сначала устройства без связи, затем остальные проблемные. */
    fun onShowProblem() {
        intent {
            val target = currentState.summary.focus ?: return@intent
            reduce { copy(selected = target, highlight = Highlight(target, (highlight?.seq ?: 0) + 1)) }
        }
    }

    fun onBlink(key: DeviceKey) {
        val device = currentState.device(key) ?: return
        if (currentState.blinking != null) return
        launchOnDevice {
            reduce { copy(blinking = key) }
            try {
                when (withContext(dispatchers.io) { repository.blink(device.address) }) {
                    is Response.Success -> {
                        postSideEffect(SystemDevicesEffect.Blinking(device.type, device.number))
                        delay(BLINK_SHOWN)
                    }

                    is Response.Error -> postSideEffect(SystemDevicesEffect.BlinkFailed)
                }
            } finally {
                reduce { copy(blinking = null) }
            }
        }
    }

    fun onUpdate(key: DeviceKey) {
        intent { reduce { copy(firmware = FirmwareStep.Confirm(key)) } }
    }

    fun onFirmwareConfirm() {
        val step = currentState.firmware ?: return
        if (step is FirmwareStep.Waiting || step is FirmwareStep.Closing) return
        enterFirmwareMode(step.device, pin = null)
    }

    fun onFirmwareDismiss() {
        intent {
            reduce {
                if (firmware is FirmwareStep.Confirm ||
                    firmware is FirmwareStep.Failed
                ) {
                    copy(firmware = null)
                } else {
                    this
                }
            }
        }
    }

    fun onPinSubmit(pin: String) {
        val dialog = currentState.pin ?: return
        if (dialog.sending) return
        val device = currentState.firmware?.device ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        enterFirmwareMode(device, servicePin)
    }

    fun onPinDismiss() {
        intent { reduce { if (pin?.sending == true) this else copy(pin = null, firmware = null) } }
    }

    // Режим программирования — заранее через ProgramModeEntry: так неверный PIN даёт диалог и кулдаун, как везде.
    private fun enterFirmwareMode(key: DeviceKey, pin: ServicePin?) {
        val systemId = currentState.systemId ?: return
        val device = currentState.device(key) ?: return
        launchOnDevice {
            reduce {
                copy(
                    firmware = FirmwareStep.Waiting(key),
                    pin = this.pin?.copy(sending = true),
                )
            }
            when (withContext(dispatchers.io) { programMode.enter(systemId, pin) }) {
                ProgramModeEntryResult.ENTERED -> sendFirmwareMode(key, device)
                ProgramModeEntryResult.PIN_LOCKED -> showPin(wrong = false)
                ProgramModeEntryResult.WRONG_PIN -> showPin(wrong = pin != null)
                ProgramModeEntryResult.FAILED -> fail(key, FirmwareFailure.REFUSED)
            }
        }
    }

    private suspend fun sendFirmwareMode(key: DeviceKey, device: SystemPeripheral) = subIntent {
        reduce { copy(pin = null) }
        when (val result = withContext(dispatchers.io) { repository.enterFirmwareMode(device.address) }) {
            is Response.Success -> {
                reduce { copy(firmware = FirmwareStep.Closing(key)) }
                delay(CLOSING_SHOWN)
                postSideEffect(SystemDevicesEffect.OpenDeviceSearch)
            }

            is Response.Error -> {
                val reason = when (result.exception.toPeripheralFailure()) {
                    PeripheralFailure.PROGRAM_MODE_REFUSED -> FirmwareFailure.REFUSED
                    else -> FirmwareFailure.NO_ANSWER
                }
                fail(key, reason)
            }
        }
    }

    private suspend fun fail(key: DeviceKey, reason: FirmwareFailure) = subIntent {
        reduce { copy(firmware = FirmwareStep.Failed(key, reason), pin = null) }
    }

    private suspend fun showPin(wrong: Boolean) = subIntent {
        reduce {
            val previous = pin
            copy(
                pin = ServicePinDialogState(
                    wrong = wrong,
                    wrongCount = (previous?.wrongCount ?: 0) + if (wrong) 1 else 0,
                    lockedFor = previous?.lockedFor,
                ),
            )
        }
    }

    private companion object {
        val BLINK_SHOWN = 3.seconds
        val CLOSING_SHOWN = 1.seconds
    }
}

/** Неответивший тип оставляет прежние устройства: блок не сообщал, что их нет. */
internal fun SystemDevicesState.withStep(step: PeripheralPollStep): SystemDevicesState {
    val current = groups.associate { it.type to it.devices }
    val devices = if (step.devices != null) current + (step.type to step.devices) else current
    val passDone = firstPassDone || step.index >= step.total - 1
    val groups = types.mapNotNull { type -> devices[type]?.takeIf { it.isNotEmpty() }?.let { DeviceGroup(type, it) } }
    val flat = groups.flatMap { it.devices }
    return copy(
        groups = groups,
        devices = flat,
        summary = summaryOf(flat),
        polled = if (firstPassDone) polled else step.index + 1,
        pollingType = if (passDone) null else types.getOrNull(step.index + 1),
        firstPassDone = passDone,
    )
}

/** Считается в `reduce`, а не в composition: опрос обновляет экран часто. */
internal fun summaryOf(devices: List<SystemPeripheral>): DevicesSummary {
    val attention = devices.filter { it.needsAttention() }
    val offline = attention.firstOrNull { it.link == PeripheralLink.OFFLINE }
    return DevicesSummary(
        total = devices.size,
        online = devices.count { it.link != PeripheralLink.OFFLINE },
        attention = attention.size,
        anyOffline = offline != null,
        focus = (offline ?: attention.firstOrNull())?.key,
    )
}
