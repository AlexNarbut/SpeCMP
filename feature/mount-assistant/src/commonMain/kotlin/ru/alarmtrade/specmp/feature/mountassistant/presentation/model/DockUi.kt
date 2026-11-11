package ru.alarmtrade.specmp.feature.mountassistant.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.CatalogMatch
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.nextStep
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.topicStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.topics
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ActionKind
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.Availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStep
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TopicStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.DockLevel
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.Pulse

@Immutable
data class DockUi(val head: DockHead, val tiles: ImmutableList<DockTile>)

@Immutable
sealed interface DockHead {
    data object Home : DockHead

    data class Topic(val topic: AssistantTopic, val status: TopicStatus) : DockHead

    data class Found(val count: Int) : DockHead

    data object Nothing : DockHead
}

/** [blocked] — причина блокировки; `null` — доступно. */
@Immutable
sealed interface DockTile {
    val key: String

    data class Hero(val step: NextStep, val blocked: BlockReason?, val version: String?, val pulseSeq: Long?) :
        DockTile {
        override val key: String get() = "hero"
    }

    data class Topic(
        val topic: AssistantTopic,
        val status: TopicStatus,
        val blocked: Boolean,
        val single: AssistantAction?,
        val pulseSeq: Long?,
    ) : DockTile {
        override val key: String get() = "topic:$topic"
    }

    data class Action(
        val action: AssistantAction,
        val blocked: BlockReason?,
        val primary: Boolean,
        val version: String?,
    ) : DockTile {
        override val key: String get() = "action:$action"
        val inFeed: Boolean get() = action.kind == ActionKind.IN_FEED
    }

    data class Setting(val setting: FoundSetting, val blocked: BlockReason?) : DockTile {
        override val key: String get() = "setting:${setting.id}"
    }

    data class SearchAll(val count: Int) : DockTile {
        override val key: String get() = "all"
    }
}

private const val SEARCH_SETTINGS_LIMIT = 6

/**
 * Содержимое пульта: дом (следующий шаг + темы), тема (её действия) или поиск ([match] — совпадения каталога).
 * Пустой [query] — пульт без поиска: в широком окне найденное показывается отдельной панелью.
 */
fun dockOf(
    facts: DeviceFacts,
    progress: MountProgress,
    level: DockLevel,
    query: String,
    match: CatalogMatch,
    settings: List<FoundSetting>,
    pulse: Pulse?,
): DockUi {
    val next = nextStep(facts, progress)
    val version = progress.firmware?.main?.latest
    fun action(action: AssistantAction): DockTile.Action? {
        val available = availability(action, facts, progress)
        if (available == Availability.Hidden) return null
        return DockTile.Action(action, (available as? Availability.Blocked)?.reason, action == next.action, version)
    }
    val topicTiles = topics(facts, progress).associateBy { it.topic }.mapValues { (topic, entry) ->
        DockTile.Topic(
            topic = topic,
            status = topicStatus(topic, facts, progress),
            blocked = entry.blocked,
            single = entry.actions.singleOrNull(),
            pulseSeq = pulse?.takeIf { it.topic == topic }?.seq,
        )
    }
    if (query.isNotBlank()) return searchDock(facts, progress, match, settings, topicTiles, ::action)
    return when (level) {
        DockLevel.Home -> {
            val blocked = availability(next.action, facts, progress) as? Availability.Blocked
            val hero = DockTile.Hero(next, blocked?.reason, version, pulseSeq = null)
            DockUi(DockHead.Home, (listOf(hero) + topicTiles.values).toImmutableList())
        }

        is DockLevel.Topic -> DockUi(
            DockHead.Topic(level.topic, topicStatus(level.topic, facts, progress)),
            AssistantAction.of(level.topic).mapNotNull(::action).sortedByDescending { it.primary }.toImmutableList(),
        )
    }
}

private fun searchDock(
    facts: DeviceFacts,
    progress: MountProgress,
    match: CatalogMatch,
    settings: List<FoundSetting>,
    topicTiles: Map<AssistantTopic, DockTile.Topic>,
    action: (AssistantAction) -> DockTile.Action?,
): DockUi {
    val findBlocked = availability(AssistantAction.SETTINGS_FIND, facts, progress)
    val settingsShown = if (findBlocked == Availability.Hidden) emptyList() else settings
    val topics = match.topics.mapNotNull(topicTiles::get)
    val singleActionTopics = topics.filter { it.single != null }.map { it.topic }
    val tiles = buildList {
        addAll(topics)
        addAll(match.actions.filterNot { it.topic in singleActionTopics }.mapNotNull(action))
        settingsShown.take(SEARCH_SETTINGS_LIMIT).forEach {
            add(DockTile.Setting(it, (findBlocked as? Availability.Blocked)?.reason))
        }
        if (settingsShown.size > 1) add(DockTile.SearchAll(settingsShown.size))
    }
    if (tiles.isEmpty()) {
        val fallback = listOf(AssistantAction.SETTINGS_ALL, AssistantAction.SUPPORT)
            .mapNotNull(action).map { it.copy(primary = false) }
        return DockUi(DockHead.Nothing, fallback.toImmutableList())
    }
    return DockUi(DockHead.Found(tiles.count { it !is DockTile.SearchAll }), tiles.toImmutableList())
}

/** Свёрнутый пульт: быстрые действия, кроме скрытых. */
fun quickActions(facts: DeviceFacts, progress: MountProgress): ImmutableList<DockTile.Action> =
    AssistantAction.Quick.mapNotNull { action ->
        when (val available = availability(action, facts, progress)) {
            Availability.Hidden -> null
            else -> DockTile.Action(action, (available as? Availability.Blocked)?.reason, false, null)
        }
    }.toImmutableList()

val EmptyDock: DockUi = DockUi(DockHead.Home, persistentListOf())

enum class ResultGroup { TOPICS, ACTIONS, SETTINGS, TRY }

/** Найденное по группам в порядке показа; при пустом результате — действия-подсказки одной группой. */
fun resultGroups(results: DockUi): List<Pair<ResultGroup, ImmutableList<DockTile>>> {
    if (results.head == DockHead.Nothing) return listOf(ResultGroup.TRY to results.tiles)
    return results.tiles
        .groupBy { tile ->
            when (tile) {
                is DockTile.Topic -> ResultGroup.TOPICS
                is DockTile.Hero, is DockTile.Action -> ResultGroup.ACTIONS
                is DockTile.Setting, is DockTile.SearchAll -> ResultGroup.SETTINGS
            }
        }
        .entries.sortedBy { it.key }
        .map { (group, tiles) -> group to tiles.toImmutableList() }
}
