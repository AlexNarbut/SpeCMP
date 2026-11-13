package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhoneVisualTransformationTest {

    private fun transform(digits: String) = PhoneVisualTransformation.filter(AnnotatedString(digits))

    @Test
    fun showsMask() {
        assertEquals("+7 (900) 123-45-67", transform("79001234567").text.text)
        assertEquals("+", transform("").text.text)
    }

    @Test
    fun cursorStaysAfterTheSameDigitBothWays() {
        listOf("79001234567", "7900", "375291234567", "49301234567", "12", "").forEach { digits ->
            val transformed = transform(digits)
            val shown = transformed.text.text
            val mapping = transformed.offsetMapping
            for (offset in 0..digits.length) {
                val position = mapping.originalToTransformed(offset)
                assertTrue(position in 1..shown.length, "$digits@$offset → $position")
                assertEquals(offset, mapping.transformedToOriginal(position), "$digits@$offset")
                assertEquals(offset, shown.take(position).count(Char::isDigit), "$digits@$offset")
            }
            for (position in 0..shown.length) {
                assertTrue(mapping.transformedToOriginal(position) in 0..digits.length, "$digits#$position")
            }
        }
    }

    @Test
    fun cursorAfterDigitSkipsTrailingSeparator() {
        val mapping = transform("79001234567").offsetMapping
        assertEquals("+7 (900".length, mapping.originalToTransformed(4))
    }
}
