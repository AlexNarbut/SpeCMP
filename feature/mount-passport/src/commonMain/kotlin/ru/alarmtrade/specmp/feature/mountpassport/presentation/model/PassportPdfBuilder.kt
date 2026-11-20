package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.PdfPageSetup
import ru.alarmtrade.specmp.core.files.PdfRenderResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vehicleTitle

/** PDF документа паспорта: HTML из SDK → PDF платформы с колонтитулом «Паспорт № …» и именем файла паспорта. */
internal class PassportPdfBuilder(
    private val documents: PassportDocumentRepository,
    private val localizer: PassportDocumentLocalizer,
    private val renderer: HtmlPdfRenderer,
    private val io: CoroutineDispatcher,
) {
    suspend fun build(passport: MountPassport): PassportPdfResult {
        val html = withContext(io) { documents.buildHtml(passport.id, localizer.strings(), localizer.format()) }
        if (html !is Response.Success) {
            return PassportPdfResult.Failed(PassportError.from((html as Response.Error).exception))
        }
        val fileName = safeFileName(localizer.fileName(passport.number, passport.fileSubject()))
        val setup = PdfPageSetup(footer = localizer.footer(passport.number))
        val rendered = renderer.render(html.value, fileName.removeSuffix(PDF_SUFFIX), setup)
        if (rendered !is PdfRenderResult.Rendered) return PassportPdfResult.Failed(PassportError.Unknown)
        return PassportPdfResult.Built(rendered.bytes, rendered.pageCount, fileName)
    }
}

internal sealed interface PassportPdfResult {
    class Built(val bytes: ByteArray, val pageCount: Int, val fileName: String) : PassportPdfResult

    class Failed(val error: PassportError) : PassportPdfResult
}

/** Вторая часть имени файла: автомобиль (пробелы → `_`) или ID блока. */
internal fun MountPassport.fileSubject(): String = vehicleTitle()?.trim()?.replace(Whitespace, "_") ?: unit.systemId

private val Whitespace = Regex("\\s+")
private const val PDF_SUFFIX = ".pdf"
