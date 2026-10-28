package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration

class MineStateTest {

    private val time = LocalDateTime(2026, 10, 21, 14, 20)
    private fun item(id: Long, name: String, description: String, model: String) =
        LocalConfiguration(id, name, description, model, time, time)

    private val items = listOf(
        item(1, "Нива — без автозапуска", "Сирена тише", "DX-9X"),
        item(2, "Камри", "Автозапуск по температуре", "DX-4GS"),
        item(3, "Веста", "", "dx-9x")
    )

    @Test
    fun `search covers name and description, model filter ignores case`() {
        val state = MineState(list = MineList.Ready(items))

        assertEquals(listOf(2L), state.copy(query = "автозапуск по").filtered.map { it.id })
        assertEquals(listOf(1L, 2L), state.copy(query = "АВТОЗАПУСК").filtered.map { it.id })
        assertEquals(listOf(1L, 3L), state.copy(modelFilter = "DX-9X").filtered.map { it.id })
        assertEquals(listOf("DX-4GS", "DX-9X", "dx-9x"), state.models)
    }
}
