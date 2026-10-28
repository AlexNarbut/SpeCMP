package ru.alarmtrade.specmp.feature.vehicle.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specialist.domain.cars.CarBusGroup
import ru.alarmtrade.specialist.domain.cars.CarFeature
import ru.alarmtrade.specialist.domain.cars.CarFeatureGroup
import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.domain.cars.CarMountPointGuide
import ru.alarmtrade.specialist.domain.cars.CarMountPointGuideImage
import ru.alarmtrade.specialist.domain.cars.CarMountSchemeFile
import ru.alarmtrade.specialist.domain.cars.EngineBlockByCanFeature
import ru.alarmtrade.specialist.domain.cars.ImmobilizerBypassFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.BusFamily
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBus
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup as DomainGroup

class CarMappersTest {

    private fun info(
        engine: EngineBlockByCanFeature = EngineBlockByCanFeature.NotAvailable,
        keyless: ImmobilizerBypassFeature = ImmobilizerBypassFeature.NotAvailable,
        description1: String? = null,
        description2: String? = null,
        features: List<CarFeature> = emptyList(),
        guides: List<CarMountPointGuide> = emptyList(),
        schemes: List<CarMountSchemeFile> = emptyList(),
        imageUrl: String? = "https://img/camry.png"
    ) = CarInfo(
        carId = 7,
        carBrand = "Toyota",
        carModel = "Camry",
        carName = "2.5 AT",
        carImageUrl = imageUrl,
        productionYears = 2017..2023,
        engineBlockByCan = engine,
        immobilizerBypass = keyless,
        description1 = description1,
        description2 = description2,
        canCode = "2154",
        features = features,
        mountingGuides = guides,
        mountingSchemes = schemes,
        mountingMaps = emptyList(),
        userManualsExist = false,
        userManuals = emptyList()
    )

    @Test
    fun `every SDK bus maps to a bus of the same name and family`() {
        CarBusGroup.entries.forEach { sdk ->
            assertEquals(sdk.name, sdk.toDomain().name)
        }
        assertEquals(BusFamily.CAN_FD, CarBus.CAN_FD1_OR_CAN_FD3.family)
        assertEquals(BusFamily.LIN, CarBus.LIN_IN_OUT.family)
        assertEquals(BusFamily.IMO, CarBus.IMO_KEY1_PLUS_IMO_KEY2.family)
        assertEquals(BusFamily.UNKNOWN, CarBus.UNKNOWN.family)
    }

    @Test
    fun `capabilities keep support and drop blank notes`() {
        val card = info(
            engine = EngineBlockByCanFeature.Available(isSupported = true, description = "<p>Через CAN2</p>"),
            keyless = ImmobilizerBypassFeature.Available(isSupported = false, description = " ")
        ).toCard()

        assertEquals(CarCapability.Supported("<p>Через CAN2</p>"), card.engineBlockByCan)
        assertEquals(CarCapability.NotSupported(null), card.keylessBypass)
        assertEquals(CarCapability.NoData, info().toCard().engineBlockByCan)
    }

    @Test
    fun `notes skip empty descriptions`() {
        assertEquals(listOf("<b>Важно</b>"), info(description1 = "", description2 = "<b>Важно</b>").toCard().notes)
        assertEquals(emptyList(), info().toCard().notes)
    }

    @Test
    fun `features, photos and documents follow server order`() {
        val card = info(
            features = listOf(
                CarFeature("Двери", null, CarFeatureGroup.MAIN, 2, emptyList(), listOf(CarBusGroup.CAN1)),
                CarFeature("Зажигание", "", CarFeatureGroup.EXTENDED, 1, listOf(CarBusGroup.LIN), emptyList())
            ),
            guides = listOf(
                CarMountPointGuide(
                    "Замок",
                    null,
                    "CAN",
                    listOf(CarMountPointGuideImage("2", "u2", 2), CarMountPointGuideImage("1", "u1", 1))
                )
            ),
            schemes = listOf(CarMountSchemeFile("B", null, "b.pdf", 2), CarMountSchemeFile("A", "", "a.pdf", 1))
        ).toCard()

        assertEquals(listOf("Зажигание", "Двери"), card.features.map { it.name })
        assertNull(card.features.first().description)
        assertEquals(DomainGroup.EXTENDED, card.features.first().group)
        assertEquals(listOf(CarBus.LIN), card.features.first().controls)
        assertEquals(listOf(CarBus.CAN1), card.features.last().reads)
        assertEquals(listOf("u1", "u2"), card.mountPoints.single().photos.map { it.url })
        assertEquals(listOf("A", "B"), card.schemes.map { it.name })
        assertNull(card.schemes.first().description)
    }

    @Test
    fun `summary keeps CAN code and treats blank image as missing`() {
        val summary = info(imageUrl = "").toSummary()
        assertEquals("2154", summary.canCode)
        assertEquals(2017..2023, summary.years)
        assertNull(summary.imageUrl)
    }

    @Test
    fun `empty group filter is no filter`() {
        assertNull(emptySet<DomainGroup>().toSdkFilter())
        assertEquals(
            listOf(CarFeatureGroup.MAIN, CarFeatureGroup.TRAILER),
            setOf(DomainGroup.TRAILER, DomainGroup.MAIN).toSdkFilter()
        )
    }
}
