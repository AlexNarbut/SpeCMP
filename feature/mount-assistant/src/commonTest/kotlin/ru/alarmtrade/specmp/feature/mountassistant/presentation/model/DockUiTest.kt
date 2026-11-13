package ru.alarmtrade.specmp.feature.mountassistant.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.CatalogMatch
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.facts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.DockLevel
import ru.alarmtrade.specmp.feature.mountassistant.testing.switchSetting
import ru.alarmtrade.specmp.feature.mountassistant.testing.timeSetting

class DockUiTest {
    private val facts = facts()
    private val progress = MountProgress()

    private fun dock(query: String, match: CatalogMatch, settings: Int = 0) = dockOf(
        facts = facts,
        progress = progress,
        level = DockLevel.Home,
        query = query,
        match = match,
        settings = listOf(switchSetting, timeSetting).take(settings),
        pulse = null,
    )

    @Test
    fun `blank query keeps the console even when the catalog matched`() {
        val match = CatalogMatch(listOf(AssistantTopic.FIRMWARE), listOf(AssistantAction.FIRMWARE_CHECK))

        val dock = dock(query = "", match = match, settings = 2)

        assertEquals(DockHead.Home, dock.head)
        assertIs<DockTile.Hero>(dock.tiles.first())
    }

    @Test
    fun `topic with a single action is shown without its action`() {
        val match = CatalogMatch(
            topics = listOf(AssistantTopic.VEHICLE, AssistantTopic.FIRMWARE),
            actions = listOf(AssistantAction.VEHICLES, AssistantAction.FIRMWARE_CHECK),
        )

        val keys = dock(query = "авто", match = match).tiles.map { it.key }

        assertTrue("topic:VEHICLE" in keys)
        assertTrue("action:VEHICLES" !in keys)
        assertTrue("action:FIRMWARE_CHECK" in keys)
    }

    @Test
    fun `found counter skips the show-all row`() {
        val match = CatalogMatch(listOf(AssistantTopic.SETTINGS), emptyList())

        val dock = dock(query = "темп", match = match, settings = 2)

        assertIs<DockTile.SearchAll>(dock.tiles.last())
        assertEquals(DockHead.Found(dock.tiles.size - 1), dock.head)
    }

    @Test
    fun `results are grouped as topics then actions then settings`() {
        val match = CatalogMatch(listOf(AssistantTopic.FIRMWARE), listOf(AssistantAction.FIRMWARE_CHECK))

        val groups = resultGroups(dock(query = "прошив", match = match, settings = 2))

        assertEquals(listOf(ResultGroup.TOPICS, ResultGroup.ACTIONS, ResultGroup.SETTINGS), groups.map { it.first })
        val settingKeys = groups.last().second.map { it.key }
        assertEquals(listOf("setting:${switchSetting.id}", "setting:${timeSetting.id}", "all"), settingKeys)
    }

    @Test
    fun `empty result offers fallback actions in one group`() {
        val groups = resultGroups(dock(query = "zzz", match = CatalogMatch(emptyList(), emptyList())))

        assertEquals(listOf(ResultGroup.TRY), groups.map { it.first })
        assertTrue(groups.single().second.all { it is DockTile.Action })
    }

    @Test
    fun `a number comes first as the product row, blocked offline, hidden without the feature`() {
        val none = CatalogMatch(emptyList(), emptyList())
        fun dockFor(f: DeviceFacts) = dockOf(f, progress, DockLevel.Home, "107131", none, emptyList(), pulse = null)

        val dock = dockFor(facts)
        assertEquals(DockTile.ProductNumber("107131", blocked = null), dock.tiles.first())
        assertEquals(DockHead.Found(1), dock.head)
        assertEquals(ResultGroup.PRODUCT, resultGroups(dock).first().first)

        val offline = dockFor(facts(online = false)).tiles.first()
        assertEquals(DockTile.ProductNumber("107131", BlockReason.NO_NETWORK), offline)

        assertEquals(DockHead.Nothing, dockFor(facts(productSearch = false)).head)
    }
}
