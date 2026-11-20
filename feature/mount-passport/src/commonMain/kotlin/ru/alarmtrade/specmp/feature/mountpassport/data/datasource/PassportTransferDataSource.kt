package ru.alarmtrade.specmp.feature.mountpassport.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportExport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportInspection
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportImportResult
import ru.alarmtrade.specialist.interactors.mountPassport.MountPassportInteractor
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ExportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ImportMountPassportsRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.InspectMountPassportImportRequest

internal interface PassportTransferDataSource {
    suspend fun export(request: ExportMountPassportsRequest): SpecialistResponse<MountPassportExport>

    suspend fun inspect(request: InspectMountPassportImportRequest): SpecialistResponse<MountPassportImportInspection>

    suspend fun import(request: ImportMountPassportsRequest): SpecialistResponse<MountPassportImportResult>

    suspend fun shouldRemindBackup(): SpecialistResponse<Boolean>

    suspend fun snoozeBackupReminder(): SpecialistResponse<Unit>
}

internal class SpecialistPassportTransferDataSource(private val interactor: MountPassportInteractor) :
    PassportTransferDataSource {

    override suspend fun export(request: ExportMountPassportsRequest): SpecialistResponse<MountPassportExport> =
        interactor.exportMountPassportsUseCase.execute(request)

    override suspend fun inspect(
        request: InspectMountPassportImportRequest,
    ): SpecialistResponse<MountPassportImportInspection> = interactor.inspectMountPassportImportUseCase.execute(request)

    override suspend fun import(request: ImportMountPassportsRequest): SpecialistResponse<MountPassportImportResult> =
        interactor.importMountPassportsUseCase.execute(request)

    override suspend fun shouldRemindBackup(): SpecialistResponse<Boolean> =
        interactor.shouldRemindMountPassportBackupUseCase.execute()

    override suspend fun snoozeBackupReminder(): SpecialistResponse<Unit> =
        interactor.snoozeMountPassportBackupReminderUseCase.execute()
}
