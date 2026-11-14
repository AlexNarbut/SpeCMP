package ru.alarmtrade.specmp.feature.appupdate.domain.model

import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.AppError

data class AppUpdateRelease(
    val version: String,
    val versionCode: Int,
    val notes: String,
    val publishedAt: LocalDate,
    val downloadUrl: String,
) {
    val fileName: String
        get() = downloadUrl.substringBefore('?').substringBefore('#').trimEnd('/').substringAfterLast('/')
}

/** [latest] — последняя версия канала; `null` и [isEmpty] — версий нет; `null` без [isEmpty] — не узнали. */
data class AppUpdateChannel(
    val id: Int,
    val name: String,
    val isDefault: Boolean,
    val latest: AppUpdateRelease?,
    val isEmpty: Boolean,
)

sealed interface AppUpdateCheck {
    data class Available(val release: AppUpdateRelease) : AppUpdateCheck

    data object UpToDate : AppUpdateCheck

    /** Отката нет: в канале версия старше установленной. */
    data class Older(val release: AppUpdateRelease) : AppUpdateCheck

    data object EmptyChannel : AppUpdateCheck
}

data class AppUpdateOverview(
    val check: AppUpdateCheck,
    val channels: List<AppUpdateChannel>,
    val selectedId: Int?,
    val settingsVersion: Long?,
) {
    val selected: AppUpdateChannel? get() = channels.firstOrNull { it.id == selectedId }
}

enum class AppUpdatePlatform(val installsItself: Boolean) {
    WINDOWS(installsItself = true),
    MACOS(installsItself = true),
    OTHER_DESKTOP(installsItself = true),
    ANDROID(installsItself = false),
    IOS(installsItself = false),
}

data class AppUpdatePackage(val path: String, val fileName: String)

enum class AppUpdateInstallResult { WRONG_FILE, FAILED }

sealed interface AppUpdateFailure {
    /** Сеть или сервер; `null` — причина неизвестна. */
    data class Transfer(val error: AppError?) : AppUpdateFailure

    data object WrongFile : AppUpdateFailure

    data object FileWrite : AppUpdateFailure

    data object Install : AppUpdateFailure
}

sealed interface AppUpdateCheckState {
    data object Idle : AppUpdateCheckState

    data object Checking : AppUpdateCheckState

    data class Loaded(val overview: AppUpdateOverview) : AppUpdateCheckState

    data class Failed(val error: AppError?) : AppUpdateCheckState
}

sealed interface AppUpdateDownload {
    data object Idle : AppUpdateDownload

    data class InProgress(val release: AppUpdateRelease, val bytes: Long) : AppUpdateDownload

    data class Ready(val release: AppUpdateRelease, val target: AppUpdatePackage) : AppUpdateDownload

    data class Installing(val release: AppUpdateRelease) : AppUpdateDownload

    /** [target] — скачанный файл: его можно сохранить или поставить снова. */
    data class Failed(val failure: AppUpdateFailure, val target: AppUpdatePackage? = null) : AppUpdateDownload
}

/** Сводное состояние обновления приложения: живёт дольше экрана — его видят экран и главный. */
data class AppUpdateProcess(
    val platform: AppUpdatePlatform,
    val check: AppUpdateCheckState = AppUpdateCheckState.Idle,
    val download: AppUpdateDownload = AppUpdateDownload.Idle,
) {
    val overview: AppUpdateOverview? get() = (check as? AppUpdateCheckState.Loaded)?.overview

    val available: AppUpdateRelease? get() = (overview?.check as? AppUpdateCheck.Available)?.release

    val isBusy: Boolean
        get() = check == AppUpdateCheckState.Checking ||
            download is AppUpdateDownload.InProgress ||
            download is AppUpdateDownload.Installing
}
