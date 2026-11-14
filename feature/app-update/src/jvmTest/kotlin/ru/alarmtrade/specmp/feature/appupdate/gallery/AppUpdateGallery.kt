package ru.alarmtrade.specmp.feature.appupdate.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheck
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateCheckState
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateDownload
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateFailure
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateOverview
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateRelease
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntryPoint
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateActions
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateDialog
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateScreen
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

/** Состояния экрана обновления по макету `design/features/app-update/`; PNG — в `build/state-gallery/app-update`. */
class AppUpdateGallery {

    private val outDir = File("build/state-gallery/app-update")
    private val actions = AppUpdateActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    private val notes = "Новое\n- Обновление приложения из канала «Бета»\n- Каналы обновлений с версиями\n\n" +
        "Исправления\n- Подключение по Bluetooth после сна\n- Поиск автомобиля по VIN\n- Перевод экрана прошивки\n" +
        "- Сохранение настроек в файл\n- Мелкие исправления интерфейса"
    private val release = AppUpdateRelease("2.2.0", 250, notes, LocalDate(2026, 11, 10), "https://host/u.dmg")
    private val older = release.copy(version = "2.0.48", versionCode = 248, publishedAt = LocalDate(2026, 11, 2))
    private val target = AppUpdatePackage("/tmp/u.dmg", "u.dmg")
    private val channels = listOf(
        AppUpdateChannel(1, "Релиз", isDefault = true, latest = older, isEmpty = false),
        AppUpdateChannel(2, "Бета", isDefault = false, latest = release, isEmpty = false),
        AppUpdateChannel(
            3,
            "Тестирование новой прошивки CAN-модулей",
            isDefault = false,
            latest = null,
            isEmpty = true,
        ),
    )

    private fun process(
        check: AppUpdateCheck = AppUpdateCheck.Available(release),
        download: AppUpdateDownload = AppUpdateDownload.Idle,
        platform: AppUpdatePlatform = AppUpdatePlatform.WINDOWS,
        selectedId: Int = 2,
    ) = AppUpdateProcess(
        platform = platform,
        check = AppUpdateCheckState.Loaded(AppUpdateOverview(check, channels, selectedId, settingsVersion = 1234L)),
        download = download,
    )

    private fun state(process: AppUpdateProcess, dialog: AppUpdateDialog? = null, expanded: Boolean = false) =
        AppUpdateState(
            installedVersion = "2.1.0",
            installedCode = 249,
            from = AppUpdateEntryPoint.PROMPT,
            process = process,
            notesExpanded = expanded,
            dialog = dialog,
        )

    private fun render(name: String, state: AppUpdateState) = StateGallery.render(outDir, name) {
        AppUpdateScreen(state = state, actions = actions, snackbar = CmpSnackbarHostState())
    }

    @Test
    fun checking() = render(
        "01-checking",
        state(AppUpdateProcess(AppUpdatePlatform.WINDOWS, check = AppUpdateCheckState.Checking)),
    )

    @Test
    fun available() = render("02-available", state(process()))

    @Test
    fun availableNotesExpanded() = render("03-available-notes", state(process(), expanded = true))

    @Test
    fun downloading() = render(
        "04-downloading",
        state(process(download = AppUpdateDownload.InProgress(release, 47_500_000))),
    )

    @Test
    fun downloadedMac() = render(
        "05-downloaded-mac",
        state(process(download = AppUpdateDownload.Ready(release, target), platform = AppUpdatePlatform.MACOS)),
    )

    @Test
    fun installing() = render("06-installing", state(process(download = AppUpdateDownload.Installing(release))))

    @Test
    fun older() = render("07-older", state(process(check = AppUpdateCheck.Older(older), selectedId = 1)))

    @Test
    fun emptyChannel() = render("08-empty", state(process(check = AppUpdateCheck.EmptyChannel, selectedId = 3)))

    @Test
    fun offline() = render(
        "09-offline",
        state(AppUpdateProcess(AppUpdatePlatform.WINDOWS, check = AppUpdateCheckState.Failed(ApiError.NoConnection))),
    )

    @Test
    fun downloadFailed() = render(
        "10-download-failed",
        state(process(download = AppUpdateDownload.Failed(AppUpdateFailure.Transfer(ApiError.NoConnection)))),
    )

    @Test
    fun wrongFile() = render(
        "11-wrong-file",
        state(process(download = AppUpdateDownload.Failed(AppUpdateFailure.WrongFile))),
    )

    @Test
    fun deviceBusyDialog() = render(
        "12-dialog-busy",
        state(process(download = AppUpdateDownload.Ready(release, target)), dialog = AppUpdateDialog.DeviceBusy),
    )

    @Test
    fun betaDialog() = render(
        "13-dialog-beta",
        state(process(selectedId = 1), dialog = AppUpdateDialog.SwitchChannel(channels[1], "Релиз")),
    )

    @Test
    fun mobileAvailable() = render(
        "14-mobile-available",
        state(process(platform = AppUpdatePlatform.ANDROID)),
    )

    @Test
    fun uptodate() = render("15-uptodate", state(process(check = AppUpdateCheck.UpToDate)))
}
