package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary

/**
 * [lines] — новые первыми (лента с `reverseLayout`: низ — индекс 0). [summary] — весь лог блока, не только
 * загруженные строки; счётчик и время последней строки ведутся по живым строкам, в т. ч. на паузе.
 */
@Immutable
data class SignalLogState(
    val deviceName: String? = null,
    val serial: String? = null,
    val load: LCEState<Unit, SignalLogError> = LCEState.Loading,
    val lines: ImmutableList<SignalLogLine> = persistentListOf(),
    val hasOlder: Boolean = false,
    val loadingOlder: Boolean = false,
    val summary: SignalLogSummary = EmptySummary,
    val connected: Boolean = true,
    val paused: Boolean = false,
    val pendingCount: Int = 0,
    val search: SignalLogSearch? = null,
    val dialog: SignalLogDialog? = null,
    val sheetOpen: Boolean = false,
    val share: SignalLogShareForm = SignalLogShareForm(),
) {
    val loaded: Boolean get() = load is LCEState.Content

    /** Поделиться, очистить и искать можно, только когда в логе блока есть строки. */
    val hasLines: Boolean get() = loaded && summary.count > 0
}

@Immutable
data class SignalLogSearch(
    val query: String = "",
    val matches: ImmutableList<SignalLogLine> = persistentListOf(),
    val current: Int = -1,
    val onlyMatches: Boolean = false,
) {
    val currentLine: SignalLogLine? get() = matches.getOrNull(current)
}

@Immutable
data class SignalLogShareForm(
    val problem: String = "",
    val problemError: Boolean = false,
    val busy: SignalLogShareAction? = null,
    val sendFailed: Boolean = false,
)

enum class SignalLogShareAction { SEND, SAVE }

@Immutable
sealed interface SignalLogDialog {
    data class ConfirmClear(val count: Int) : SignalLogDialog
}

enum class SignalLogError { LOAD_FAILED, NO_SERIAL }

internal val EmptySummary = SignalLogSummary(count = 0, firstTimeMs = null, lastTimeMs = null)
