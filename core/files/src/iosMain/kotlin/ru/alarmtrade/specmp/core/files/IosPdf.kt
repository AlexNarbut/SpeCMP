@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreGraphics.CGContextDrawPDFPage
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGDataProviderCreateWithCFData
import platform.CoreGraphics.CGDataProviderRelease
import platform.CoreGraphics.CGFontCopyPostScriptName
import platform.CoreGraphics.CGFontCreateWithDataProvider
import platform.CoreGraphics.CGFontRelease
import platform.CoreGraphics.CGPDFDocumentCreateWithProvider
import platform.CoreGraphics.CGPDFDocumentGetNumberOfPages
import platform.CoreGraphics.CGPDFDocumentGetPage
import platform.CoreGraphics.CGPDFDocumentRef
import platform.CoreGraphics.CGPDFDocumentRelease
import platform.CoreGraphics.CGPDFPageGetBoxRect
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGPDFCropBox
import platform.CoreText.CTFontManagerRegisterGraphicsFont
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSError
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.NSValue
import platform.Foundation.create
import platform.Foundation.setValue
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetPDFContextBounds
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIRectFill
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.UIKit.valueWithCGRect
import platform.UIKit.viewPrintFormatter
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSInteger
import platform.darwin.NSObject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.theme.DocumentFontFile

private val logger = Logger.withTag("Pdf")

/**
 * WKWebView вне экрана + `UIPrintPageRenderer`: лист и поля — `paperRect`/`printableRect`, колонтитул рисуется
 * в `drawFooterForPageAtIndex` (WebKit не поддерживает margin boxes `@page`).
 */
class IosHtmlPdfRenderer(private val dispatchers: AppDispatchers) : HtmlPdfRenderer {
    private val fonts = DocumentFontCache()
    private var footerFontName: String? = null

    // Граница WebKit/UIKit: любой сбой — Failed.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun render(html: String, baseName: String, page: PdfPageSetup): PdfRenderResult = try {
        val fontFiles = fonts.get()
        val source = withContext(dispatchers.default) { withHeadStyle(html, fontFaceCss(fontFiles) + BODY_RESET_CSS) }
        val (data, pages) = withContext(dispatchers.main) {
            val fontName = footerFontName ?: registerFont(fontFiles)?.also { footerFontName = it }
            printToPdf(source, page, fontName)
        }
        if (pages > 0) {
            PdfRenderResult.Rendered(withContext(dispatchers.io) { data.toByteArray() }, pages)
        } else {
            PdfRenderResult.Failed
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.w(e) { "Html to pdf failed" }
        PdfRenderResult.Failed
    }

    private suspend fun printToPdf(html: String, page: PdfPageSetup, fontName: String?): Pair<NSMutableData, Int> {
        val geometry = page.pointGeometry()
        // Вне иерархии окон WebKit не всегда раскладывает страницу для печати: держим вид в окне, но за экраном.
        val webView = WKWebView(
            frame = CGRectMake(
                -OFFSCREEN_SHIFT,
                0.0,
                geometry.contentWidth.toDouble(),
                geometry.contentHeight.toDouble(),
            ),
            configuration = WKWebViewConfiguration(),
        )
        topViewController()?.view?.window?.addSubview(webView)
        try {
            webView.loadAndWait(html)
            val renderer = FooterPageRenderer(page.footer, fontName)
            renderer.addPrintFormatter(webView.viewPrintFormatter(), startingAtPageAtIndex = 0)
            val paper = CGRectMake(0.0, 0.0, geometry.paperWidth.toDouble(), geometry.paperHeight.toDouble())
            val printable = CGRectMake(
                geometry.contentLeft.toDouble(),
                geometry.contentTop.toDouble(),
                geometry.contentWidth.toDouble(),
                (geometry.paperHeight - geometry.contentTop).toDouble(),
            )
            renderer.setValue(NSValue.valueWithCGRect(paper), forKey = "paperRect")
            renderer.setValue(NSValue.valueWithCGRect(printable), forKey = "printableRect")
            renderer.footerHeight = (geometry.paperHeight - geometry.footerTop).toDouble()
            val data = NSMutableData()
            UIGraphicsBeginPDFContextToData(data, paper, null)
            val pages = renderer.numberOfPages.toInt()
            renderer.prepareForDrawingPages(NSMakeRange(0u, pages.toULong()))
            val bounds = UIGraphicsGetPDFContextBounds()
            repeat(pages) { index ->
                UIGraphicsBeginPDFPage()
                renderer.drawPageAtIndex(index.toLong(), bounds)
            }
            UIGraphicsEndPDFContext()
            return data to pages
        } finally {
            webView.removeFromSuperview()
        }
    }
}

