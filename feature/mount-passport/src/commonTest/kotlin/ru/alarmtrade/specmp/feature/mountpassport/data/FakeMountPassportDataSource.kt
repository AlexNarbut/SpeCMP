package ru.alarmtrade.specmp.feature.mountpassport.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.mountPassport.MountPassport as SdkMountPassport
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFiles
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportListItem
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStorageUsage
import ru.alarmtrade.specialist.interactors.mountPassport.requests.AddMountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.EditMountPassportRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSearchRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSystemIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportPhotoCaptionRequest
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.MountPassportDataSource

internal class FakeMountPassportDataSource : MountPassportDataSource {
    val all = MutableSharedFlow<SpecialistResponse<List<MountPassportListItem>>>(replay = 1)
    val unit = MutableSharedFlow<SpecialistResponse<List<MountPassportListItem>>>(replay = 1)
    var unitRequest: String? = null
    var searchQuery: String? = null
    var searchResponse: SpecialistResponse<List<MountPassportListItem>> = SpecialistResponse.Success(emptyList())
    var storage: SpecialistResponse<MountPassportStorageUsage> =
        SpecialistResponse.Success(MountPassportStorageUsage(0, 0))
    var passport: SpecialistResponse<SdkMountPassport> = SpecialistResponse.Success(sdkPassport())
    var filesResponse: SpecialistResponse<MountPassportFiles> = SpecialistResponse.Success(sdkFiles())
    var editRequest: EditMountPassportRequest? = null
    var deleted: String? = null
    var photoRequest: AddMountPassportPhotoRequest? = null
    var photoResponse: SpecialistResponse<SdkMountPassport>? = null
    var deletedPhoto: MountPassportPhotoRequest? = null
    var caption: SetMountPassportPhotoCaptionRequest? = null

    override fun observeAll(): Flow<SpecialistResponse<List<MountPassportListItem>>> = all

    override fun observeUnit(
        request: MountPassportSystemIdRequest,
    ): Flow<SpecialistResponse<List<MountPassportListItem>>> {
        unitRequest = request.systemId
        return unit
    }

    override suspend fun search(request: MountPassportSearchRequest): SpecialistResponse<List<MountPassportListItem>> {
        searchQuery = request.query
        return searchResponse
    }

    override suspend fun storageUsage(): SpecialistResponse<MountPassportStorageUsage> = storage

    override suspend fun get(request: MountPassportIdRequest): SpecialistResponse<SdkMountPassport> = passport

    override suspend fun files(request: MountPassportIdRequest): SpecialistResponse<MountPassportFiles> = filesResponse

    override suspend fun edit(request: EditMountPassportRequest): SpecialistResponse<SdkMountPassport> {
        editRequest = request
        return passport
    }

    override suspend fun delete(request: MountPassportIdRequest): SpecialistResponse<Unit> {
        deleted = request.id
        return SpecialistResponse.Success(Unit)
    }

    override suspend fun addPhoto(request: AddMountPassportPhotoRequest): SpecialistResponse<SdkMountPassport> {
        photoRequest = request
        return photoResponse ?: passport
    }

    override suspend fun deletePhoto(request: MountPassportPhotoRequest): SpecialistResponse<SdkMountPassport> {
        deletedPhoto = request
        return passport
    }

    override suspend fun setPhotoCaption(
        request: SetMountPassportPhotoCaptionRequest,
    ): SpecialistResponse<SdkMountPassport> {
        caption = request
        return passport
    }
}
