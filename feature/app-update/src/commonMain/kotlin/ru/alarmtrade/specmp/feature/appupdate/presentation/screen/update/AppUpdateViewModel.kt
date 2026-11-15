package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import kotlinx.collections.immutable.toImmutableList
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateDeviceActivity
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateService
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateStage
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntryPoint

/** Состояние обновления живёт в [AppUpdateService] (загрузка идёт и вне экрана); VM — его проекция и диалоги. */
internal class AppUpdateViewModel(
    private val args: AppUpdateArgs,
    private val service: AppUpdateService,
    private val installer: AppUpdateInstaller,
    private val deviceActivity: AppUpdateDeviceActivity,
    versionProvider: AppVersionProvider,
) : BaseViewModel<AppUpdateState, AppUpdateEffect>(
    AppUpdateState(
        installedVersion = versionProvider.current().name,
        installedCode = versionProvider.current().code,
        from = args.from,
        process = service.process.value,
    ),
) {
    private val withChannelVersions = installer.platform.installsItself

    override suspend fun IntentScope.onStart() {
        service.process.collectIntoState { copy(process = it) }
        when (args.from) {
            AppUpdateEntryPoint.MENU -> service.check(withChannelVersions)
            AppUpdateEntryPoint.PROMPT -> service.ensureChecked(withChannelVersions)
        }
        if (args.install) onInstall()
    }

    fun onRetry() {
        service.check(withChannelVersions)
    }

    fun onUpdate() {
        if (installer.platform.installsItself) {
            service.download(anyFile = false)
        } else {
            intent { postSideEffect(AppUpdateEffect.OpenStorePage(installer.storeUrls.toImmutableList())) }
        }
    }

    /** «Скачать файл»: установщик в «Загрузки»; не скачан — сначала загрузка, без проверки, что он для этой ОС. */
    fun onSave() {
        launchReplacing(SAVE) {
            val effect = when (service.saveCopy()) {
                is Response.Success -> AppUpdateEffect.Saved
                is Response.Error -> AppUpdateEffect.SaveFailed
            }
            postSideEffect(effect)
        }
    }

    fun onLater() {
        intent { postSideEffect(AppUpdateEffect.Close) }
    }

    fun onCancel() {
        cancelLaunched(SAVE)
        service.cancelDownload()
    }

    fun onInstall() {
        if (!hasDownloaded()) return
        if (deviceActivity.isBusy()) {
            intent { reduce { copy(dialog = AppUpdateDialog.DeviceBusy) } }
        } else {
            service.install()
        }
    }

    fun onToggleNotes() {
        intent { reduce { copy(notesExpanded = !notesExpanded) } }
    }

    /** Не основной канал (бета) — с подтверждением; обратно на основной — сразу. */
    fun onSelectChannel(id: Int) {
        val state = currentState
        if (state.channelsLocked || id == state.selectedChannel?.id) return
        val channel = state.channels.firstOrNull { it.id == id } ?: return
        val default = state.channels.firstOrNull { it.isDefault }
        if (channel.isDefault || default == null) {
            service.selectChannel(id)
        } else {
            intent { reduce { copy(dialog = AppUpdateDialog.SwitchChannel(channel, default.name)) } }
        }
    }

    fun onConfirmDialog() {
        intent {
            val dialog = currentState.dialog
            reduce { copy(dialog = null) }
            when (dialog) {
                AppUpdateDialog.DeviceBusy -> service.install()
                is AppUpdateDialog.SwitchChannel -> service.selectChannel(dialog.channel.id)
                null -> Unit
            }
        }
    }

    fun onDismissDialog() {
        intent { reduce { copy(dialog = null) } }
    }

    private fun hasDownloaded(): Boolean = when (val stage = currentState.stage) {
        is AppUpdateStage.Downloaded -> true
        is AppUpdateStage.DownloadFailed -> stage.canSave
        else -> false
    }

    private companion object {
        const val SAVE = "save"
    }
}