private class FooterPageRenderer(private val footer: PdfFooter?, private val fontName: String?) :
    UIPrintPageRenderer() {

    override fun drawFooterForPageAtIndex(pageIndex: NSInteger, inRect: CValue<CGRect>) {
        val footer = footer ?: return
        val pages = numberOfPages.toInt()
        val page = pageIndex.toInt() + 1
        val size = footer.fontSizePt.toDouble()
        val font = fontName?.let { UIFont.fontWithName(it, size) } ?: UIFont.systemFontOfSize(size)
        val attributes = mapOf<Any?, Any?>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to rgbColor(footer.textColor).toUIColor(),
        )
        inRect.useContents {
            val left = origin.x
            val right = origin.x + this.size.width
            val top = origin.y
            UIBezierPath().apply {
                moveToPoint(CGPointMake(left, top))
                addLineToPoint(CGPointMake(right, top))
                lineWidth = FOOTER_RULE_PT
            }.also { path ->
                rgbColor(footer.ruleColor).toUIColor().setStroke()
                path.stroke()
            }
            val textTop = top + FOOTER_GAP_MM.mmToPt()
            NSString.create(string = footer.startText(page, pages)).drawAtPoint(CGPointMake(left, textTop), attributes)
            val end = NSString.create(string = footer.endText(page, pages))
            val width = end.sizeWithAttributes(attributes).useContents { width }
            end.drawAtPoint(CGPointMake(right - width, textTop), attributes)
        }
    }
}

class IosPdfPageRasterizer(private val dispatchers: AppDispatchers) : PdfPageRasterizer {
    override suspend fun pageCount(bytes: ByteArray): Int = withContext(dispatchers.io) {
        withPdfDocument(bytes) { CGPDFDocumentGetNumberOfPages(it).toInt() } ?: 0
    }

    override suspend fun render(bytes: ByteArray, pageIndex: Int, widthPx: Int): ImageBitmap? {
        val png = withContext(dispatchers.io) {
            withPdfDocument(bytes) { document -> renderPng(document, pageIndex, widthPx) }
        } ?: return null
        return withContext(dispatchers.default) { png.decodeToImageBitmap() }
    }

    private fun renderPng(document: CGPDFDocumentRef, pageIndex: Int, widthPx: Int): ByteArray? {
        if (pageIndex !in 0 until CGPDFDocumentGetNumberOfPages(document).toInt() || widthPx <= 0) return null
        val page = CGPDFDocumentGetPage(document, (pageIndex + 1).toULong()) ?: return null
        val (boxWidth, boxHeight) = CGPDFPageGetBoxRect(page, kCGPDFCropBox).useContents { size.width to size.height }
        val scale = widthPx / boxWidth
        val height = (boxHeight * scale).roundToInt().toDouble()
        val format = UIGraphicsImageRendererFormat().apply {
            this.scale = 1.0
            opaque = true
        }
        val image = UIGraphicsImageRenderer(size = CGSizeMake(widthPx.toDouble(), height), format = format)
            .imageWithActions { context ->
                UIColor.whiteColor.setFill()
                UIRectFill(CGRectMake(0.0, 0.0, widthPx.toDouble(), height))
                val cg = context?.CGContext
                CGContextTranslateCTM(cg, 0.0, height)
                CGContextScaleCTM(cg, scale, -scale)
                CGContextDrawPDFPage(cg, page)
            }
        return UIImagePNGRepresentation(image)?.toByteArray()
    }
}

private inline fun <T> withPdfDocument(bytes: ByteArray, block: (CGPDFDocumentRef) -> T): T? {
    if (bytes.isEmpty()) return null
    val data = bytes.asUByteArray().usePinned { CFDataCreate(null, it.addressOf(0), bytes.size.toLong()) }
        ?: return null
    val provider = CGDataProviderCreateWithCFData(data)
    val document = CGPDFDocumentCreateWithProvider(provider)
    return try {
        document?.let(block)
    } finally {
        CGPDFDocumentRelease(document)
        CGDataProviderRelease(provider)
        CFRelease(data)
    }
}

/** Шрифт для колонтитула: UIKit рисует его сам, мимо @font-face страницы. */
private fun registerFont(fonts: List<DocumentFontFile>): String? {
    val regular = fonts.firstOrNull { it.weight == REGULAR_WEIGHT } ?: return null
    val bytes = regular.bytes.asUByteArray()
    val data = bytes.usePinned { CFDataCreate(null, it.addressOf(0), bytes.size.toLong()) } ?: return null
    val provider = CGDataProviderCreateWithCFData(data)
    val font = CGFontCreateWithDataProvider(provider)
    return try {
        font ?: return null
        CTFontManagerRegisterGraphicsFont(font, null)
        val name: CFStringRef? = CGFontCopyPostScriptName(font)
        CFBridgingRelease(name) as? String
    } finally {
        CGFontRelease(font)
        CGDataProviderRelease(provider)
        CFRelease(data)
    }
}

private suspend fun WKWebView.loadAndWait(html: String) {
    val delegate = suspendCancellableCoroutine { cont ->
        val delegate = LoadDelegate { ok -> if (cont.isActive) cont.resumeWith(Result.success(ok)) }
        navigationDelegate = delegate
        cont.invokeOnCancellation { stopLoading() }
        loadHTMLString(html, baseURL = null)
    }
    navigationDelegate = null
    check(delegate) { "WKWebView load failed" }
}

private class LoadDelegate(private val onDone: (Boolean) -> Unit) :
    NSObject(),
    WKNavigationDelegateProtocol {
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) = onDone(true)

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) = onDone(false)

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailProvisionalNavigation: WKNavigation?, withError: NSError) =
        onDone(false)
}

private fun RgbColor.toUIColor(): UIColor =
    UIColor.colorWithRed(red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = 1.0)

private const val BODY_RESET_CSS = "html, body { margin: 0; padding: 0; }"
private const val OFFSCREEN_SHIFT = 10_000.0
private const val FOOTER_RULE_PT = 0.3
private const val FOOTER_GAP_MM = 1.5f
private const val REGULAR_WEIGHT = 400
