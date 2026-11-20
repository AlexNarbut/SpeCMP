package ru.alarmtrade.specmp.feature.mountpassport.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadState
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnitLookup
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.mountPassport.MountPassportInteractor
import ru.alarmtrade.specialist.interactors.mountPassport.requests.CreateMountPassportWithoutUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.FindMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportReadCommandRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ReadMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.RereadMountPassportStageRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

internal interface PassportReadingDataSource {
    fun read(request: ReadMountPassportUnitRequest): Flow<SpecialistResponse<MountPassportReadState>>

    suspend fun send(request: MountPassportReadCommandRequest): SpecialistResponse<Unit>

    suspend fun reread(request: RereadMountPassportStageRequest): SpecialistResponse<SdkMountPassport>

    suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles>

    suspend fun find(request: FindMountPassportUnitRequest): SpecialistResponse<MountPassportUnitLookup>

    /** `null` — интерактор недоступен на платформе. */
    suspend fun unitModels(): SpecialistResponse<List<String>>?

    suspend fun createWithoutUnit(request: CreateMountPassportWithoutUnitRequest): SpecialistResponse<SdkMountPassport>
}

internal class SpecialistPassportReadingDataSource(
    private val interactor: MountPassportInteractor,
    private val deviceInformation: () -> DeviceInformationInteractor,
) : PassportReadingDataSource {

    override fun read(request: ReadMountPassportUnitRequest): Flow<SpecialistResponse<MountPassportReadState>> =
        flow { emitAll(interactor.readMountPassportUnitUseCase.execute(request)) }

    override suspend fun send(request: MountPassportReadCommandRequest): SpecialistResponse<Unit> =
        interactor.sendMountPassportReadCommandUseCase.execute(request)

    override suspend fun reread(request: RereadMountPassportStageRequest): SpecialistResponse<SdkMountPassport> =
        interactor.rereadMountPassportStageUseCase.execute(request)

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> =
        interactor.getMountPassportFilesUseCase.execute(request)

    override suspend fun find(request: FindMountPassportUnitRequest): SpecialistResponse<MountPassportUnitLookup> =
        interactor.findMountPassportUnitUseCase.execute(request)

    override suspend fun unitModels(): SpecialistResponse<List<String>>? =
        deviceInformation.resolveOrNull()?.getSubsystemNamesUseCase?.execute(EmptyRequest())

    override suspend fun createWithoutUnit(
        request: CreateMountPassportWithoutUnitRequest,
    ): SpecialistResponse<SdkMountPassport> = interactor.createMountPassportWithoutUnitUseCase.execute(request)
}
