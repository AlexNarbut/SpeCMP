package ru.alarmtrade.specmp.core.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.writePdf
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Logger
import java.io.File
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers

private val logger = Logger.withTag("Pdf")

/**
 * Невидимый WebView печатает HTML в PDF (поля и колонтитул — CSS `@page`; margin boxes — с WebView 131).
 * Контекст — только application: порт живёт во ViewModel.
 */
class AndroidHtmlPdfRenderer(context: Context, private val dispatchers: AppDispatchers) : HtmlPdfRenderer {
    private val appContext = context.applicationContext
    private val fonts = DocumentFontCache()

    // Граница WebView и файлов: любой сбой — Failed.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun render(html: String, baseName: String, page: PdfPageSetup): PdfRenderResult {
        val directory = File(appContext.cacheDir, "$RENDER_DIR/${UUID.randomUUID()}")
        return try {
            val source = withContext(dispatchers.default) {
                withHeadStyle(html, fontFaceCss(fonts.get()) + pagedMediaCss(page))
            }
            val target = withContext(dispatchers.io) { File(directory.apply { mkdirs() }, pdfFileName(baseName)) }
            val written = withContext(dispatchers.main) { printToFile(source, baseName, page, target) }
            if (!written) return PdfRenderResult.Failed
            withContext(dispatchers.io) {
                val bytes = target.readBytes()
                PdfRenderResult.Rendered(bytes, target.pdfPageCount())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Html to pdf failed" }
            PdfRenderResult.Failed
        } finally {
            withContext(dispatchers.io) { directory.deleteRecursively() }
        }
    }

    private suspend fun printToFile(html: String, baseName: String, page: PdfPageSetup, target: File): Boolean {
        val webView = WebView(appContext)
        return try {
            webView.settings.javaScriptEnabled = false
            webView.loadAndWait(html)
            webView.createPrintDocumentAdapter(baseName).writePdf(page.printAttributes(), target)
        } finally {
            webView.destroy()
        }
    }
}

class AndroidPdfPageRasterizer(context: Context, private val dispatchers: AppDispatchers) : PdfPageRasterizer {
    private val appContext = context.applicationContext
    private val mutex = Mutex()

    override suspend fun pageCount(bytes: ByteArray): Int = withPdf(bytes) { it.pageCount } ?: 0

    override suspend fun render(bytes: ByteArray, pageIndex: Int, widthPx: Int): ImageBitmap? = withPdf(bytes) { pdf ->
        if (pageIndex !in 0 until pdf.pageCount || widthPx <= 0) return@withPdf null
        pdf.openPage(pageIndex).use { page ->
            val height = (widthPx.toFloat() * page.height / page.width).roundToInt()
            val bitmap = Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap.asImageBitmap()
        }
    }

    // PdfRenderer читает только файл и не потокобезопасен; битый PDF — null.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> withPdf(bytes: ByteArray, block: (PdfRenderer) -> T): T? = mutex.withLock {
        withContext(dispatchers.io) {
            val file = File(appContext.cacheDir, "$RASTER_DIR/${UUID.randomUUID()}.pdf")
            try {
                file.parentFile?.mkdirs()
                file.writeBytes(bytes)
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use(block)
                }
            } catch (e: Exception) {
                logger.w(e) { "Pdf read failed" }
                null
            } finally {
                file.delete()
            }
        }
    }
}

private suspend fun WebView.loadAndWait(html: String) = suspendCancellableCoroutine { cont ->
    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
            if (cont.isActive) cont.resumeWith(Result.success(Unit))
        }
    }
    cont.invokeOnCancellation { stopLoading() }
    loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

private fun File.pdfPageCount(): Int = ParcelFileDescriptor.open(this, ParcelFileDescriptor.MODE_READ_ONLY)
    .use { descriptor -> PdfRenderer(descriptor).use { it.pageCount } }

/** Системные поля нулевые: поля листа задаёт CSS `@page`. */
private fun PdfPageSetup.printAttributes(): PrintAttributes = PrintAttributes.Builder()
    .setMediaSize(PrintAttributes.MediaSize(MEDIA_ID, MEDIA_ID, widthMm.toMils(), heightMm.toMils()))
    .setResolution(PrintAttributes.Resolution(MEDIA_ID, MEDIA_ID, PRINT_DPI, PRINT_DPI))
    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
    .build()

private fun Float.toMils(): Int = (this / MM_PER_INCH * MILS_PER_INCH).roundToInt()

private const val RENDER_DIR = "pdf-render"
private const val RASTER_DIR = "pdf-raster"
private const val MEDIA_ID = "specmp"
private const val PRINT_DPI = 300
private const val MM_PER_INCH = 25.4f
private const val MILS_PER_INCH = 1000f
