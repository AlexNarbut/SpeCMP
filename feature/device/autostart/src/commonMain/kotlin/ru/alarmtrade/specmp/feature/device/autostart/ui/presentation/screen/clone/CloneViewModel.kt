package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import kotlin.time.Duration
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/**
 * Экран «Процедура Clone»: попытки → подтверждение → [CloneRepository.run] по этапам → итог.
 *
 * Обрыв связи на процессе: работа отменяется ([launchOnDevice]), экран возвращается на старт с причиной (D2): до
 * вычисления на сервере попытка точно цела, позже — могла списаться, счётчик перечитывается. Неверный PIN (BLE) — из
 * потока SDK: диалог PIN, повтор с введённым PIN. На этапах записи выход запрещён. Процедура живёт, пока жива VM, а не
 * пока экран подписан на state.
 */
class CloneViewModel(
    deviceSession: DeviceSession,
    private val repository: CloneRepository,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    fromBypass: Boolean,
) : BaseDeviceViewModel<CloneState, CloneEffect>(deviceSession, CloneState(fromBypass = fromBypass)) {

    override suspend fun onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CloneDialog.Pin } })
            .collectIntoState { lock -> withPinLock(lock) }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        reduce {
            copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                usb = deviceSession.transport.value != ConnectionTransport.BLE,
            )
        }
        refreshAttempts()
    }

    private suspend fun refreshAttempts() {
        val attempts = withContext(dispatchers.io) { repository.attempts() }
        reduce { copy(attempts = attempts?.let(CloneAttempts::Known) ?: CloneAttempts.Unknown) }
    }

    private fun onConnectionLost() {
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

    /** «Начать Clone» / «Повторить»: сначала подтверждение с числом попыток. */
    fun onStartClick() {
        reduce { if (canStart && phase !is ClonePhase.Running) copy(dialog = CloneDialog.Confirm) else this }
    }

    /** «Начать» в подтверждении: диалог забирается сразу — повторный тап не перезапустит процедуру. */
    fun onConfirm() {
        if (currentState.dialog != CloneDialog.Confirm) return
        reduce { copy(dialog = null) }
        run()
    }

    /** «Продолжить» в диалоге PIN; значение живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        val dialog = currentState.dialog as? CloneDialog.Pin ?: return
        // PIN уже отправляется — повторный тап не шлёт его второй раз.
        if (dialog.pin.sending) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        val systemId = currentState.deviceId ?: return
        if (pinCooldown.remainingNow(systemId) > Duration.ZERO) return
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

    fun onDialogDismiss() {
        reduce {
            val sending = (dialog as? CloneDialog.Pin)?.pin?.sending == true
            if (sending) this else copy(dialog = null)
        }
    }

    /** «Назад»: на процессе — «Прервать?» (на этапах записи — ничего), иначе уйти сразу ([CloneEffect.Close]). */
    fun onBack() {
        val state = currentState
        when {
            state.phase !is ClonePhase.Running -> postSideEffect(CloneEffect.Close)
            !state.locked -> reduce { copy(dialog = CloneDialog.Exit) }
        }
    }

    /** «Да» в «Прервать?»: процедура отменяется (если запись ещё не началась). */
    fun onExitConfirm() {
        if (currentState.locked) return
        cancelLaunched(PROCEDURE)
        reduce { copy(phase = ClonePhase.Start(), dialog = null) }
    }

    /**
     * [pinTries] — сколько раз PIN уже запрашивали в этой попытке (0 — PIN не вводили). Сюда доходят, только когда
     * процедура не идёт (подтверждение — вне Running, PIN — после её конца), — замена по ключу ничего не обрывает.
     */
    private fun run(pinTries: Int = 0) {
        launchOnDevice(PROCEDURE) {
            reduce { copy(phase = ClonePhase.Running(CloneStage.PREPARING)) }
            // До итогового события (итог, неверный PIN, сбой): после него работа заканчивается —
            // повтор не упрётся в неё.
            repository.run()
                .transformWhile { event ->
                    emit(event)
                    event is CloneEvent.Stage
                }
                .flowOn(dispatchers.io)
                .collect { event -> onEvent(event, pinTries) }
        }
    }

    private fun onEvent(event: CloneEvent, pinTries: Int) {
        when (event) {
            is CloneEvent.Stage -> reduce { copy(phase = ClonePhase.Running(event.stage, event.inQueue)) }

            is CloneEvent.Finished -> finish(event.result)

            CloneEvent.WrongPin -> {
                currentState.deviceId?.let(pinCooldown::start)
                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                reduce {
                    withPinDialog(ServicePinDialogState(wrong = pinTries > 0, wrongCount = pinTries + 1))
                        .copy(phase = ClonePhase.Start())
                }
            }

            // Сбой при живой связи — ошибка; при обрыве экран уже вернулся на старт (onConnectionLost).
            CloneEvent.Failure -> if (currentDevice != null) finish(CloneResult.Failed(CloneFailure.UNKNOWN))
        }
    }

    // Счётчик попыток — отдельной работой: «Повторить» сразу после итога не ждёт его и не обрывает.
    private fun finish(result: CloneResult) {
        reduce { copy(phase = ClonePhase.Finished(result)) }
        launchOnDevice { refreshAttempts() }
    }

    private companion object {
        const val PROCEDURE = "procedure"
    }
}
