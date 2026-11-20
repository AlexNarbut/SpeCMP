package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlinx.coroutines.CancellationException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPdfSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportThumbnailer
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportTransferRepository

internal val TEST_ARCHIVE = PassportArchive("/tmp/export/Паспорт_1.zip", "Паспорт_1.zip", 3_145_728, 1)

/** Как SDK: PDF каждого паспорта — по очереди через [PassportPdfSource], сбой PDF — ошибка экспорта. */
internal class FakePassportTransferRepository : PassportTransferRepository {
    class Export(val ids: List<String>, val options: PassportExportOptions, val fullBackup: Boolean)

    class Import(val path: String, val decisions: Map<String, PassportImportDecision>)

    val exports = mutableListOf<Export>()
    val pdfs = mutableListOf<ByteArray>()
    var afterPdf: () -> Unit = {}
    var exportResult: Response<PassportArchive> = Response.Success(TEST_ARCHIVE)
    val inspected = mutableListOf<String>()
    var inspection: Response<PassportImportInspection> = Response.Success(PassportImportInspection.NotArchive)
    val imports = mutableListOf<Import>()
    var thumbnailJpeg: ByteArray? = null
    val thumbnails = mutableListOf<ByteArray>()
    var importResult: Response<PassportImportResult> = Response.Success(PassportImportResult(1, 0, 0, emptyList()))
    var remind: Response<Boolean> = Response.Success(false)
    var snoozes = 0

    override suspend fun export(
        ids: List<String>,
        options: PassportExportOptions,
        fullBackup: Boolean,
        pdf: PassportPdfSource,
    ): Response<PassportArchive> {
        exports += Export(ids, options, fullBackup)
        ids.forEach { id ->
            try {
                pdfs += pdf.pdf(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return Response.Error(e)
            }
            afterPdf()
        }
        return exportResult
    }

    override suspend fun inspect(path: String): Response<PassportImportInspection> {
        inspected += path
        return inspection
    }

    override suspend fun import(
        path: String,
        decisions: Map<String, PassportImportDecision>,
        thumbnailer: PassportThumbnailer,
    ): Response<PassportImportResult> {
        imports += Import(path, decisions)
        thumbnailJpeg?.let { thumbnails += thumbnailer.thumbnail(it) }
        return importResult
    }

    override suspend fun shouldRemindBackup(): Response<Boolean> = remind

    override suspend fun snoozeBackupReminder(): Response<Unit> {
        snoozes++
        return Response.Success(Unit)
    }
}
