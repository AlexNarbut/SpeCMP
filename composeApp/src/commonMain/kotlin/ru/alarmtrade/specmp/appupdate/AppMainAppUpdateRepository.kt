package ru.alarmtrade.specmp.appupdate

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheckState
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateDownload
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateService
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus
import ru.alarmtrade.specmp.feature.main.domain.repository.MainAppUpdateRepository

/** Главный видит обновление через свой порт; ведёт его `AppUpdateService`, общий с экраном обновления. */
internal class AppMainAppUpdateRepository(private val service: AppUpdateService, private val settings: Settings) :
    MainAppUpdateRepository {

    override fun status(): Flow<Response<MainAppUpdateStatus>> = service.process.map { Response.Success(it.toMain()) }

    override suspend fun check(): Response<Unit> {
        service.check(withChannelVersions = service.process.value.platform.installsItself)
        return Response.Success(Unit)
    }

    override suspend fun download(): Response<Unit> {
        service.download(anyFile = false)
        return Response.Success(Unit)
    }

    override suspend fun dismissedBannerVersion(): Response<String?> =
        safeRun { settings.getStringOrNull(KEY_DISMISSED_BANNER) }

    override suspend fun dismissBanner(version: String): Response<Unit> =
        safeRun { settings.putString(KEY_DISMISSED_BANNER, version) }

    private fun AppUpdateProcess.toMain(): MainAppUpdateStatus {
        val update = when (val download = download) {
            is AppUpdateDownload.InProgress -> MainAppUpdate.Downloading(download.release.version, download.bytes)

            is AppUpdateDownload.Ready -> MainAppUpdate.Downloaded(download.release.version)

            is AppUpdateDownload.Installing -> MainAppUpdate.Installing

            is AppUpdateDownload.Failed, AppUpdateDownload.Idle ->
                if (check == AppUpdateCheckState.Checking) {
                    MainAppUpdate.Checking
                } else {
                    available?.let { MainAppUpdate.Available(it.version) } ?: MainAppUpdate.None
                }
        }
        return MainAppUpdateStatus(installsItself = platform.installsItself, update = update)
    }

    private companion object {
        const val KEY_DISMISSED_BANNER = "dismissed_banner_version"
    }
}
