package ru.alarmtrade.specmp.feature.appupdate.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheckState
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateDownload
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateFailure
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateRelease

/** Что показывает главная карточка экрана: стадия загрузки важнее результата проверки. */
@Immutable
internal sealed interface AppUpdateStage {
    data object Checking : AppUpdateStage

    data class Available(val release: AppUpdateRelease) : AppUpdateStage

    data class Downloading(val release: AppUpdateRelease, val bytes: Long) : AppUpdateStage

    data class Downloaded(val release: AppUpdateRelease) : AppUpdateStage

    data object Installing : AppUpdateStage

    data object UpToDate : AppUpdateStage

    data class Older(val release: AppUpdateRelease) : AppUpdateStage

    data object EmptyChannel : AppUpdateStage

    data object Offline : AppUpdateStage

    data object ServerError : AppUpdateStage

    /** [canSave] — файл скачан: его можно сохранить, а после сбоя установки — поставить снова. */
    data class DownloadFailed(val reason: AppUpdateFailureReason, val canSave: Boolean) : AppUpdateStage
}

internal enum class AppUpdateFailureReason { TRANSFER, FILE_WRITE, WRONG_FILE, INSTALL }

internal fun AppUpdateProcess.stage(): AppUpdateStage = when (val download = download) {
    is AppUpdateDownload.InProgress -> AppUpdateStage.Downloading(download.release, download.bytes)
    is AppUpdateDownload.Ready -> AppUpdateStage.Downloaded(download.release)
    is AppUpdateDownload.Installing -> AppUpdateStage.Installing
    is AppUpdateDownload.Failed -> AppUpdateStage.DownloadFailed(download.failure.reason(), download.target != null)
    AppUpdateDownload.Idle -> checkStage()
}

private fun AppUpdateProcess.checkStage(): AppUpdateStage = when (val check = check) {
    AppUpdateCheckState.Idle, AppUpdateCheckState.Checking -> AppUpdateStage.Checking

    is AppUpdateCheckState.Failed ->
        if (check.error == ApiError.NoConnection) AppUpdateStage.Offline else AppUpdateStage.ServerError

    is AppUpdateCheckState.Loaded -> when (val result = check.overview.check) {
        is AppUpdateCheck.Available -> AppUpdateStage.Available(result.release)
        AppUpdateCheck.UpToDate -> AppUpdateStage.UpToDate
        is AppUpdateCheck.Older -> AppUpdateStage.Older(result.release)
        AppUpdateCheck.EmptyChannel -> AppUpdateStage.EmptyChannel
    }
}

private fun AppUpdateFailure.reason(): AppUpdateFailureReason = when (this) {
    is AppUpdateFailure.Transfer -> AppUpdateFailureReason.TRANSFER
    AppUpdateFailure.FileWrite -> AppUpdateFailureReason.FILE_WRITE
    AppUpdateFailure.WrongFile -> AppUpdateFailureReason.WRONG_FILE
    AppUpdateFailure.Install -> AppUpdateFailureReason.INSTALL
}
