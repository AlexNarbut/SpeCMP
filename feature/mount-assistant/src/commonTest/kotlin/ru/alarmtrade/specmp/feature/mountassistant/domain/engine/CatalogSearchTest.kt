package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic

class CatalogSearchTest {
    private val topicLabels = mapOf(
        AssistantTopic.FIRMWARE to "Прошивка",
        AssistantTopic.AUTOSTART to "Обход и Clone",
        AssistantTopic.SETTINGS to "Настройки",
        AssistantTopic.STATE to "Состояние",
        AssistantTopic.DEVICES to "Устройства",
        AssistantTopic.VEHICLE to "Автомобиль",
        AssistantTopic.SUPPORT to "Поддержка",
    )
    private val actionLabels = mapOf(
        AssistantAction.FIRMWARE_CHECK to "Проверить прошивку",
        AssistantAction.BYPASS_RUN to "Обучить обход",
        AssistantAction.CLONE_RUN to "Запустить Clone",
        AssistantAction.SETTINGS_LOAD to "Загрузить настройки",
        AssistantAction.STATE to "Состояние блока",
    )
    private val keywords = mapOf(
        AssistantTopic.AUTOSTART to listOf("обход", "бесключ", "clone"),
        AssistantTopic.STATE to listOf("датчик", "капот"),
        AssistantTopic.DEVICES to listOf("метк", "прижив"),
        AssistantTopic.SUPPORT to listOf("заявк", "звон"),
    )

    private fun match(query: String) = matchCatalog(query, actionLabels, topicLabels, keywords)

    @Test
    fun `stems are words from three letters cut to six`() {
        assertEquals(listOf("бескл", "обход"), stemsOf("Бескл, обход и ок"))
        assertEquals(listOf("прошив"), stemsOf("прошивка"))
    }

    @Test
    fun `keyword finds topic and all its actions`() {
        val result = match("бесключевой обход")
        assertEquals(listOf(AssistantTopic.AUTOSTART), result.topics)
        assertEquals(AssistantAction.of(AssistantTopic.AUTOSTART), result.actions)
    }

    @Test
    fun `request and pairing words find support and devices`() {
        assertEquals(listOf(AssistantTopic.SUPPORT), match("оставить заявку").topics)
        assertEquals(listOf(AssistantTopic.DEVICES), match("приживить метку").topics)
    }

    @Test
    fun `stem in label finds topic and action`() {
        val result = match("прошивку")
        assertEquals(listOf(AssistantTopic.FIRMWARE), result.topics)
        assertTrue(AssistantAction.FIRMWARE_CHECK in result.actions)
    }

    @Test
    fun `action label matches without its topic`() {
        val result = match("загрузить")
        assertEquals(emptyList(), result.topics)
        assertEquals(listOf(AssistantAction.SETTINGS_LOAD), result.actions)
    }

    @Test
    fun `case does not matter and blank finds nothing`() {
        assertEquals(listOf(AssistantTopic.AUTOSTART), match("CLONE").topics)
        assertTrue(match("   ").isEmpty)
        assertTrue(match("xyz").isEmpty)
    }

    @Test
    fun `stem ranges mark first occurrence of each stem in text order`() {
        assertEquals(listOf(0..5, 12..16), stemRanges("Бесключевой обход", "обход бесключевой"))
        assertEquals(listOf(10..14), stemRanges("Запустить Clone", "CLONE"))
        assertEquals(emptyList(), stemRanges("Прошивка", "ab"))
        assertEquals(emptyList(), stemRanges("Прошивка", "датчик"))
    }
}
