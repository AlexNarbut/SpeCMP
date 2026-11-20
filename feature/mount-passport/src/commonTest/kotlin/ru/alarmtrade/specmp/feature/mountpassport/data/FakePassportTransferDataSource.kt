package ru.alarmtrade.specmp.feature.mountpassport.data

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportExport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportInspection
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportResult
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ExportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ImportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.InspectMountPassportImportRequest
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportTransferDataSource

internal class FakePassportTransferDataSource : PassportTransferDataSource {
    var exportRequest: ExportMountPassportsRequest? = null
    var exportResponse: SpecialistResponse<MountPassportExport> =
        SpecialistResponse.Success(MountPassportExport("/tmp/export/a.zip", "a.zip", 2048, 1))
    var inspectRequest: InspectMountPassportImportRequest? = null
    var inspection: SpecialistResponse<MountPassportImportInspection> =
        SpecialistResponse.Success(MountPassportImportInspection.NotPassportArchive)
    var importRequest: ImportMountPassportsRequest? = null
    var importResponse: SpecialistResponse<MountPassportImportResult> =
        SpecialistResponse.Success(MountPassportImportResult(emptyList(), emptyList(), 0, emptyList()))
    var remind: SpecialistResponse<Boolean> = SpecialistResponse.Success(true)
    var snoozes = 0

    override suspend fun export(request: ExportMountPassportsRequest): SpecialistResponse<MountPassportExport> {
        exportRequest = request
        return exportResponse
    }

    override suspend fun inspect(
        request: InspectMountPassportImportRequest,
    ): SpecialistResponse<MountPassportImportInspection> {
        inspectRequest = request
        return inspection
    }

    override suspend fun import(request: ImportMountPassportsRequest): SpecialistResponse<MountPassportImportResult> {
        importRequest = request
        return importResponse
    }

    override suspend fun shouldRemindBackup(): SpecialistResponse<Boolean> = remind

    override suspend fun snoozeBackupReminder(): SpecialistResponse<Unit> {
        snoozes++
        return SpecialistResponse.Success(Unit)
    }
}
