package ru.alarmtrade.specmp.feature.productinfo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQuery
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQueryError

class ProductQueryTest {

    @Test
    fun digitsAreValid() {
        assertEquals(ProductQuery.Valid(107131), ProductQuery.parse("107131"))
    }

    @Test
    fun spacesAndDashesFromBarcodeAreRemoved() {
        assertEquals(ProductQuery.Valid(4607123456789), ProductQuery.parse(" 4 607123-456789 "))
    }

    @Test
    fun emptyAfterCleanupIsEmpty() {
        assertEquals(ProductQuery.Invalid(ProductQueryError.EMPTY), ProductQuery.parse(" - "))
    }

    @Test
    fun lettersAreNotDigits() {
        assertEquals(ProductQuery.Invalid(ProductQueryError.NOT_DIGITS), ProductQuery.parse("12a4"))
    }

    @Test
    fun numberBeyondLongIsTooLong() {
        assertEquals(ProductQuery.Invalid(ProductQueryError.TOO_LONG), ProductQuery.parse("9223372036854775808"))
        assertEquals(ProductQuery.Invalid(ProductQueryError.TOO_LONG), ProductQuery.parse("12345678901234567890"))
    }
}
