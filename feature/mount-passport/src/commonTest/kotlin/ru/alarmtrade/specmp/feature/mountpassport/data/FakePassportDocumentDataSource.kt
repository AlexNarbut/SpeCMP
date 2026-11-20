package ru.alarmtrade.specmp.feature.mountpassport.data

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.interactors.mountPassport.requests.BuildMountPassportHtmlRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportStatusRequest
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportDocumentDataSource

internal class FakePassportDocumentDataSource : PassportDocumentDataSource {
    var htmlRequest: BuildMountPassportHtmlRequest? = null
    var statusRequest: SetMountPassportStatusRequest? = null

    override suspend fun buildHtml(request: BuildMountPassportHtmlRequest): SpecialistResponse<String> {
        htmlRequest = request
        return SpecialistResponse.Success("<html/>")
    }

    override suspend fun setStatus(request: SetMountPassportStatusRequest): SpecialistResponse<SdkMountPassport> {
        statusRequest = request
        return SpecialistResponse.Success(sdkPassport().copy(status = request.status))
    }

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> =
        SpecialistResponse.Success(sdkFiles())
}
