package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalState

/**
 * Экран строится из журнала ([journal]): после прошивки ViewModel может быть и новой, и прежней. [facts] — последние
 * известные факты: при обрыве связи не обнуляются.
 */
@Immutable
data class MountAssistantState(
    val facts: DeviceFacts? = null,
    val journal: JournalState = JournalState(),
    val level: DockLevel = DockLevel.Home,
    val query: String = "",
    val liveSettings: List<FoundSetting> = emptyList(),
    val dockCollapsed: Boolean = false,
    val header: HeaderMode = HeaderMode.AUTO,
    val pulse: Pulse? = null,
    val pin: PinPrompt? = null,
    val awaitingReturn: Boolean = false,
    val archiveCard: Long? = null,
    val passportChecked: Boolean = false,
)

@Immutable
sealed interface DockLevel {
    data object Home : DockLevel

    data class Topic(val topic: AssistantTopic) : DockLevel
}

/** AUTO — развёрнута до первого действия, при прокрутке и клавиатуре — строкой; OPEN/CLOSED — выбор монтажника. */
enum class HeaderMode { AUTO, OPEN, CLOSED }

/** Плитка темы мигает один раз; [seq] перезапускает мигание для той же темы. */
@Immutable
data class Pulse(val topic: AssistantTopic, val seq: Long)

/** Диалог PIN для записи настройки из карточки [entryId]. */
@Immutable
data class PinPrompt(
    val entryId: Long,
    val systemId: Long,
    val wrongCount: Int = 0,
    val sending: Boolean = false,
    val lockedFor: Duration? = null,
)
