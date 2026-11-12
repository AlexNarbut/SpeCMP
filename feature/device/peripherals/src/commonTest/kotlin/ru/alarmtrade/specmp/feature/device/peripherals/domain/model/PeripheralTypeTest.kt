package ru.alarmtrade.specmp.feature.device.peripherals.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PeripheralTypeTest {
    @Test
    fun `catalog has sixteen types ending with rf module`() {
        assertEquals(16, SystemPeripheralTypes.size)
        assertEquals(PeripheralType.RF_MODULE, SystemPeripheralTypes.last())
    }

    @Test
    fun `old bluetooth module is not asked about newer types`() {
        val types = systemPeripheralTypesFor("VER4.12")

        assertTrue(PeripheralType.TPMS in types)
        assertFalse(PeripheralType.FUEL_SENSOR in types)
        assertFalse(PeripheralType.PIR_SENSOR in types)
    }

    @Test
    fun `minor version is a number of hundredths`() {
        assertFalse(PeripheralType.VS in systemPeripheralTypesFor("4.1"))
        assertTrue(PeripheralType.VS in systemPeripheralTypesFor("4.10"))
    }

    @Test
    fun `unknown version polls everything`() {
        assertEquals(SystemPeripheralTypes, systemPeripheralTypesFor(null))
        assertEquals(SystemPeripheralTypes, systemPeripheralTypesFor("boot"))
        assertEquals(SystemPeripheralTypes, systemPeripheralTypesFor("1.14"))
    }
}
