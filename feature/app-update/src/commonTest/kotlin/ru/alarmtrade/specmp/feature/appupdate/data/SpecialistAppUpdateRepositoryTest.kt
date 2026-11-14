package ru.alarmtrade.specmp.feature.appupdate.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdateType
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesChannel
import ru.alarmtrade.specialist.domain.appUpdates.UpdateChannelAccessLevel
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.domain.errors.UpdateVersionNotFoundException
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.data.datasource.AppUpdateDataSource
import ru.alarmtrade.specmp.feature.appupdate.data.repository.SpecialistAppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateOverview

class SpecialistAppUpdateRepositoryTest {

    private val source = FakeAppUpdateDataSource()
    private val repository = SpecialistAppUpdateRepository(source) { AppVersion("2.1.0", CURRENT_CODE) }

    @Test
    fun newerVersionInSavedChannelIsAvailable() = runTest {
        source.channels = listOf(channel(1, PUBLIC), channel(2, LIMITED))
        source.savedId = 2
        source.latest[2] = SpecialistResponse.Success(sdkVersion(" 2.2.0 ", 250))

        val overview = overview(withChannelVersions = false)
        val release = assertIs<AppUpdateCheck.Available>(overview.check).release

        assertEquals("2.2.0", release.version)
        assertEquals(LocalDate(2026, 11, 12), release.publishedAt)
        assertEquals("Pandora Spec-2.2.0.dmg", release.fileName)
        assertEquals(2, overview.selectedId)
        assertEquals(listOf(2), source.latestRequests)
        assertEquals(1234L, overview.settingsVersion)
    }

    @Test
    fun withoutSavedChannelFirstPublicIsSelectedAndSaved() = runTest {
        source.channels = listOf(channel(3, LIMITED), channel(4, PUBLIC))
        source.savedId = 99
        source.latest[4] = SpecialistResponse.Success(sdkVersion("2.1.0", CURRENT_CODE))

        val overview = overview(withChannelVersions = false)

        assertEquals(4, overview.selectedId)
        assertEquals(listOf(4), source.selected)
        assertEquals(AppUpdateCheck.UpToDate, overview.check)
    }

    @Test
    fun olderVersionAndEmptyChannelAreReported() = runTest {
        source.channels = listOf(channel(1, PUBLIC), channel(2, LIMITED))
        source.savedId = 1
        source.latest[1] = SpecialistResponse.Success(sdkVersion("2.0.48", 248))
        source.latest[2] = SpecialistResponse.Error.General(UpdateVersionNotFoundException("Update was not found"))

        val overview = overview(withChannelVersions = true)

        assertIs<AppUpdateCheck.Older>(overview.check)
        val beta = overview.channels.single { it.id == 2 }
        assertTrue(beta.isEmpty)
        assertNull(beta.latest)
        assertEquals("2.0.48", overview.channels.single { it.id == 1 }.latest?.version)

        source.savedId = 2
        assertEquals(AppUpdateCheck.EmptyChannel, overview(withChannelVersions = false).check)
    }

    @Test
    fun networkErrorOfSelectedChannelIsError() = runTest {
        source.channels = listOf(channel(1, PUBLIC))
        source.latest[1] = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        assertIs<Response.Error>(repository.overview(withChannelVersions = false))
    }

    @Test
    fun downloadErrorIsResponseError() = runTest {
        source.download = flow {
            emit(SpecialistResponse.Success(byteArrayOf(1)))
            error("broken stream")
        }

        val steps = repository.download("https://host/file.dmg").toList()

        assertIs<Response.Success<ByteArray>>(steps.first())
        assertIs<Response.Error>(steps.last())
    }

    private suspend fun overview(withChannelVersions: Boolean): AppUpdateOverview =
        assertIs<Response.Success<AppUpdateOverview>>(repository.overview(withChannelVersions)).value

    private fun sdkVersion(version: String, versionInt: Int) = UpdateVersionInformation(
        id = 1,
        version = version,
        versionInt = versionInt,
        description = "- Исправления\n",
        updateChannelId = 1,
        downloadUrl = "https://host/files/Pandora Spec-${version.trim()}.dmg?x=1",
        publishedDate = LocalDateTime(2026, 11, 12, 10, 30),
    )

    private fun channel(id: Int, access: UpdateChannelAccessLevel) =
        AppUpdatesChannel(id, "Канал $id", AppUpdateType.App(DeviceType.MACOS), access)

    private companion object {
        const val CURRENT_CODE = 249
        val PUBLIC = UpdateChannelAccessLevel.PUBLIC
        val LIMITED = UpdateChannelAccessLevel.LIMITED
    }
}

private class FakeAppUpdateDataSource : AppUpdateDataSource {
    var channels: List<AppUpdatesChannel> = emptyList()
    var savedId: Int = 0
    val latest = mutableMapOf<Int, SpecialistResponse<UpdateVersionInformation>>()
    val latestRequests = mutableListOf<Int>()
    val selected = mutableListOf<Int>()
    var download: Flow<SpecialistResponse<ByteArray>> = flowOf()

    override suspend fun channels(): SpecialistResponse<List<AppUpdatesChannel>> = SpecialistResponse.Success(channels)

    override suspend fun selectedChannelId(): SpecialistResponse<Int> = SpecialistResponse.Success(savedId)

    override suspend fun selectChannel(id: Int): SpecialistResponse<Unit> {
        selected += id
        return SpecialistResponse.Success(Unit)
    }

    override suspend fun latestVersion(channelId: Int): SpecialistResponse<UpdateVersionInformation> {
        latestRequests += channelId
        return latest.getValue(channelId)
    }

    override suspend fun settingsVersion(): SpecialistResponse<LongWrapper> =
        SpecialistResponse.Success(LongWrapper(1234L))

    override fun download(url: String): Flow<SpecialistResponse<ByteArray>> = download
}
