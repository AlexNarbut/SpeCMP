package ru.alarmtrade.specmp.core.ui.view.layout

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CmpWindowSizeTest {

    private fun size(width: Int, height: Int) = CmpWindowSize(width.dp, height.dp)

    @Test
    fun referenceWindowsGetTheirClasses() {
        assertEquals(CmpWidthClass.Compact, size(412, 915).widthClass)
        assertEquals(CmpWidthClass.Compact, size(344, 882).widthClass)
        assertEquals(CmpWidthClass.Medium, size(690, 829).widthClass)
        assertEquals(CmpWidthClass.Medium, size(800, 1280).widthClass)
        assertEquals(CmpWidthClass.Expanded, size(915, 412).widthClass)
        assertEquals(CmpWidthClass.Large, size(1280, 800).widthClass)
    }

    @Test
    fun boundariesBelongToTheWiderClass() {
        assertEquals(CmpWidthClass.Compact, size(599, 900).widthClass)
        assertEquals(CmpWidthClass.Medium, size(600, 900).widthClass)
        assertEquals(CmpWidthClass.Expanded, size(840, 900).widthClass)
        assertEquals(CmpWidthClass.Large, size(1100, 900).widthClass)
    }

    @Test
    fun lowHeightOnlyWhenWideEnough() {
        assertTrue(size(915, 412).isLowHeight)
        assertFalse(size(915, 520).isLowHeight)
        assertFalse(size(412, 400).isLowHeight)
    }

    @Test
    fun atLeastFollowsOrder() {
        assertTrue(size(900, 800).atLeast(CmpWidthClass.Medium))
        assertFalse(size(700, 800).atLeast(CmpWidthClass.Expanded))
    }
}
