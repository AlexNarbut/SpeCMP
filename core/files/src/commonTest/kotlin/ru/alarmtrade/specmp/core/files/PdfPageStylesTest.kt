package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.core.ui.theme.DocumentFontFile

class PdfPageStylesTest {

    private val footer = PdfFooter(start = "Паспорт монтажа № 1", end = "стр. {page} из {pages}")
    private val setup = PdfPageSetup(footer = footer)

    @Test
    fun `footer template substitutes page and page count`() {
        assertEquals("стр. 2 из 5", footer.endText(page = 2, pages = 5))
        assertEquals("Паспорт монтажа № 1", footer.startText(page = 2, pages = 5))
    }

    @Test
    fun `footer template without placeholders stays as is`() {
        assertEquals("page", PdfFooter(start = "", end = "page").endText(page = 1, pages = 1))
    }

    @Test
    fun `default page is a4 portrait with 15 mm margins and room for the footer`() {
        val page = PdfPageSetup()

        assertEquals(210f, page.widthMm)
        assertEquals(297f, page.heightMm)
        assertEquals(PdfMargins(top = 15f, end = 15f, bottom = 23f, start = 15f), page.margins)
    }

    @Test
    fun `paged media css sets size margins and footer counters`() {
        val css = pagedMediaCss(setup)

        assertTrue("size: 210mm 297mm" in css, css)
        assertTrue("margin: 15mm 15mm 23mm 15mm" in css, css)
        assertTrue("@bottom-left" in css && "@bottom-right" in css, css)
        assertTrue("content: \"стр. \" counter(page) \" из \" counter(pages)" in css, css)
        assertTrue("content: \"Паспорт монтажа № 1\"" in css, css)
    }

    @Test
    fun `paged media css without footer has no margin boxes`() {
        assertFalse("@bottom" in pagedMediaCss(PdfPageSetup()))
    }

    @Test
    fun `quotes backslashes and line breaks in footer text are escaped for css`() {
        assertEquals("\"a\\\"b\\\\c d\"", cssStringContent("a\"b\\c\nd"))
        assertEquals("\"№ \" counter(page)", cssStringContent("№ {page}"))
        assertEquals("counter(pages)", cssStringContent("{pages}"))
    }

    @Test
    fun `font faces are inlined as base64 data urls`() {
        val css = fontFaceCss(listOf(DocumentFontFile("IBM Plex Sans", 500, byteArrayOf(1, 2, 3))))

        assertTrue("font-family: 'IBM Plex Sans'" in css, css)
        assertTrue("font-weight: 500" in css, css)
        assertTrue("src: url(data:font/ttf;base64,AQID)" in css, css)
    }

    @Test
    fun `style is injected before the closing head tag`() {
        val html = "<html><head><title>x</title></head><body>b</body></html>"

        assertEquals(
            "<html><head><title>x</title><style>p{}</style></head><body>b</body></html>",
            withHeadStyle(html, "p{}"),
        )
    }

    @Test
    fun `style is injected into html without head`() {
        assertEquals("<style>p{}</style><p>b</p>", withHeadStyle("<p>b</p>", "p{}"))
        assertEquals(
            "<HTML><HEAD><style>p{}</style></HEAD></HTML>",
            withHeadStyle("<HTML><HEAD></HEAD></HTML>", "p{}"),
        )
    }

    @Test
    fun `point geometry of a4 matches the css page`() {
        val geometry = setup.pointGeometry()

        assertEquals(595.28f, geometry.paperWidth, 0.01f)
        assertEquals(841.89f, geometry.paperHeight, 0.01f)
        assertEquals(42.52f, geometry.contentLeft, 0.01f)
        assertEquals(42.52f, geometry.contentTop, 0.01f)
        assertEquals(510.24f, geometry.contentWidth, 0.01f)
        assertEquals(841.89f - 42.52f - 65.2f, geometry.contentHeight, 0.01f)
        assertEquals(geometry.contentTop + geometry.contentHeight, geometry.footerTop, 0.01f)
    }

    @Test
    fun `footer colors are parsed from css hex for native drawing`() {
        assertEquals(RgbColor(0x33 / 255f, 0x33 / 255f, 0x33 / 255f), rgbColor("#333333"))
        assertEquals(RgbColor(1f, 0f, 0f), rgbColor("#f00"))
        assertEquals(RgbColor(0f, 0f, 0f), rgbColor("not a color"))
    }

    @Test
    fun `pdf file name is a safe basename with pdf extension`() {
        assertEquals("Паспорт 1.pdf", pdfFileName("Паспорт 1"))
        assertEquals("a.pdf", pdfFileName("a.PDF").lowercase())
        assertFalse('/' in pdfFileName("x/../y"))
    }
}

private fun assertEquals(expected: Float, actual: Float, tolerance: Float) {
    assertTrue(kotlin.math.abs(expected - actual) <= tolerance, "expected $expected, actual $actual")
}
