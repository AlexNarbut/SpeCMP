package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_btn_recheck
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_channel
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_older
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_server
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_msg_wait
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_checking
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_empty
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_offline
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_older
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_server
import ru.alarmtrade.specmp.core.localization.resources.appupdate_check_title_uptodate
import ru.alarmtrade.specmp.core.localization.resources.appupdate_download_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.appupdate_download_msg_background
import ru.alarmtrade.specmp.core.localization.resources.appupdate_download_msg_progress
import ru.alarmtrade.specmp.core.localization.resources.appupdate_download_msg_version
import ru.alarmtrade.specmp.core.localization.resources.appupdate_download_title
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_btn_install
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_msg_download
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_msg_file
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_msg_install
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_msg_write
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_title_download
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_title_file
import ru.alarmtrade.specmp.core.localization.resources.appupdate_failure_title_install
import ru.alarmtrade.specmp.core.localization.resources.appupdate_installing_msg
import ru.alarmtrade.specmp.core.localization.resources.appupdate_installing_title
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_btn_install
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_hint_mac
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_hint_windows
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_msg
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_msg_restart
import ru.alarmtrade.specmp.core.localization.resources.appupdate_ready_title
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_btn_appstore
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_btn_later
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_btn_play
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_btn_save
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_btn_update
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_hint_store
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_msg_available
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_title_available
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateFailureReason
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateStage
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateActions
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

private const val BYTES_IN_MB = 1024L * 1024L
private const val COMPACT_LABEL_LINES = 2
private val ProgressHeight = 8.dp

@Immutable
private class StageActions(val base: AppUpdateActions, val compact: Boolean)

@Composable
internal fun StageCard(
    state: AppUpdateState,
    actions: AppUpdateActions,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val stage = state.stage
    val channel = state.selectedChannel?.name.takeIf { state.showChannels }
    UpdateCard(
        modifier = modifier.testTag(AppUpdateTags.STAGE),
        padding = PaddingValues(if (compact) CmpSpacing.space16 else CmpSpacing.space24),
    ) {
        val stageActions = remember(actions, compact) { StageActions(actions, compact) }
        when (stage) {
            is AppUpdateStage.Available -> Available(stage, state, stageActions)

            is AppUpdateStage.Downloading -> Downloading(stage, stageActions)

            is AppUpdateStage.Downloaded -> Downloaded(stage, state, stageActions)

            AppUpdateStage.Installing -> StageHead(
                title = stringResource(Strings.string.appupdate_installing_title),
                icon = null,
                tone = HeadTone.PRIMARY,
                subtitle = stringResource(Strings.string.appupdate_installing_msg),
            )

            is AppUpdateStage.DownloadFailed -> DownloadFailed(stage, stageActions)

            else -> CheckResult(stage, channel, stageActions)
        }
    }
}

@Composable
private fun CheckResult(stage: AppUpdateStage, channel: String?, actions: StageActions) {
    val channelLine = channel?.let { stringResource(Strings.string.appupdate_check_msg_channel, it) }
    when (stage) {
        AppUpdateStage.Checking -> StageHead(
            title = stringResource(Strings.string.appupdate_check_title_checking),
            icon = null,
            tone = HeadTone.PRIMARY,
            subtitle = channelLine ?: stringResource(Strings.string.appupdate_check_msg_wait),
        )

        AppUpdateStage.UpToDate -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_check_title_uptodate),
                icon = CmpIcons.CheckCircle,
                tone = HeadTone.SUCCESS,
                subtitle = channelLine,
            )
            Recheck(actions)
        }

        is AppUpdateStage.Older -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_check_title_older),
                icon = CmpIcons.CheckCircle,
                tone = HeadTone.SUCCESS,
                subtitle = channelLine,
            )
            InfoNote(
                stringResource(Strings.string.appupdate_check_msg_older, channel.orEmpty(), stage.release.version),
            )
            Recheck(actions)
        }

        AppUpdateStage.EmptyChannel -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_check_title_empty, channel.orEmpty()),
                icon = CmpIcons.Inbox,
                tone = HeadTone.NEUTRAL,
                subtitle = stringResource(Strings.string.appupdate_check_msg_empty),
            )
            Recheck(actions)
        }

        AppUpdateStage.Offline -> CheckError(
            title = stringResource(Strings.string.appupdate_check_title_offline),
            text = stringResource(Strings.string.appupdate_check_msg_offline),
            icon = CmpIcons.WifiOff,
            actions = actions,
        )

        else -> CheckError(
            title = stringResource(Strings.string.appupdate_check_title_server),
            text = stringResource(Strings.string.appupdate_check_msg_server),
            icon = CmpIcons.CloudOff,
            actions = actions,
        )
    }
}

