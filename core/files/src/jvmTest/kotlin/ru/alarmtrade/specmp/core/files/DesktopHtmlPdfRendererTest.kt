package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toPixelMap
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import ru.alarmtrade.specmp.core.common.AppDispatchers

class DesktopHtmlPdfRendererTest {

    private val dispatchers = AppDispatchers(io = Dispatchers.IO, default = Dispatchers.Default, main = Dispatchers.IO)
    private val setup = PdfPageSetup(
        footer = PdfFooter(start = "Паспорт монтажа № 20261119-234567-1", end = "стр. {page} из {pages}"),
    )
    private val html = checkNotNull(javaClass.getResource("/passport-sample.html")).readText()

    private val rendered: PdfRenderResult.Rendered by lazy {
        val result = runBlocking { DesktopHtmlPdfRenderer(dispatchers).render(html, "Паспорт монтажа", setup) }
        assertIs<PdfRenderResult.Rendered>(result).also { saveForInspection(it.bytes) }
    }

    @Test
    fun `passport renders to a multi page a4 pdf`() {
        Loader.loadPDF(rendered.bytes).use { pdf ->
            assertTrue(pdf.numberOfPages > 1, "pages: ${pdf.numberOfPages}")
            assertEquals(pdf.numberOfPages, rendered.pageCount)
            pdf.pages.forEach { page ->
                assertEquals(595.28f, page.mediaBox.width, 0.5f)
                assertEquals(841.89f, page.mediaBox.height, 0.5f)
            }
        }
    }

    @Test
    fun `cyrillic text is extractable and the font is embedded`() {
        Loader.loadPDF(rendered.bytes).use { pdf ->
            val text = pdf.pageText(1)
            assertTrue("Паспорт монтажа" in text, text)
            assertTrue("Ковалёва" in pdf.allText())
            val fonts = pdf.getPage(0).resources.fontNames.map { pdf.getPage(0).resources.getFont(it) }
            assertTrue(fonts.isNotEmpty() && fonts.all { it.isEmbedded }, fonts.joinToString { it.name })
            assertTrue(fonts.any { "IBMPlexSans" in it.name.replace(" ", "") }, fonts.joinToString { it.name })
        }
    }

    @Test
    fun `footer shows the document number and page n of m`() {
        Loader.loadPDF(rendered.bytes).use { pdf ->
            val pages = pdf.numberOfPages
            val second = pdf.pageText(2)
            assertTrue("стр. 2 из $pages" in second, second)
            assertTrue("Паспорт монтажа № 20261119-234567-1" in second, second)
            assertTrue("стр. 1 из $pages" in pdf.pageText(1))
        }
    }

    @Test
    fun `check table header is repeated on the next page`() {
        Loader.loadPDF(rendered.bytes).use { pdf ->
            val first = pdf.pageText(1)
            val second = pdf.pageText(2)
            assertTrue("Задание" in first && "Постановка на охрану" in first, first)
            assertFalse("GPS" in first, "table should continue on page 2")
            assertTrue("Задание" in second && "GPS" in second, second)
        }
    }

    @Test
    fun `signature block is not split between pages`() {
        Loader.loadPDF(rendered.bytes).use { pdf ->
            val signaturePages = (1..pdf.numberOfPages).filter { "Работу выполнил" in pdf.pageText(it) }
            assertEquals(1, signaturePages.size)
            val text = pdf.pageText(signaturePages.single())
            assertTrue("Работу принял" in text, text)
            assertEquals(2, Regex("подпись").findAll(text).count(), text)
        }
    }

    @Test
    fun `signature block at the page edge moves to the next page whole`() {
        val signatures = html.substring(html.indexOf("<table class=\"signatures\">"), html.indexOf("</body>"))
        val styles = html.substring(html.indexOf("<head>"), html.indexOf("</head>") + "</head>".length)
        val edge = "<html>$styles<body><div style=\"height: 243mm\">Начало</div>$signatures</body></html>"

        val result = runBlocking { DesktopHtmlPdfRenderer(dispatchers).render(edge, "edge", setup) }

        Loader.loadPDF(assertIs<PdfRenderResult.Rendered>(result).bytes).use { pdf ->
            assertEquals(2, pdf.numberOfPages)
            val second = pdf.pageText(2)
            assertTrue("Работу выполнил" in second && "Работу принял" in second, second)
            assertEquals(2, Regex("подпись").findAll(second).count(), second)
            assertFalse("Работу" in pdf.pageText(1))
        }
    }

    @Test
    fun `page is rasterized at the requested width`() {
        val rasterizer = DesktopPdfPageRasterizer(dispatchers)

        runBlocking {
            assertEquals(rendered.pageCount, rasterizer.pageCount(rendered.bytes))
            val image = assertNotNull(rasterizer.render(rendered.bytes, pageIndex = 1, widthPx = 600))
            assertEquals(600, image.width)
            assertEquals((600 * 297 / 210f).roundToInt(), image.height, "A4 proportion")
            assertEquals(Color.White, image.toPixelMap()[2, 2], "opaque white paper")
            repeat(minOf(2, rendered.pageCount)) { index ->
                val page = assertNotNull(rasterizer.render(rendered.bytes, pageIndex = index, widthPx = 1240))
                ImageIO.write(page.toAwtImage(), "png", outputDir.resolve("passport-sample-${index + 1}.png"))
            }
        }
    }

    @Test
    fun `broken pdf is not rasterized`() {
        val rasterizer = DesktopPdfPageRasterizer(dispatchers)

        runBlocking {
            assertEquals(0, rasterizer.pageCount(byteArrayOf(1, 2, 3)))
            assertEquals(null, rasterizer.render(byteArrayOf(1, 2, 3), pageIndex = 0, widthPx = 100))
            assertEquals(null, rasterizer.render(rendered.bytes, pageIndex = 99, widthPx = 100))
        }
    }

    private fun PDDocument.pageText(page: Int): String = PDFTextStripper().apply {
        startPage = page
        endPage = page
    }.getText(this)

    private fun PDDocument.allText(): String = PDFTextStripper().getText(this)

    private val outputDir = File("build/test-output").apply { mkdirs() }

    private fun saveForInspection(bytes: ByteArray) {
        outputDir.resolve("passport-sample.pdf").writeBytes(bytes)
    }
}

private fun assertEquals(expected: Float, actual: Float, tolerance: Float) {
    assertTrue(kotlin.math.abs(expected - actual) <= tolerance, "expected $expected, actual $actual")
}
