package ru.alarmtrade.specmp.mountpassport

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehiclePick
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.navigation.toPassportPick

class PassportCatalogPickTest {

    private fun car(years: IntRange) = CarSummary(
        id = 15,
        brand = "Toyota",
        model = "RAV4",
        name = "",
        years = years,
        canCode = "0213",
        imageUrl = null,
    )

    @Test
    fun catalogCarBecomesTitleWithYears() {
        assertEquals(PassportVehiclePick("15", "Toyota RAV4 2019–2024"), car(2019..2024).toPassportPick())
        assertEquals("Toyota RAV4 2020", car(2020..2020).toPassportPick().title)
    }
}
