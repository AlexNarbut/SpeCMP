package ru.alarmtrade.specmp.feature.mountpassport.data.repository

import ru.alarmtrade.specialist.domain.errors.MountPassportArchiveException
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportExportOptions
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportPdfProvider
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportThumbnailer
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ExportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ImportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.InspectMountPassportImportRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportTransferDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportNotArchiveException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPdfSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportThumbnailer
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportTransferRepository

internal class SpecialistPassportTransferRepository(private val dataSource: PassportTransferDataSource) :
    PassportTransferRepository {

    override suspend fun export(
        ids: List<String>,
        options: PassportExportOptions,
        fullBackup: Boolean,
        pdf: PassportPdfSource,
    ): Response<PassportArchive> = dataSource.export(
        ExportMountPassportsRequest(
            ids = ids,
            pdf = MountPassportPdfProvider { passport -> pdf.pdf(passport.id) },
            options = MountPassportExportOptions(options.includeSettings, options.includePhotos),
            fullBackup = fullBackup,
        ),
    ).toResponse().map { it.toDomain() }

    override suspend fun inspect(path: String): Response<PassportImportInspection> =
        dataSource.inspect(InspectMountPassportImportRequest(path)).toResponse().map { it.toDomain() }

    override suspend fun import(
        path: String,
        decisions: Map<String, PassportImportDecision>,
        thumbnailer: PassportThumbnailer,
    ): Response<PassportImportResult> = dataSource.import(
        ImportMountPassportsRequest(
            path = path,
            decisions = decisions.mapValues { (_, decision) -> decision.toSdk() },
            thumbnailer = MountPassportThumbnailer(thumbnailer::thumbnail),
        ),
    ).toResponse()
        .map { it.toDomain() }
        .mapIfError { error ->
            val exception = error.exception
            if (exception is MountPassportArchiveException) {
                Response.Error(PassportNotArchiveException(exception))
            } else {
                error
            }
        }

    override suspend fun shouldRemindBackup(): Response<Boolean> = dataSource.shouldRemindBackup().toResponse()

    override suspend fun snoozeBackupReminder(): Response<Unit> = dataSource.snoozeBackupReminder().toResponse()
}
