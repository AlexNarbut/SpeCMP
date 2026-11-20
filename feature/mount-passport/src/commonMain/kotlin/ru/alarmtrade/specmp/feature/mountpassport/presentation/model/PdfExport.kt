package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.BinaryFileType

/** «Экспорт PDF»: где есть «Поделиться» (телефон, планшет) — системный лист, иначе (desktop) — сохранить файл. */
internal suspend fun BinaryFileExporter.exportPdf(fileName: String, bytes: ByteArray): PdfExportResult = if (canShare) {
    if (share(fileName, BinaryFileType.Pdf, bytes)) PdfExportResult.Done else PdfExportResult.ShareFailed
} else {
    when (val saved = save(fileName, BinaryFileType.Pdf, bytes)) {
        is BinaryFileSaveResult.Saved -> PdfExportResult.Saved(saved.name)
        BinaryFileSaveResult.Failed -> PdfExportResult.SaveFailed
        BinaryFileSaveResult.Cancelled -> PdfExportResult.Done
    }
}

internal sealed interface PdfExportResult {
    data object Done : PdfExportResult

    data class Saved(val name: String) : PdfExportResult

    data object SaveFailed : PdfExportResult

    data object ShareFailed : PdfExportResult
}
