package ru.alarmtrade.specmp.feature.mountpassport.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportHtmlDocument
import ru.alarmtrade.specialist.interactors.mountPassport.MountPassportInteractor
import ru.alarmtrade.specialist.interactors.mountPassport.requests.BuildMountPassportHtmlRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportStatusRequest

internal interface PassportDocumentDataSource {
    suspend fun buildHtml(request: BuildMountPassportHtmlRequest): SpecialistResponse<MountPassportHtmlDocument>

    suspend fun setStatus(request: SetMountPassportStatusRequest): SpecialistResponse<SdkMountPassport>

    suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles>
}

internal class SpecialistPassportDocumentDataSource(private val interactor: MountPassportInteractor) :
    PassportDocumentDataSource {

    override suspend fun buildHtml(
        request: BuildMountPassportHtmlRequest,
    ): SpecialistResponse<MountPassportHtmlDocument> = interactor.buildMountPassportHtmlUseCase.execute(request)

    override suspend fun setStatus(request: SetMountPassportStatusRequest): SpecialistResponse<SdkMountPassport> =
        interactor.setMountPassportStatusUseCase.execute(request)

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> =
        interactor.getMountPassportFilesUseCase.execute(request)
}
