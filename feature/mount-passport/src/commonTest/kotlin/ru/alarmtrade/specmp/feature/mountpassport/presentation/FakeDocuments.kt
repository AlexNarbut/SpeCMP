package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.BinaryFileOpenResult
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.BinaryFileType
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.PdfFooter
import ru.alarmtrade.specmp.core.files.PdfPageRasterizer
import ru.alarmtrade.specmp.core.files.PdfPageSetup
import ru.alarmtrade.specmp.core.files.PdfRenderResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentText
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportDocumentLocalizer

internal class FakePassportDocumentRepository(private val passports: FakeMountPassportRepository) :
    PassportDocumentRepository {
    var html: Response<String> = Response.Success("<html>p1</html>")
    val builds = mutableListOf<Pair<PassportDocumentStrings, PassportDocumentFormat>>()
    var issuedCalls = 0

    override suspend fun buildHtml(
        id: String,
        strings: PassportDocumentStrings,
        format: PassportDocumentFormat,
    ): Response<String> {
        builds += strings to format
        return html
    }

    override suspend fun markIssued(id: String): Response<MountPassport> {
        issuedCalls++
        val current = (passports.passport as Response.Success).value
        val issued = current.copy(status = PassportStatus.ISSUED, updatedAt = ISSUED_AT)
        passports.passport = Response.Success(issued)
        return Response.Success(issued)
    }
}

internal class FakeHtmlPdfRenderer : HtmlPdfRenderer {
    var result: PdfRenderResult = PdfRenderResult.Rendered(byteArrayOf(1, 2, 3), pageCount = 3)
    var delayMs = 0L
    val renders = mutableListOf<Triple<String, String, PdfPageSetup>>()

    override suspend fun render(html: String, baseName: String, page: PdfPageSetup): PdfRenderResult {
        renders += Triple(html, baseName, page)
        delay(delayMs)
        return result
    }
}

internal class FakePdfPageRasterizer : PdfPageRasterizer {
    val renders = mutableListOf<Pair<Int, Int>>()

    override suspend fun pageCount(bytes: ByteArray): Int = 3

    override suspend fun render(bytes: ByteArray, pageIndex: Int, widthPx: Int): ImageBitmap? {
        renders += pageIndex to widthPx
        return ImageBitmap(1, 1)
    }
}

internal class FakeBinaryFileExporter(override val canShare: Boolean = true) : BinaryFileExporter {
    var shareResult = true
    var saveResult: BinaryFileSaveResult = BinaryFileSaveResult.Saved("Паспорт.pdf")
    var openResult: BinaryFileOpenResult = BinaryFileOpenResult.Opened
    val shared = mutableListOf<Pair<String, ByteArray>>()
    val saved = mutableListOf<String>()
    val opened = mutableListOf<String>()

    override suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult {
        saved += fileName
        return saveResult
    }

    override suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean {
        shared += fileName to bytes
        return shareResult
    }

    override suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult {
        opened += fileName
        return openResult
    }
}

internal class FakePassportDocumentLocalizer : PassportDocumentLocalizer {
    val strings = PassportDocumentStrings(mapOf(PassportDocumentText.TITLE to "Паспорт монтажа"))
    val format = PassportDocumentFormat("ru", List(12) { "m$it" }, "{d} {MMMM} {yyyy}", " ", null)

    override suspend fun strings(): PassportDocumentStrings = strings

    override suspend fun format(): PassportDocumentFormat = format

    override suspend fun footer(number: String): PdfFooter = PdfFooter("Паспорт № $number", "стр. {page} из {pages}")

    override suspend fun fileName(number: String, subject: String): String = "Паспорт_${number}_$subject.pdf"
}

internal val ISSUED_AT: Instant = Instant.fromEpochMilliseconds(1_763_560_000_000L)