@Composable
private fun Available(stage: AppUpdateStage.Available, state: AppUpdateState, actions: StageActions) {
    val release = stage.release
    val platform = state.process.platform
    StageHead(
        title = stringResource(Strings.string.appupdate_release_title_available),
        icon = CmpIcons.SystemUpdate,
        tone = HeadTone.PRIMARY,
        subtitle = stringResource(
            Strings.string.appupdate_release_msg_available,
            release.version,
            release.publishedAt.formatted(),
        ),
    )
    VersionPills(installed = state.installedVersion, new = release.version)
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        Actions {
            if (platform.installsItself) {
                ActionButton(
                    text = stringResource(Strings.string.appupdate_release_btn_update),
                    compact = actions.compact,
                    onClick = actions.base.onUpdate,
                    icon = CmpIcons.SystemUpdate,
                    tag = AppUpdateTags.UPDATE,
                )
                SaveButton(actions)
            } else {
                ActionButton(
                    compact = actions.compact,
                    text = stringResource(
                        if (platform == AppUpdatePlatform.IOS) {
                            Strings.string.appupdate_release_btn_appstore
                        } else {
                            Strings.string.appupdate_release_btn_play
                        },
                    ),
                    onClick = actions.base.onUpdate,
                    icon = CmpIcons.OpenInNew,
                    tag = AppUpdateTags.STORE,
                )
            }
            if (state.showLater) LaterButton(actions)
        }
        if (!platform.installsItself) {
            CmpText(
                text = stringResource(Strings.string.appupdate_release_hint_store),
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
    if (release.notes.isNotBlank()) {
        CmpDivider()
        ReleaseNotes(text = release.notes, expanded = state.notesExpanded, onToggle = actions.base.onToggleNotes)
    }
}

@Composable
private fun Downloading(stage: AppUpdateStage.Downloading, actions: StageActions) {
    StageHead(
        title = stringResource(Strings.string.appupdate_download_title),
        icon = CmpIcons.Download,
        tone = HeadTone.PRIMARY,
        subtitle = stringResource(Strings.string.appupdate_download_msg_version, stage.release.version),
    )
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpLinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(ProgressHeight).testTag(AppUpdateTags.PROGRESS),
        )
        CmpText(
            text = stringResource(Strings.string.appupdate_download_msg_progress, (stage.bytes / BYTES_IN_MB).toInt()),
            style = CmpTextStyle.Headline2,
        )
        CmpText(
            text = stringResource(Strings.string.appupdate_download_msg_background),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
    Actions {
        ActionButton(
            text = stringResource(Strings.string.appupdate_download_btn_cancel),
            compact = actions.compact,
            onClick = actions.base.onCancel,
            type = CmpButtonType.Secondary,
            icon = CmpIcons.Close,
            tag = AppUpdateTags.CANCEL,
        )
    }
}

@Composable
private fun Downloaded(stage: AppUpdateStage.Downloaded, state: AppUpdateState, actions: StageActions) {
    StageHead(
        title = stringResource(Strings.string.appupdate_ready_title),
        icon = CmpIcons.DownloadDone,
        tone = HeadTone.PRIMARY,
        subtitle = stringResource(Strings.string.appupdate_ready_msg, stage.release.version),
    )
    VersionPills(installed = state.installedVersion, new = stage.release.version)
    Actions {
        ActionButton(
            text = stringResource(Strings.string.appupdate_ready_btn_install),
            compact = actions.compact,
            onClick = actions.base.onInstall,
            icon = CmpIcons.RestartAlt,
            tag = AppUpdateTags.INSTALL,
        )
        if (state.showLater) LaterButton(actions)
    }
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpText(
            text = stringResource(Strings.string.appupdate_ready_msg_restart),
            style = CmpTextStyle.Paragraph1,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        val mac = state.process.platform == AppUpdatePlatform.MACOS
        InfoNote(
            text = stringResource(
                if (mac) Strings.string.appupdate_ready_hint_mac else Strings.string.appupdate_ready_hint_windows,
            ),
            icon = if (mac) CmpIcons.LaptopMac else CmpIcons.DesktopWindows,
        )
    }
}

@Composable
private fun DownloadFailed(stage: AppUpdateStage.DownloadFailed, actions: StageActions) {
    when (stage.reason) {
        AppUpdateFailureReason.WRONG_FILE -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_failure_title_file),
                icon = CmpIcons.FileDownloadOff,
                tone = HeadTone.NEUTRAL,
                subtitle = stringResource(Strings.string.appupdate_failure_msg_file),
            )
            Actions { SaveButton(actions, type = CmpButtonType.Primary) }
        }

        AppUpdateFailureReason.INSTALL -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_failure_title_install),
                icon = CmpIcons.ErrorOutline,
                tone = HeadTone.ERROR,
                subtitle = stringResource(Strings.string.appupdate_failure_msg_install),
            )
            Actions {
                ActionButton(
                    text = stringResource(Strings.string.appupdate_failure_btn_install),
                    compact = actions.compact,
                    onClick = actions.base.onInstall,
                    icon = CmpIcons.RestartAlt,
                    tag = AppUpdateTags.RETRY,
                )
                SaveButton(actions)
            }
        }

        AppUpdateFailureReason.TRANSFER, AppUpdateFailureReason.FILE_WRITE -> {
            StageHead(
                title = stringResource(Strings.string.appupdate_failure_title_download),
                icon = CmpIcons.ErrorOutline,
                tone = HeadTone.ERROR,
                subtitle = stringResource(
                    if (stage.reason == AppUpdateFailureReason.FILE_WRITE) {
                        Strings.string.appupdate_failure_msg_write
                    } else {
                        Strings.string.appupdate_failure_msg_download
                    },
                ),
            )
            Actions {
                ActionButton(
                    text = stringResource(Strings.string.appupdate_check_btn_retry),
                    compact = actions.compact,
                    onClick = actions.base.onUpdate,
                    icon = CmpIcons.Refresh,
                    tag = AppUpdateTags.RETRY,
                )
                SaveButton(actions)
            }
        }
    }
}

