package ru.alarmtrade.specmp.feature.mountpassport.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportListItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStorageUsage
import ru.alarmtrade.specialist.interactors.mountPassport.MountPassportInteractor
import ru.alarmtrade.specialist.interactors.mountPassport.requests.AddMountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.CreateMountPassportRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.EditMountPassportRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSearchRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSystemIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportPhotoCaptionRequest

internal interface MountPassportDataSource {
    fun observeAll(): Flow<SpecialistResponse<List<MountPassportListItem>>>

    fun observeUnit(request: MountPassportSystemIdRequest): Flow<SpecialistResponse<List<MountPassportListItem>>>

    suspend fun search(request: MountPassportSearchRequest): SpecialistResponse<List<MountPassportListItem>>

    suspend fun storageUsage(): SpecialistResponse<MountPassportStorageUsage>

    suspend fun create(request: CreateMountPassportRequest): SpecialistResponse<SdkMountPassport>

    suspend fun lastOfUnitToday(request: MountPassportSystemIdRequest): SpecialistResponse<MountPassportListItem?>

    suspend fun get(request: MountPassportIdRequest): SpecialistResponse<SdkMountPassport>

    suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles>

    suspend fun edit(request: EditMountPassportRequest): SpecialistResponse<SdkMountPassport>

    suspend fun delete(request: MountPassportIdRequest): SpecialistResponse<Unit>

    suspend fun addPhoto(request: AddMountPassportPhotoRequest): SpecialistResponse<SdkMountPassport>

    suspend fun deletePhoto(request: MountPassportPhotoRequest): SpecialistResponse<SdkMountPassport>

    suspend fun setPhotoCaption(request: SetMountPassportPhotoCaptionRequest): SpecialistResponse<SdkMountPassport>

    suspend fun missingPhotos(request: MountPassportIdRequest): SpecialistResponse<List<String>>
}

internal class SpecialistMountPassportDataSource(private val interactor: MountPassportInteractor) :
    MountPassportDataSource {

    override fun observeAll(): Flow<SpecialistResponse<List<MountPassportListItem>>> =
        flow { emitAll(interactor.observeMountPassportsUseCase.execute()) }

    override fun observeUnit(
        request: MountPassportSystemIdRequest,
    ): Flow<SpecialistResponse<List<MountPassportListItem>>> =
        flow { emitAll(interactor.observeUnitMountPassportsUseCase.execute(request)) }

    override suspend fun search(request: MountPassportSearchRequest): SpecialistResponse<List<MountPassportListItem>> =
        interactor.searchMountPassportsUseCase.execute(request)

    override suspend fun storageUsage(): SpecialistResponse<MountPassportStorageUsage> =
        interactor.getMountPassportStorageUsageUseCase.execute()

    override suspend fun create(request: CreateMountPassportRequest): SpecialistResponse<SdkMountPassport> =
        interactor.createMountPassportUseCase.execute(request)

    override suspend fun lastOfUnitToday(
        request: MountPassportSystemIdRequest,
    ): SpecialistResponse<MountPassportListItem?> = interactor.getLastUnitMountPassportTodayUseCase.execute(request)

    override suspend fun get(request: MountPassportIdRequest): SpecialistResponse<SdkMountPassport> =
        interactor.getMountPassportUseCase.execute(request)

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> =
        interactor.getMountPassportFilesUseCase.execute(request)

    override suspend fun edit(request: EditMountPassportRequest): SpecialistResponse<SdkMountPassport> =
        interactor.editMountPassportUseCase.execute(request)

    override suspend fun delete(request: MountPassportIdRequest): SpecialistResponse<Unit> =
        interactor.deleteMountPassportUseCase.execute(request)

    override suspend fun addPhoto(request: AddMountPassportPhotoRequest): SpecialistResponse<SdkMountPassport> =
        interactor.addMountPassportPhotoUseCase.execute(request)

    override suspend fun deletePhoto(request: MountPassportPhotoRequest): SpecialistResponse<SdkMountPassport> =
        interactor.deleteMountPassportPhotoUseCase.execute(request)

    override suspend fun setPhotoCaption(
        request: SetMountPassportPhotoCaptionRequest,
    ): SpecialistResponse<SdkMountPassport> = interactor.setMountPassportPhotoCaptionUseCase.execute(request)

    override suspend fun missingPhotos(request: MountPassportIdRequest): SpecialistResponse<List<String>> =
        interactor.getMountPassportMissingPhotosUseCase.execute(request)
}
