package ru.alarmtrade.specmp.core.files

import kotlin.io.encoding.Base64
import ru.alarmtrade.specmp.core.ui.theme.DOCUMENT_FONT_FAMILY
import ru.alarmtrade.specmp.core.ui.theme.DocumentFontFile

/** `@page` с полями и колонтитулом в margin boxes — для движков с CSS Paged Media (openhtmltopdf, Chromium 131+). */
internal fun pagedMediaCss(setup: PdfPageSetup): String = buildString {
    val m = setup.margins
    append("@page { size: ${setup.widthMm.css()}mm ${setup.heightMm.css()}mm; ")
    append("margin: ${m.top.css()}mm ${m.end.css()}mm ${m.bottom.css()}mm ${m.start.css()}mm; ")
    setup.footer?.let { footer ->
        append(footerBoxCss("bottom-left", "left", footer.start, footer))
        append(footerBoxCss("bottom-right", "right", footer.end, footer))
    }
    append("} html, body { margin: 0; padding: 0; }")
}

private fun footerBoxCss(box: String, align: String, text: String, footer: PdfFooter): String =
    "@$box { content: ${cssStringContent(text)}; width: 50%; text-align: $align; vertical-align: top; " +
        "font-family: '$DOCUMENT_FONT_FAMILY'; font-weight: 400; font-size: ${footer.fontSizePt.css()}pt; " +
        "color: ${footer.textColor}; border-top: 0.3pt solid ${footer.ruleColor}; padding-top: 1.5mm; } "

/** Текст колонтитула → значение `content`: строки CSS вперемешку со счётчиками листов. */
internal fun cssStringContent(text: String): String = PLACEHOLDER_REGEX.split(text)
    .zip(PLACEHOLDER_REGEX.findAll(text).map { it.value }.plus("").toList())
    .flatMap { (literal, placeholder) ->
        listOfNotNull(
            literal.takeIf { it.isNotEmpty() }?.let(::cssString),
            when (placeholder) {
                PAGE_PLACEHOLDER -> "counter(page)"
                PAGES_PLACEHOLDER -> "counter(pages)"
                else -> null
            },
        )
    }
    .ifEmpty { listOf("\"\"") }
    .joinToString(" ")

private fun cssString(text: String): String {
    val escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace(Regex("[\\r\\n]+"), " ")
    return "\"$escaped\""
}

internal fun fontFaceCss(fonts: List<DocumentFontFile>): String = fonts.joinToString(" ") { font ->
    "@font-face { font-family: '${font.family}'; font-weight: ${font.weight}; font-style: normal; " +
        "src: url(data:font/ttf;base64,${Base64.Default.encode(font.bytes)}) format('truetype'); }"
}

internal fun withHeadStyle(html: String, css: String): String {
    val style = "<style>$css</style>"
    val headEnd = html.indexOf("</head>", ignoreCase = true)
    return if (headEnd >= 0) html.substring(0, headEnd) + style + html.substring(headEnd) else style + html
}

internal class PdfPointGeometry(
    val paperWidth: Float,
    val paperHeight: Float,
    val contentLeft: Float,
    val contentTop: Float,
    val contentWidth: Float,
    val contentHeight: Float,
    val footerTop: Float,
)

/** Те же лист и поля в пунктах — для печати через UIKit, где `@page` не работает. */
internal fun PdfPageSetup.pointGeometry(): PdfPointGeometry {
    val width = widthMm.pt()
    val height = heightMm.pt()
    val contentHeight = height - margins.top.pt() - margins.bottom.pt()
    return PdfPointGeometry(
        paperWidth = width,
        paperHeight = height,
        contentLeft = margins.start.pt(),
        contentTop = margins.top.pt(),
        contentWidth = width - margins.start.pt() - margins.end.pt(),
        contentHeight = contentHeight,
        footerTop = margins.top.pt() + contentHeight,
    )
}

internal data class RgbColor(val red: Float, val green: Float, val blue: Float)

/** `#rgb` / `#rrggbb`; иное — чёрный. */
internal fun rgbColor(hex: String): RgbColor {
    val raw = hex.removePrefix("#")
    val digits = if (raw.length == SHORT_HEX) raw.map { "$it$it" }.joinToString("") else raw
    val value = digits.takeIf { it.length == LONG_HEX }?.toIntOrNull(radix = HEX_RADIX) ?: return RgbColor(0f, 0f, 0f)
    return RgbColor(value.channel(RED_SHIFT), value.channel(GREEN_SHIFT), value.channel(0))
}

private fun Int.channel(shift: Int): Float = (this shr shift and CHANNEL_MAX) / CHANNEL_MAX.toFloat()

internal fun pdfFileName(baseName: String): String = fileNameWithExtension(baseName, PDF_EXTENSION)

internal fun Float.mmToPt(): Float = this * POINTS_PER_INCH / MM_PER_INCH

private fun Float.pt(): Float = mmToPt()

private fun Float.css(): String = if (this % 1f == 0f) toInt().toString() else toString()

private const val SHORT_HEX = 3
private const val LONG_HEX = 6
private const val HEX_RADIX = 16
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MAX = 0xFF
private val PLACEHOLDER_REGEX = Regex("\\{pages?\\}")
private const val PDF_EXTENSION = "pdf"
private const val POINTS_PER_INCH = 72f
private const val MM_PER_INCH = 25.4f
