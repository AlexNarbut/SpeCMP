package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim

class VehicleSelectTransitionsTest {

    private val brand = CarBrand(8, "Audi", null)
    private val model = CarModel(6, "A1", null, 2010..2014)
    private val base = VehicleSelectState(
        mode = VehicleSelectModeUi(
            pick = true,
            configurationsOnly = false,
            typeSelectable = false,
            deviceName = "DX-9X",
            firmwareCanCode = null
        )
    )
    private val inCard = base.copy(
        selection = CatalogSelection(brand, model, carId = 1),
        trims = LCEState.Content(listOf(CarTrim(1, "1.4"))),
        query = "a1"
    )

    @Test
    fun `crumbs step to brands, brand models and model trims`() {
        assertEquals(CatalogSelection(), inCard.withCrumb(CrumbTarget.TOP).selection)
        assertEquals(CatalogSelection(brand), inCard.withCrumb(CrumbTarget.BRAND).selection)
        assertEquals(
            CatalogSelection(brand, model, carId = null),
            inCard.withCrumb(CrumbTarget.MODEL).selection
        )
        assertEquals(LCEState.None, inCard.withCrumb(CrumbTarget.BRAND).trims)
        assertEquals("", inCard.withCrumb(CrumbTarget.MODEL).query)
    }

    @Test
    fun `closing a single-trim card on a phone goes straight to the models`() {
        assertNull(inCard.withCardClosed(VehicleLayout.COMPACT).selection.model)
        assertEquals(model, inCard.withCardClosed(VehicleLayout.EXPANDED).selection.model)
    }

    @Test
    fun `closing a card opened from the CAN search returns to the start`() {
        val fromCan = inCard.copy(selection = inCard.selection.copy(cardSource = CardSource.CAN))

        assertEquals(CatalogSelection(), fromCan.withCardClosed(VehicleLayout.COMPACT).selection)
    }

    @Test
    fun `phone back walks model to brand to top, wide layout has no catalog step`() {
        val atTrims = inCard.copy(selection = CatalogSelection(brand, model))
        val atModels = atTrims.catalogStepBack(VehicleLayout.COMPACT)

        assertEquals(CatalogSelection(brand), atModels?.selection)
        assertEquals(CatalogSelection(), atModels?.catalogStepBack(VehicleLayout.COMPACT)?.selection)
        assertNull(base.catalogStepBack(VehicleLayout.COMPACT))
        assertNull(atTrims.catalogStepBack(VehicleLayout.EXPANDED))
    }
}
