package ru.alarmtrade.specmp.core.common.phone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhoneNumbersTest {

    private val desktopRegex = Regex("""^\+([1-689]\d{7,14}|7\d{10})$""")

    @Test
    fun everyCountryAcceptsItsLengthsAndRejectsNeighbours() {
        PhoneCountries.all.forEach { country ->
            for (length in country.minLength..country.maxLength) {
                val digits = country.code + "5".repeat(length)
                assertTrue(PhoneNumbers.isValid(digits), "${country.code}: $length")
                assertTrue(desktopRegex.matches(PhoneNumbers.toE164(digits)), "desktop ${country.code}: $length")
            }
            assertFalse(PhoneNumbers.isValid(country.code + "5".repeat(country.minLength - 1)), "short ${country.code}")
            assertFalse(PhoneNumbers.isValid(country.code + "5".repeat(country.maxLength + 1)), "long ${country.code}")
        }
    }

    @Test
    fun countryCodesArePrefixFree() {
        val codes = PhoneCountries.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
        codes.forEach { code ->
            assertTrue(codes.none { it != code && it.startsWith(code) }, code)
        }
    }

    @Test
    fun masksHoldExactlyMaxLengthSlots() {
        PhoneCountries.all.filter { it.mask != null }.forEach { country ->
            assertEquals(country.maxLength, country.mask!!.count { it == PhoneNumbers.SLOT }, country.code)
        }
    }

    @Test
    fun formatsKnownCountries() {
        assertEquals("+7 (900) 123-45-67", PhoneNumbers.format("79001234567"))
        assertEquals("+375 (29) 123-45-67", PhoneNumbers.format("375291234567"))
        assertEquals("+48 512 345 678", PhoneNumbers.format("48512345678"))
        assertEquals("+33 6 12 34 56 78", PhoneNumbers.format("33612345678"))
        assertEquals("+49 301 234 567", PhoneNumbers.format("49301234567"))
    }

    @Test
    fun formatsPartialInputWithoutTrailingSeparators() {
        assertEquals("+", PhoneNumbers.format(""))
        assertEquals("+7", PhoneNumbers.format("7"))
        assertEquals("+7 (9", PhoneNumbers.format("79"))
        assertEquals("+7 (900", PhoneNumbers.format("7900"))
        assertEquals("+7 (900) 1", PhoneNumbers.format("79001"))
        assertEquals("+12", PhoneNumbers.format("12"))
    }

    @Test
    fun unknownCodesUseGroupsAndGenericLength() {
        assertEquals("+120 255 501 23", PhoneNumbers.format("12025550123"))
        assertTrue(PhoneNumbers.isValid("12025550123"))
        assertFalse(PhoneNumbers.isValid("1202555"))
        assertFalse(PhoneNumbers.isValid("0123456789"))
    }

    @Test
    fun rawValuesAreNormalized() {
        assertEquals("79001234567", PhoneNumbers.fromRaw("8 (900) 123-45-67"))
        assertEquals("79001234567", PhoneNumbers.fromRaw("+7 900 123-45-67"))
        assertEquals("89001234567".take(PhoneNumbers.MAX_DIGITS), PhoneNumbers.fromRaw("+89001234567"))
        assertEquals("79001234567", PhoneNumbers.fromRaw("7900123456789"))
    }

    @Test
    fun inputIsCappedByCountryLength() {
        assertEquals("79001234567", PhoneNumbers.fromInput("790012345678"))
        assertEquals("375291234567", PhoneNumbers.fromInput("3752912345678"))
        assertEquals(PhoneNumbers.MAX_DIGITS, PhoneNumbers.fromInput("1".repeat(20)).length)
    }
}
