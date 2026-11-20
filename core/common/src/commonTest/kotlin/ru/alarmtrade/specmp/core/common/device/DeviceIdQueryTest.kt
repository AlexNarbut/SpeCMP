package ru.alarmtrade.specmp.core.common.device

import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceIdQueryTest {

    @Test
    fun digitsAreValid() {
        assertEquals(DeviceIdQuery.Valid(107131), DeviceIdQuery.parse("107131"))
    }

    @Test
    fun spacesAndDashesFromBarcodeAreRemoved() {
        assertEquals(DeviceIdQuery.Valid(4607123456789), DeviceIdQuery.parse(" 4 607123-456789 "))
    }

    @Test
    fun emptyAfterCleanupIsEmpty() {
        assertEquals(DeviceIdQuery.Invalid(DeviceIdQueryError.EMPTY), DeviceIdQuery.parse(" - "))
    }

    @Test
    fun lettersAreNotDigits() {
        assertEquals(DeviceIdQuery.Invalid(DeviceIdQueryError.NOT_DIGITS), DeviceIdQuery.parse("12a4"))
    }

    @Test
    fun numberBeyondLongIsTooLong() {
        assertEquals(DeviceIdQuery.Invalid(DeviceIdQueryError.TOO_LONG), DeviceIdQuery.parse("9223372036854775808"))
        assertEquals(DeviceIdQuery.Invalid(DeviceIdQueryError.TOO_LONG), DeviceIdQuery.parse("12345678901234567890"))
    }
}