@Composable
private fun CheckError(title: String, text: String, icon: CmpIcons, actions: StageActions) {
    StageHead(title = title, icon = icon, tone = HeadTone.NEUTRAL, subtitle = text)
    Actions {
        ActionButton(
            text = stringResource(Strings.string.appupdate_check_btn_retry),
            compact = actions.compact,
            onClick = actions.base.onRetry,
            icon = CmpIcons.Refresh,
            tag = AppUpdateTags.RETRY,
        )
    }
}

@Composable
private fun Recheck(actions: StageActions) {
    Actions {
        ActionButton(
            text = stringResource(Strings.string.appupdate_check_btn_recheck),
            compact = actions.compact,
            onClick = actions.base.onRetry,
            type = CmpButtonType.Secondary,
            icon = CmpIcons.Refresh,
            tag = AppUpdateTags.RETRY,
        )
    }
}

@Composable
private fun SaveButton(actions: StageActions, type: CmpButtonType = CmpButtonType.Secondary) {
    ActionButton(
        text = stringResource(Strings.string.appupdate_release_btn_save),
        compact = actions.compact,
        onClick = actions.base.onSave,
        type = type,
        icon = CmpIcons.Download,
        tag = AppUpdateTags.SAVE,
    )
}

@Composable
private fun LaterButton(actions: StageActions) {
    ActionButton(
        text = stringResource(Strings.string.appupdate_release_btn_later),
        compact = actions.compact,
        onClick = actions.base.onLater,
        type = CmpButtonType.Tertiary,
        tag = AppUpdateTags.LATER,
    )
}

@Composable
private fun ActionButton(
    text: String,
    compact: Boolean,
    onClick: () -> Unit,
    tag: String,
    type: CmpButtonType = CmpButtonType.Primary,
    icon: CmpIcons? = null,
) {
    CmpButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.testTag(tag),
        type = type,
        size = CmpButtonSize.S,
        icon = icon,
        maxLines = if (compact) COMPACT_LABEL_LINES else 1,
    )
}

@Composable
private fun Actions(content: @Composable () -> Unit) {
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
