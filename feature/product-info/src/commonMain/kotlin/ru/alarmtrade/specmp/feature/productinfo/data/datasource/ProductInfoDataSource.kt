package ru.alarmtrade.specmp.feature.productinfo.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageInformation
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDevicePackageInformationRequest
import ru.alarmtrade.specialist.interactors.user.UserInteractor

internal interface ProductInfoDataSource {
    suspend fun packageInformation(id: Long): SpecialistResponse<DevicePackageInformation>

    suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>>
}

internal class SpecialistProductInfoDataSource(
    private val interactor: DeviceInformationInteractor,
    private val userInteractor: UserInteractor,
) : ProductInfoDataSource {
    override suspend fun packageInformation(id: Long): SpecialistResponse<DevicePackageInformation> =
        interactor.getDevicePackageInformation.execute(GetDevicePackageInformationRequest(id))

    override suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>> =
        userInteractor.getUserAppFeaturesUseCase.execute()
}
