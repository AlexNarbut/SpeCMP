package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateStage
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.stage
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntryPoint

/** [from] — откуда открыт экран; [install] — сразу установить скачанное (кнопка «Установить» на главном). */
@Immutable
internal data class AppUpdateArgs(val from: AppUpdateEntryPoint, val install: Boolean = false)

@Immutable
internal data class AppUpdateState(
    val installedVersion: String,
    val installedCode: Int,
    val from: AppUpdateEntryPoint,
    val process: AppUpdateProcess,
    val notesExpanded: Boolean = false,
    val dialog: AppUpdateDialog? = null,
) {
    val stage: AppUpdateStage get() = process.stage()

    val channels: List<AppUpdateChannel> get() = process.overview?.channels.orEmpty()

    val selectedChannel: AppUpdateChannel? get() = process.overview?.selected

    val showChannels: Boolean get() = process.platform.installsItself && channels.size > 1

    val channelsLocked: Boolean get() = stage is AppUpdateStage.Downloading || stage is AppUpdateStage.Installing

    val showLater: Boolean get() = from != AppUpdateEntryPoint.MENU
}

@Immutable
internal sealed interface AppUpdateDialog {
    data object DeviceBusy : AppUpdateDialog

    data class SwitchChannel(val channel: AppUpdateChannel, val defaultName: String) : AppUpdateDialog
}
