package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

sealed interface CloneAttempts {
    data object Loading : CloneAttempts

    /** Сервер не ответил: счётчик скрыт, старт разрешён. */
    data object Unknown : CloneAttempts

    data class Known(val count: Int) : CloneAttempts
}

enum class CloneInterruption {
    /** Оборвалось до вычисления на сервере — попытка точно не потрачена. */
    ATTEMPT_KEPT,

    /** Оборвалось на вычислении или позже — попытка могла списаться. */
    CHECK_ATTEMPTS,
}

sealed interface ClonePhase {
    data class Start(val interruption: CloneInterruption? = null) : ClonePhase

    data class Running(val stage: CloneStage, val inQueue: Boolean = false) : ClonePhase

    data class Finished(val result: CloneResult) : ClonePhase
}

sealed interface CloneDialog {
    data object Confirm : CloneDialog

    data class Pin(val pin: ServicePinDialogState = ServicePinDialogState()) : CloneDialog

    data object Exit : CloneDialog
}

@Immutable
data class CloneState(
    val deviceName: String? = null,
    val deviceId: Long? = null,
    val usb: Boolean = false,
    val attempts: CloneAttempts = CloneAttempts.Loading,
    val phase: ClonePhase = ClonePhase.Start(),
    val dialog: CloneDialog? = null,
    val fromBypass: Boolean = false,
) {
    /** Старт возможен: попытки не исчерпаны (неизвестно — можно). */
    val canStart: Boolean get() = (attempts as? CloneAttempts.Known)?.count != 0

    val locked: Boolean get() = (phase as? ClonePhase.Running)?.stage?.locked == true

    /** Диалог PIN с [pin]; отсчёт блокировки открытого диалога PIN сохраняется (он приходит отдельным потоком). */
    fun withPinDialog(pin: ServicePinDialogState): CloneState {
        val lockedFor = (dialog as? CloneDialog.Pin)?.pin?.lockedFor
        return copy(dialog = CloneDialog.Pin(pin.copy(lockedFor = lockedFor)))
    }

    /** Блокировка ввода PIN — в открытый диалог PIN; без него не меняется. */
    fun withPinLock(lock: Duration?): CloneState {
        val pinDialog = dialog as? CloneDialog.Pin ?: return this
        return copy(dialog = pinDialog.copy(pin = pinDialog.pin.copy(lockedFor = lock)))
    }
}
