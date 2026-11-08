package ru.alarmtrade.specmp.feature.configurator.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar
import ru.alarmtrade.specmp.feature.configurator.domain.model.sortedBy

class CloudOrderTest {

    private fun item(id: Int, rating: Int?, day: Int) = CloudConfigurationSummary(
        id = id,
        name = "C$id",
        shortDescription = "",
        deviceName = "DX-9X",
        deviceTitle = "DX-9X",
        car = ConfigurationCar(1, "Lada", "Niva", "Travel"),
        author = "Иванов",
        rating = rating,
        updated = LocalDateTime(2026, 10, day, 12, 0),
        onlyForAuthor = false,
    )

    private val items = listOf(item(1, null, 20), item(2, 4, 10), item(3, 4, 15), item(4, 5, 1))

    @Test
    fun `best is by rating then by date, unrated last`() {
        assertEquals(listOf(4, 3, 2, 1), items.sortedBy(CloudConfigurationOrder.BEST).map { it.id })
    }

    @Test
    fun `newest is by date`() {
        assertEquals(listOf(1, 3, 2, 4), items.sortedBy(CloudConfigurationOrder.NEWEST).map { it.id })
    }

    @Test
    fun `pdf attachment is detected by url`() {
        assertEquals(true, ConfigurationAttachment("Карта", null, "https://x/map.PDF?v=2").isPdf)
        assertEquals(false, ConfigurationAttachment("Фото", "p.jpg", "https://x/photo.jpg").isPdf)
    }

    @Test
    fun `car title skips blank parts`() {
        assertEquals("Lada Niva", ConfigurationCar(1, "Lada", "Niva", " ").title)
    }
}
