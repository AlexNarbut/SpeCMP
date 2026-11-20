package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

@Immutable
internal data class PassportReadArgs(val source: PassportReadSource)

/** Остановка считывания: экран показывает причину и кнопки вместо «Отмена». */
internal sealed interface ReadStop {
    data class UnitFailed(val connectionLost: Boolean) : ReadStop

    data class ConnectionLost(val step: PassportReadStep) : ReadStop

    data class Offline(val canContinueWithout: Boolean) : ReadStop

    data class WrongUnit(val expectedSystemId: String, val actualSystemId: String) : ReadStop

    data object SaveFailed : ReadStop

    data class Failed(val error: PassportError) : ReadStop
}

/** [failure] — считывание оборвалось ошибкой SDK (не фаза): «Повторить» запускает его заново. */
@Immutable
internal data class PassportReadingState(
    val source: PassportReadSource,
    val read: PassportReadState = PassportReadState.Initial,
    val failure: PassportError? = null,
    val settingsSeconds: Int = 0,
) {
    val stop: ReadStop?
        get() = failure?.let(ReadStop::Failed) ?: when (val phase = read.phase) {
            PassportReadPhase.Reading, is PassportReadPhase.Done -> null
            is PassportReadPhase.UnitFailed -> ReadStop.UnitFailed(phase.connectionLost)
            is PassportReadPhase.ConnectionLost -> ReadStop.ConnectionLost(phase.step)
            is PassportReadPhase.Offline -> ReadStop.Offline(phase.canContinueWithout)
            is PassportReadPhase.WrongUnit -> ReadStop.WrongUnit(phase.expectedSystemId, phase.actualSystemId)
            PassportReadPhase.SaveFailed -> ReadStop.SaveFailed
        }

    val done: Boolean get() = read.phase is PassportReadPhase.Done

    /** Доля пройденного: завершённые этапы целиком, идущий — наполовину. */
    val progress: Float
        get() {
            if (done) return 1f
            val finished = read.steps.count { it.status.finished }
            val running = read.steps.count { it.status == PassportStepStatus.Reading }
            return (finished + running * HALF) / read.steps.size.coerceAtLeast(1)
        }
}

private val PassportStepStatus.finished: Boolean
    get() = this != PassportStepStatus.Waiting && this != PassportStepStatus.Reading

private const val HALF = 0.5f
