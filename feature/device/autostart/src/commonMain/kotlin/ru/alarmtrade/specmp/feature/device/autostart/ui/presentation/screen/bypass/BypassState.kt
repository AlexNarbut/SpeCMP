package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState

/** Фаза экрана: старт → шаги → итог. */
sealed interface BypassPhase {
    /** @property interrupted вернулись на старт после обрыва связи на шаге. */
    data class Start(val interrupted: Boolean = false) : BypassPhase

    /** @property step `null` — вход в режим программирования, шагов от блока ещё нет. */
    data class Running(val step: BypassStep? = null) : BypassPhase

    data class Finished(val result: BypassResult) : BypassPhase
}

sealed interface BypassDialog {
    data class Pin(val pin: ServicePinDialogState = ServicePinDialogState()) : BypassDialog

    /** «Прервать?» — выход во время обучения. */
    data object Exit : BypassDialog
}

/**
 * Состояние экрана «Бесключевой обход».
 *
 * @property canCode CAN-код прошивки блока (только у блоков с прошивкой под CAN-код) — плашка на старте.
 * @property cloneAvailable у блока есть процедура Clone — на успехе предлагается переход в неё.
 */
@Immutable
data class BypassState(
    val deviceName: String? = null,
    val deviceId: Long? = null,
    val usb: Boolean = false,
    val canCode: String? = null,
    val cloneAvailable: Boolean = false,
    val phase: BypassPhase = BypassPhase.Start(),
    val dialog: BypassDialog? = null,
)
