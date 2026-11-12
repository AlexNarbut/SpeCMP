package ru.alarmtrade.specmp.feature.device.peripherals.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheralTypes

class PeripheralMappersTest {
    private val unknownOnPurpose = setOf(DeviceType.VEHICLE_CHARGER, DeviceType.MICROBOT, DeviceType.UNKNOWN)

    @Test
    fun `every sdk peripheral type has its own type`() {
        val unmapped = DeviceType.entries.filter {
            it !in unknownOnPurpose &&
                it.toPeripheralType() == PeripheralType.UNKNOWN
        }

        assertEquals(emptyList(), unmapped)
    }

    @Test
    fun `every catalog type is polled and maps back to itself`() {
        SystemPeripheralTypes.forEach { type ->
            val sdkTypes = type.pollTypes()
            assertTrue(sdkTypes.isNotEmpty(), "$type")
            sdkTypes.forEach { assertEquals(type, it.toPeripheralType()) }
        }
    }
}
