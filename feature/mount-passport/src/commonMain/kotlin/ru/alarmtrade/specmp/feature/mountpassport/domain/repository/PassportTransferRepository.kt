package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPdfSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportThumbnailer

/** Перенос архивом (ZIP) и напоминание о копии; сборка и разбор архива — в SDK. */
interface PassportTransferRepository {
    /** [fullBackup] — копия всего архива: успешный экспорт откладывает напоминание о копии. */
    suspend fun export(
        ids: List<String>,
        options: PassportExportOptions,
        fullBackup: Boolean,
        pdf: PassportPdfSource,
    ): Response<PassportArchive>

    suspend fun inspect(path: String): Response<PassportImportInspection>

    /** Ключи без решения — `SKIP`; не архив — `PassportNotArchiveException`. */
    suspend fun import(
        path: String,
        decisions: Map<String, PassportImportDecision>,
        thumbnailer: PassportThumbnailer,
    ): Response<PassportImportResult>

    suspend fun shouldRemindBackup(): Response<Boolean>

    suspend fun snoozeBackupReminder(): Response<Unit>
}
