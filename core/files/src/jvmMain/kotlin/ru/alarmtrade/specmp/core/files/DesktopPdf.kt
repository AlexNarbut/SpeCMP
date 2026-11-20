package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import co.touchlab.kermit.Logger
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import ru.alarmtrade.specmp.core.common.AppDispatchers

private val logger = Logger.withTag("Pdf")

class DesktopHtmlPdfRenderer(private val dispatchers: AppDispatchers) : HtmlPdfRenderer {
    private val fonts = DocumentFontCache()

    // Граница движка вёрстки: любой сбой разбора или рендера — Failed.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun render(html: String, baseName: String, page: PdfPageSetup): PdfRenderResult =
        withContext(dispatchers.io) {
            try {
                val fontFiles = fonts.get()
                val source = withHeadStyle(html, pagedMediaCss(page) + TABLE_PAGINATION_CSS)
                val document = W3CDom().fromJsoup(Jsoup.parse(source))
                val output = ByteArrayOutputStream()
                PdfRendererBuilder().apply {
                    useFastMode()
                    withW3cDocument(document, null)
                    fontFiles.forEach { font ->
                        useFont({ ByteArrayInputStream(font.bytes) }, font.family, font.weight, FontStyle.NORMAL, true)
                    }
                    withProducer(baseName)
                    toStream(output)
                }.run()
                val bytes = output.toByteArray()
                PdfRenderResult.Rendered(bytes, Loader.loadPDF(bytes).use { it.numberOfPages })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.w(e) { "Html to pdf failed" }
                PdfRenderResult.Failed
            }
        }
}

class DesktopPdfPageRasterizer(private val dispatchers: AppDispatchers) : PdfPageRasterizer {
    override suspend fun pageCount(bytes: ByteArray): Int = withContext(dispatchers.io) {
        guardedPdf { Loader.loadPDF(bytes).use { it.numberOfPages } } ?: 0
    }

    override suspend fun render(bytes: ByteArray, pageIndex: Int, widthPx: Int): ImageBitmap? =
        withContext(dispatchers.io) {
            guardedPdf {
                Loader.loadPDF(bytes).use { pdf ->
                    if (pageIndex !in 0 until pdf.numberOfPages || widthPx <= 0) return@use null
                    val box = pdf.getPage(pageIndex).cropBox
                    val scale = widthPx / box.width
                    val image = BufferedImage(widthPx, (box.height * scale).roundToInt(), BufferedImage.TYPE_INT_RGB)
                    val graphics = image.createGraphics()
                    try {
                        // PDFBox очищает лист цветом фона Graphics2D (по умолчанию чёрным), а не рисует бумагу.
                        graphics.background = Color.WHITE
                        graphics.clearRect(0, 0, image.width, image.height)
                        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        PDFRenderer(pdf).renderPageToGraphics(pageIndex, graphics, scale)
                    } finally {
                        graphics.dispose()
                    }
                    image.toComposeImageBitmap()
                }
            }
        }
}

// Граница PDFBox: битый PDF — null.
@Suppress("TooGenericExceptionCaught")
private inline fun <T> guardedPdf(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    logger.w(e) { "Pdf read failed" }
    null
}

/** openhtmltopdf повторяет `thead` на новом листе только с этим своим свойством. */
private const val TABLE_PAGINATION_CSS = " table { -fs-table-paginate: paginate; }"
