package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import kotlin.time.Duration
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.toCloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/**
 * Обрыв связи на процессе возвращает на старт с причиной: до вычисления на сервере попытка цела, позже — могла
 * списаться (счётчик перечитывается). Процедура живёт, пока жива VM, а не пока экран подписан на state.
 */
class CloneViewModel(
    deviceSession: DeviceSession,
    private val repository: CloneRepository,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    fromBypass: Boolean,
) : BaseDeviceViewModel<CloneState, CloneEffect>(deviceSession, CloneState(fromBypass = fromBypass)) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CloneDialog.Pin } })
            .collectIntoState { lock -> withPinLock(lock) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) = subIntent {
        reduce {
            copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                usb = deviceSession.transport.value != ConnectionTransport.BLE,
            )
        }
        refreshAttempts()
    }

    private suspend fun refreshAttempts() = subIntent {
        val attempts = when (val response = withContext(dispatchers.io) { repository.attempts() }) {
            is Response.Success -> response.value?.let(CloneAttempts::Known) ?: CloneAttempts.Unknown
            is Response.Error -> CloneAttempts.Unknown
        }
        reduce { copy(attempts = attempts) }
    }

    private suspend fun onConnectionLost() = subIntent {
        reduce {
            val running = phase as? ClonePhase.Running ?: return@reduce this
            val interruption = if (running.stage.ordinal < CloneStage.CALCULATING.ordinal) {
                CloneInterruption.ATTEMPT_KEPT
            } else {
                CloneInterruption.CHECK_ATTEMPTS
            }
            copy(phase = ClonePhase.Start(interruption), dialog = null)
        }
    }

    fun onStartClick() {
        intent {
            reduce { if (canStart && phase !is ClonePhase.Running) copy(dialog = CloneDialog.Confirm) else this }
        }
    }

    /** Диалог забирается сразу — повторный тап не перезапустит процедуру. */
    fun onConfirm() {
        intent {
            if (currentState.dialog != CloneDialog.Confirm) return@intent
            reduce { copy(dialog = null) }
            run()
        }
    }

    /** PIN живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        intent {
            val dialog = currentState.dialog as? CloneDialog.Pin ?: return@intent
            if (dialog.pin.sending) return@intent
            val servicePin = ServicePin.parseOrNull(pin) ?: return@intent
            val systemId = currentState.deviceId ?: return@intent
            if (pinCooldown.remainingNow(systemId) > Duration.ZERO) return@intent
            reduce { copy(dialog = CloneDialog.Pin(dialog.pin.copy(sending = true, wrong = false))) }
            launchOnDevice {
                when (withContext(dispatchers.io) { deviceSession.setServicePin(servicePin) }) {
                    is Response.Error -> reduce { withPinDialog(dialog.pin) }

                    is Response.Success -> {
                        reduce { copy(dialog = null) }
                        run(pinTries = dialog.pin.wrongCount)
                    }
                }
            }
        }
    }

    fun onDialogDismiss() {
        intent {
            reduce {
                val sending = (dialog as? CloneDialog.Pin)?.pin?.sending == true
                if (sending) this else copy(dialog = null)
            }
        }
    }

    fun onBack() {
        intent {
            val state = currentState
            when {
                state.phase !is ClonePhase.Running -> postSideEffect(CloneEffect.Close)
                !state.locked -> reduce { copy(dialog = CloneDialog.Exit) }
            }
        }
    }

    fun onExitConfirm() {
        intent {
            if (currentState.locked) return@intent
            cancelLaunched(PROCEDURE)
            reduce { copy(phase = ClonePhase.Start(), dialog = null) }
        }
    }

    /**
     * [pinTries] — сколько раз PIN уже запрашивали в этой попытке. Сюда доходят, только когда процедура не идёт, —
     * замена по ключу ничего не обрывает.
     */
    private fun run(pinTries: Int = 0) {
        launchOnDevice(PROCEDURE) {
            reduce { copy(phase = ClonePhase.Running(CloneStage.PREPARING)) }
            // До итогового события (итог, неверный PIN, сбой): после него работа заканчивается —
            // повтор не упрётся в неё.
            repository.run()
                .transformWhile { step ->
                    emit(step)
                    step is Response.Success && step.value is CloneEvent.Stage
                }
                .flowOn(dispatchers.io)
                .collect { step ->
                    when (step) {
                        is Response.Success -> onEvent(step.value)
                        is Response.Error -> onFailure(step.exception.toCloneProcedureFailure(), pinTries)
                    }
                }
        }
    }

    private suspend fun onEvent(event: CloneEvent) = subIntent {
        when (event) {
            is CloneEvent.Stage -> reduce { copy(phase = ClonePhase.Running(event.stage, event.inQueue)) }
            is CloneEvent.Finished -> finish(event.result)
        }
    }

    private suspend fun onFailure(failure: CloneProcedureFailure, pinTries: Int) = subIntent {
        when (failure) {
            CloneProcedureFailure.WRONG_PIN -> {
                currentState.deviceId?.let(pinCooldown::start)
                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                reduce {
                    withPinDialog(ServicePinDialogState(wrong = pinTries > 0, wrongCount = pinTries + 1))
                        .copy(phase = ClonePhase.Start())
                }
            }

            // Сбой при живой связи — ошибка; при обрыве экран уже вернулся на старт (onConnectionLost).
            CloneProcedureFailure.UNKNOWN -> if (currentDevice != null) finish(CloneResult.Failed(CloneFailure.UNKNOWN))
        }
    }

    // Счётчик попыток — отдельной работой: «Повторить» сразу после итога не ждёт его и не обрывает.
    private suspend fun finish(result: CloneResult) = subIntent {
        reduce { copy(phase = ClonePhase.Finished(result)) }
        launchOnDevice { refreshAttempts() }
    }

    private companion object {
        const val PROCEDURE = "procedure"
    }
}
