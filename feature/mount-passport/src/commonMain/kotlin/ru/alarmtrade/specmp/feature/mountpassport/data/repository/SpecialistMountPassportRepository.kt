package ru.alarmtrade.specmp.feature.mountpassport.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.MountPassportPhotoLimitException
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportListItem
import ru.alarmtrade.specialist.interactors.mountPassport.requests.AddMountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.EditMountPassportRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportPhotoRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSearchRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportSystemIdRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.SetMountPassportPhotoCaptionRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.MountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhotoLimitException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageFullException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository

internal class SpecialistMountPassportRepository(private val dataSource: MountPassportDataSource) :
    MountPassportRepository {

    override fun observeAll(): Flow<Response<List<MountPassportSummary>>> =
        dataSource.observeAll().map { it.toSummaries() }

    override fun observeUnit(systemId: String): Flow<Response<List<MountPassportSummary>>> =
        dataSource.observeUnit(MountPassportSystemIdRequest(systemId)).map { it.toSummaries() }

    override suspend fun search(query: String): Response<List<MountPassportSummary>> =
        dataSource.search(MountPassportSearchRequest(query)).toSummaries()

    override suspend fun storageUsage(): Response<PassportStorageUsage> =
        dataSource.storageUsage().toResponse().map { it.toDomain() }

    override suspend fun get(id: String): Response<MountPassport> =
        dataSource.get(MountPassportIdRequest(id)).withFiles(dataSource::files)

    override suspend fun edit(id: String, edit: PassportEdit): Response<MountPassport> =
        dataSource.edit(EditMountPassportRequest(id, edit.toSdk())).withFiles(dataSource::files)

    override suspend fun delete(id: String): Response<Unit> = dataSource.delete(MountPassportIdRequest(id)).toResponse()

    override suspend fun addPhoto(id: String, jpeg: ByteArray, thumbnail: ByteArray): Response<MountPassport> =
        dataSource.addPhoto(AddMountPassportPhotoRequest(id, jpeg, thumbnail, caption = null))
            .withFiles(dataSource::files)
            .mapIfError { Response.Error(it.exception.toPhotoError(), it.message) }

    override suspend fun deletePhoto(id: String, photoId: String): Response<MountPassport> =
        dataSource.deletePhoto(MountPassportPhotoRequest(id, photoId)).withFiles(dataSource::files)

    override suspend fun setPhotoCaption(id: String, photoId: String, caption: String?): Response<MountPassport> =
        dataSource.setPhotoCaption(
            SetMountPassportPhotoCaptionRequest(id, photoId, caption),
        ).withFiles(dataSource::files)

    override suspend fun settingsFile(id: String): Response<String> =
        dataSource.files(MountPassportIdRequest(id)).toResponse().map { it.settingsDxs }
}

private fun SpecialistResponse<List<MountPassportListItem>>.toSummaries(): Response<List<MountPassportSummary>> =
    toResponse().map { items -> items.map { it.toDomain() } }

/** SDK не различает нехватку места: признак — текст системной ошибки записи (`ENOSPC`) в цепочке причин. */
private fun Throwable.toPhotoError(): Throwable = when {
    this is MountPassportPhotoLimitException -> PassportPhotoLimitException(limit, this)
    causes().any { it.message.orEmpty().containsNoSpace() } -> PassportStorageFullException(this)
    else -> this
}

private fun Throwable.causes(): Sequence<Throwable> = generateSequence(this) { it.cause.takeIf { next -> next !== it } }

private fun String.containsNoSpace(): Boolean = NO_SPACE_MARKERS.any { contains(it, ignoreCase = true) }

private val NO_SPACE_MARKERS = listOf("ENOSPC", "No space left", "not enough space", "disk full")
