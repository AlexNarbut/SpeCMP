package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.graphics.ImageBitmap

/**
 * HTML → PDF. Размер листа, поля и колонтитул задаёт [PdfPageSetup], а не `@page` документа: iOS рисует колонтитул
 * сам (WebKit не умеет margin boxes). Вёрстка документа — CSS 2.1 (таблицы, float), без flex/grid: их не знает desktop.
 */
interface HtmlPdfRenderer {
    suspend fun render(html: String, baseName: String, page: PdfPageSetup = PdfPageSetup()): PdfRenderResult
}

sealed interface PdfRenderResult {
    class Rendered(val bytes: ByteArray, val pageCount: Int) : PdfRenderResult

    data object Failed : PdfRenderResult
}

data class PdfPageSetup(
    val widthMm: Float = A4_WIDTH_MM,
    val heightMm: Float = A4_HEIGHT_MM,
    val margins: PdfMargins = PdfMargins(),
    val footer: PdfFooter? = null,
)

/** [bottom] включает полосу колонтитула: он печатается сразу под текстом, в нижнем поле. */
data class PdfMargins(
    val top: Float = DEFAULT_MARGIN_MM,
    val end: Float = DEFAULT_MARGIN_MM,
    val bottom: Float = DEFAULT_BOTTOM_MARGIN_MM,
    val start: Float = DEFAULT_MARGIN_MM,
)

/** Тексты слева и справа; `{page}` и `{pages}` — номер листа и число листов. */
data class PdfFooter(
    val start: String,
    val end: String,
    val fontSizePt: Float = FOOTER_FONT_SIZE_PT,
    val textColor: String = "#333333",
    val ruleColor: String = "#999999",
) {
    fun startText(page: Int, pages: Int): String = start.withPageNumbers(page, pages)

    fun endText(page: Int, pages: Int): String = end.withPageNumbers(page, pages)
}

/** Предпросмотр PDF картинками; битый PDF или лист вне диапазона — `0` / `null`. */
interface PdfPageRasterizer {
    suspend fun pageCount(bytes: ByteArray): Int

    suspend fun render(bytes: ByteArray, pageIndex: Int, widthPx: Int): ImageBitmap?
}

internal fun String.withPageNumbers(page: Int, pages: Int): String =
    replace(PAGE_PLACEHOLDER, page.toString()).replace(PAGES_PLACEHOLDER, pages.toString())

internal const val PAGE_PLACEHOLDER = "{page}"
internal const val PAGES_PLACEHOLDER = "{pages}"

private const val A4_WIDTH_MM = 210f
private const val A4_HEIGHT_MM = 297f
private const val DEFAULT_MARGIN_MM = 15f
private const val DEFAULT_BOTTOM_MARGIN_MM = 23f
private const val FOOTER_FONT_SIZE_PT = 7.5f
