package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportEdit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository

internal class FakeMountPassportRepository : MountPassportRepository {
    val all = MutableStateFlow<Response<List<MountPassportSummary>>>(Response.Success(emptyList()))
    val unit = MutableStateFlow<Response<List<MountPassportSummary>>>(Response.Success(emptyList()))
    val unitRequests = mutableListOf<String>()
    var searchResult: (String) -> Response<List<MountPassportSummary>> = { Response.Success(emptyList()) }
    var searchDelayMs = 0L
    val searchCalls = mutableListOf<String>()
    val completedSearches = mutableListOf<String>()
    var storage: Response<PassportStorageUsage> = Response.Success(PassportStorageUsage(0, 0))
    var deleteResult: Response<Unit> = Response.Success(Unit)
    val deleted = mutableListOf<String>()

    var passport: Response<MountPassport> = Response.Success(testPassport())
    var passportsById: Map<String, MountPassport> = emptyMap()
    var getCalls = 0
    val edits = mutableListOf<PassportEdit>()
    var editResult: Response<MountPassport>? = null
    val addedPhotos = mutableListOf<Pair<ByteArray, ByteArray>>()
    var addPhotoResult: Response<MountPassport>? = null
    val deletedPhotos = mutableListOf<String>()
    val captions = mutableListOf<Pair<String, String?>>()
    var settingsPath: Response<String> = Response.Success("/data/p1/settings.dxs")

    override fun observeAll(): Flow<Response<List<MountPassportSummary>>> = all

    override fun observeUnit(systemId: String): Flow<Response<List<MountPassportSummary>>> {
        unitRequests += systemId
        return unit
    }

    override suspend fun search(query: String): Response<List<MountPassportSummary>> {
        searchCalls += query
        delay(searchDelayMs)
        completedSearches += query
        return searchResult(query)
    }

    override suspend fun storageUsage(): Response<PassportStorageUsage> = storage

    override suspend fun createBlank(result: PassportFastMountResult): Response<String> = Response.Success("blank")

    override suspend fun lastOfUnitToday(systemId: String): Response<MountPassportSummary?> = Response.Success(null)

    override suspend fun get(id: String): Response<MountPassport> {
        getCalls++
        return passportsById[id]?.let { Response.Success(it) } ?: passport
    }

    override suspend fun edit(id: String, edit: PassportEdit): Response<MountPassport> {
        edits += edit
        return editResult ?: passport
    }

    override suspend fun delete(id: String): Response<Unit> {
        deleted += id
        return deleteResult
    }

    override suspend fun addPhoto(id: String, jpeg: ByteArray, thumbnail: ByteArray): Response<MountPassport> {
        addedPhotos += jpeg to thumbnail
        addPhotoResult?.let { return it }
        val current = (passport as Response.Success).value
        val photo = PassportPhoto("ph${current.photos.size + 1}", "/p/${current.photos.size}.jpg", "/t/x.jpg", null)
        return Response.Success(current.copy(photos = current.photos + photo)).also { passport = it }
    }

    override suspend fun deletePhoto(id: String, photoId: String): Response<MountPassport> {
        deletedPhotos += photoId
        val current = (passport as Response.Success).value
        return Response.Success(current.copy(photos = current.photos.filterNot { it.id == photoId }))
            .also { passport = it }
    }

    override suspend fun setPhotoCaption(id: String, photoId: String, caption: String?): Response<MountPassport> {
        captions += photoId to caption
        return passport
    }

    override suspend fun settingsFile(id: String): Response<String> = settingsPath
}
