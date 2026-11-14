package ru.alarmtrade.specmp.feature.appupdate.support

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateOverview
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateRelease
import ru.alarmtrade.specmp.feature.appupdate.domain.repository.AppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

internal val TEST_PACKAGE = AppUpdatePackage("/tmp/update.dmg", "update.dmg")

internal fun release(version: String = "2.2.0", code: Int = 250) = AppUpdateRelease(
    version = version,
    versionCode = code,
    notes = "",
    publishedAt = LocalDate(2026, 11, 12),
    downloadUrl = "https://host/update.dmg",
)

internal val RELEASE_CHANNEL = AppUpdateChannel(1, "Релиз", isDefault = true, latest = null, isEmpty = false)
internal val BETA_CHANNEL = AppUpdateChannel(2, "Бета", isDefault = false, latest = null, isEmpty = false)

internal class FakeAppUpdateRepository : AppUpdateRepository {
    var check: AppUpdateCheck = AppUpdateCheck.Available(release())
    var selectedId = 1
    var chunks: Flow<Response<ByteArray>> =
        flowOf(Response.Success(byteArrayOf(1, 2)), Response.Success(byteArrayOf(3)))
    val overviewRequests = mutableListOf<Boolean>()
    val selected = mutableListOf<Int>()
    var downloads = 0

    override suspend fun overview(withChannelVersions: Boolean): Response<AppUpdateOverview> {
        overviewRequests += withChannelVersions
        return Response.Success(
            AppUpdateOverview(
                check = check,
                channels = listOf(RELEASE_CHANNEL, BETA_CHANNEL),
                selectedId = selectedId,
                settingsVersion = 1234L,
            ),
        )
    }

    override suspend fun selectChannel(id: Int): Response<Unit> {
        selected += id
        selectedId = id
        return Response.Success(Unit)
    }

    override fun download(url: String): Flow<Response<ByteArray>> {
        downloads++
        return chunks
    }
}

internal class FakeInstaller(
    override val platform: AppUpdatePlatform = AppUpdatePlatform.MACOS,
    private val accepts: Boolean = true,
    private val installResult: AppUpdateInstallResult = AppUpdateInstallResult.FAILED,
) : AppUpdateInstaller {
    var written = 0
    var exports = 0

    override val storeUrls: List<String> = listOf("market://details?id=test")

    override fun accepts(fileName: String): Boolean = accepts

    override suspend fun prepare(fileName: String): Response<AppUpdatePackage> = Response.Success(TEST_PACKAGE)

    override suspend fun append(target: AppUpdatePackage, chunk: ByteArray): Response<Unit> {
        written += chunk.size
        return Response.Success(Unit)
    }

    override suspend fun install(target: AppUpdatePackage): Response<AppUpdateInstallResult> =
        Response.Success(installResult)

    override suspend fun export(target: AppUpdatePackage): Response<Unit> {
        exports++
        return Response.Success(Unit)
    }
}
