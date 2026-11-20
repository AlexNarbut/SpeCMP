package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadState
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnitLookup
import ru.alarmtrade.specialist.interactors.mountPassport.requests.CreateMountPassportWithoutUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.FindMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportReadCommandRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ReadMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.RereadMountPassportStageRequest
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportReadingDataSource

internal class FakePassportReadingDataSource : PassportReadingDataSource {
    val states = MutableSharedFlow<SpecialistResponse<MountPassportReadState>>(replay = 1)
    var readRequest: ReadMountPassportUnitRequest? = null
    var command: MountPassportReadCommandRequest? = null
    var rereadRequest: RereadMountPassportStageRequest? = null
    var rereadResponse: SpecialistResponse<SdkMountPassport> = SpecialistResponse.Success(sdkPassport())
    var findRequest: FindMountPassportUnitRequest? = null
    var findResponse: SpecialistResponse<MountPassportUnitLookup> =
        SpecialistResponse.Success(MountPassportUnitLookup.NotFound)
    var models: SpecialistResponse<List<String>>? = SpecialistResponse.Success(emptyList())
    var createRequest: CreateMountPassportWithoutUnitRequest? = null

    override fun read(request: ReadMountPassportUnitRequest): Flow<SpecialistResponse<MountPassportReadState>> {
        readRequest = request
        return states
    }

    override suspend fun send(request: MountPassportReadCommandRequest): SpecialistResponse<Unit> {
        command = request
        return SpecialistResponse.Success(Unit)
    }

    override suspend fun reread(request: RereadMountPassportStageRequest): SpecialistResponse<SdkMountPassport> {
        rereadRequest = request
        return rereadResponse
    }

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> =
        SpecialistResponse.Success(sdkFiles())

    override suspend fun find(request: FindMountPassportUnitRequest): SpecialistResponse<MountPassportUnitLookup> {
        findRequest = request
        return findResponse
    }

    override suspend fun unitModels(): SpecialistResponse<List<String>>? = models

    override suspend fun createWithoutUnit(
        request: CreateMountPassportWithoutUnitRequest,
    ): SpecialistResponse<SdkMountPassport> {
        createRequest = request
        return SpecialistResponse.Success(sdkPassport())
    }
}
