package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel

/** Обучение живёт, пока жива VM, а не пока экран подписан на state: уход экрана в фон его не прерывает. */
class BypassLearningViewModel(
    deviceSession: DeviceSession,
    private val repository: BypassLearningRepository,
    private val programMode: ProgramModeEntry,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<BypassState, BypassEffect>(deviceSession, BypassState()) {

    override suspend fun onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is BypassDialog.Pin } })
            .collectIntoState { lock -> withPinLock(lock) }
    }

    private fun onDeviceReady(device: AlarmDevice) {
        reduce {
            copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                usb = deviceSession.transport.value != ConnectionTransport.BLE,
                canCode = device.can?.code,
                cloneAvailable = device.features?.contains(DeviceFeature.CLONE) == true,
            )
        }
    }

    private fun onConnectionLost() {
        reduce {
            if (phase is BypassPhase.Running) {
                copy(phase = BypassPhase.Start(interrupted = true), dialog = null)
            } else {
                this
            }
        }
    }

    /** Вход в режим программирования без PIN: SDK пробует сохранённый или 1111. */
    fun onStartClick() {
        if (currentState.phase is BypassPhase.Running) return
        start(pin = null)
    }

    /** PIN живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        val dialog = currentState.dialog as? BypassDialog.Pin ?: return
        if (dialog.pin.sending) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        start(servicePin)
    }

    fun onDialogDismiss() {
        reduce {
            val sending = (dialog as? BypassDialog.Pin)?.pin?.sending == true
            if (sending) this else copy(dialog = null)
        }
    }

    fun onBack() {
        if (currentState.phase is BypassPhase.Running) {
            reduce { copy(dialog = BypassDialog.Exit) }
        } else {
            postSideEffect(BypassEffect.Close)
        }
    }

    fun onExitConfirm() {
        cancelLaunched(LEARNING)
        reduce { copy(phase = BypassPhase.Start(), dialog = null) }
    }

    // Сюда доходят, только когда прежний запуск закончился (фаза не Running, PIN не отправляется), — замена по ключу
    // ничего не обрывает.
    private fun start(pin: ServicePin?) {
        val systemId = currentState.deviceId ?: return
        launchOnDevice(LEARNING) {
            val dialog = currentState.dialog as? BypassDialog.Pin
            reduce {
                if (dialog != null) {
                    copy(dialog = dialog.copy(pin = dialog.pin.copy(sending = true, wrong = false)))
                } else {
                    copy(phase = BypassPhase.Running(), dialog = null)
                }
            }
            when (withContext(dispatchers.io) { programMode.enter(systemId, pin) }) {
                ProgramModeEntryResult.ENTERED -> learn()

                ProgramModeEntryResult.PIN_LOCKED -> showPin(dialog, wrong = false)

                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                ProgramModeEntryResult.WRONG_PIN -> showPin(dialog, wrong = pin != null)

                ProgramModeEntryResult.FAILED -> finishIfConnected(BypassResult.Error(BypassError.START_FAILED))
            }
        }
    }

    private fun showPin(previous: BypassDialog.Pin?, wrong: Boolean) {
        val count = (previous?.pin?.wrongCount ?: 0) + if (wrong) 1 else 0
        reduce {
            // Отсчёт блокировки приходит отдельным потоком по блоку — новый диалог не должен его потерять.
            val lockedFor = (dialog as? BypassDialog.Pin)?.pin?.lockedFor
            copy(
                phase = BypassPhase.Start(),
                dialog = BypassDialog.Pin(
                    ServicePinDialogState(wrong = wrong, wrongCount = count, lockedFor = lockedFor),
                ),
            )
        }
    }

    private suspend fun learn() {
        reduce { copy(phase = BypassPhase.Running(), dialog = null) }
        // До итогового события (итог, сбой): после него работа заканчивается — «Повторить» не упрётся в неё.
        repository.learn()
            .transformWhile { step ->
                emit(step)
                step is Response.Success && step.value is BypassEvent.Step
            }
            .flowOn(dispatchers.io)
            .collect { step ->
                when (step) {
                    is Response.Success -> onEvent(step.value)
                    is Response.Error -> finishIfConnected(BypassResult.Error(BypassError.START_FAILED))
                }
            }
    }

    private fun onEvent(event: BypassEvent) {
        when (event) {
            is BypassEvent.Step -> reduce { copy(phase = BypassPhase.Running(event.step)) }
            is BypassEvent.Finished -> reduce { copy(phase = BypassPhase.Finished(event.result)) }
        }
    }

    // Сбой при живой связи — ошибка; при обрыве экран уже вернулся на старт (onConnectionLost).
    private fun finishIfConnected(result: BypassResult) {
        if (currentDevice == null) return
        reduce { copy(phase = BypassPhase.Finished(result), dialog = null) }
    }

    private companion object {
        const val LEARNING = "learning"
    }
}
