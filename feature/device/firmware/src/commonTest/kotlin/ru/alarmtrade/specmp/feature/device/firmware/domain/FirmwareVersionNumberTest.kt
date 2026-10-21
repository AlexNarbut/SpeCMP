package ru.alarmtrade.specmp.feature.device.firmware.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionNumber
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo

class FirmwareVersionNumberTest {

    private fun v(raw: String) = FirmwareVersionNumber.parse(raw)

    @Test
    fun `numbers are compared as numbers not as strings`() {
        // Строки: "3.9" > "3.10" — так сравнивало старое приложение.
        assertTrue(v("3.9") < v("3.10"))
        assertTrue(v("3.61") > v("3.57"))
        assertTrue(v("10.0") > v("9.99"))
    }

    @Test
    fun `VER prefix, spaces and trailing zeros do not matter`() {
        assertEquals(v("3.57"), v("VER3.57"))
        assertEquals(v("3.57"), v(" VER 3.57 "))
        assertEquals(v("3.57"), v("3.57.0"))
        assertEquals(v("3.57").hashCode(), v("3.57.0").hashCode())
    }

    @Test
    fun `release is newer than prerelease and labels are ordered`() {
        assertTrue(v("3.62") > v("3.62-beta.2"))
        assertTrue(v("3.62-beta.2") > v("3.62-alpha.5"))
        assertTrue(v("3.62-beta.3") > v("3.62-beta.2"))
        // Номер ядра главнее суффикса.
        assertTrue(v("3.63-alpha.1") > v("3.62"))
    }

    @Test
    fun `text without numbers falls back to text comparison`() {
        assertEquals(v("unknown"), v("unknown"))
        assertTrue(v("b") > v("a"))
    }

    @Test
    fun `relation to the installed version`() {
        assertEquals(VersionRelation.Newer, "3.61".relationTo("VER3.57"))
        assertEquals(VersionRelation.Same, "3.57".relationTo("VER3.57"))
        assertEquals(VersionRelation.Older, "3.55".relationTo("3.57"))
        assertEquals(VersionRelation.Older, "3.9".relationTo("3.10"))
    }

    @Test
    fun `archive order is newest first`() {
        val sorted = listOf("3.9", "3.10", "3.2", "3.57", "3.57-beta.1").map(::v).sortedDescending()
        assertEquals(listOf("3.57", "3.57-beta.1", "3.10", "3.9", "3.2"), sorted.map { it.toString() })
    }
}
