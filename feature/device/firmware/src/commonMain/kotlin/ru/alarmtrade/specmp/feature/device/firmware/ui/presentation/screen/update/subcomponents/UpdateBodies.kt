// *Body-функции эмитят несколько узлов в Column родителя: обёртка добавила бы лишний layout-узел
@file:Suppress("ktlint:compose:multiple-emitters-check")

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can_profile
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_msg_test_channel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_alpha
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_beta
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_release
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_archive
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_disconnect
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_exit_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_reconnect
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_support
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_update
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_data
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_exit
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_applying
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_crc
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_device_not_ready
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_lost_packet
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_preparation
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_timeout
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_unknown
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_failure_wrong_packet_length
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_file_too_large
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_msg
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_source
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_title
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_format_confirm_can_profile
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_item_step_apply
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_item_step_prepare
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_item_step_transfer
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_item_step_verify
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_channel
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_confirm_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_confirm_can_will_be
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_confirm_current
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_confirm_found
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_confirm_whats_new
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_subsystem
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_version
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_loading
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_confirm_can_other
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_confirm_can_same
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_confirm_local_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_confirm_no_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_confirm_older
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_entering
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_entering_waiting
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_finishing
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_lost_idle
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_lost_updating
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_power
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_success
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_success_boot_ble
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_success_boot_usb
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_status_apply
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_status_prepare
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_status_reconnecting
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_status_transfer
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_status_verify
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_entering
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_finishing
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_lost
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_success
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_value_can_factory
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_value_confirm_can_factory
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpDropdown
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.BlockCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FailureKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase

private val ProgressSteps = listOf(
    Strings.string.firmware_update_item_step_prepare,
    Strings.string.firmware_update_item_step_transfer,
    Strings.string.firmware_update_item_step_verify,
    Strings.string.firmware_update_item_step_apply,
)

private val StepBarHeight = 6.dp
private val ProgressHeight = 12.dp
private val PercentRowHeight = 56.dp
private val SpinnerStroke = 2.5.dp
private val HighlightBorder = 2.dp

/** Предел высоты блока «Что нового»: длинный текст прокручивается внутри, кнопки остаются на экране. */
private val WhatsNewMaxHeight = 220.dp

internal const val SOURCE_DOWNLOAD_TAG = "firmware_source_download"
internal const val SOURCE_ARCHIVE_TAG = "firmware_source_archive"
internal const val SOURCE_FILE_TAG = "firmware_source_file"
internal const val UPDATE_CONFIRM_TAG = "firmware_update_confirm"
internal const val UPDATE_CANCEL_TAG = "firmware_update_cancel"
internal const val UPDATE_PROGRESS_TAG = "firmware_progress"
internal const val RESULT_OK_TAG = "firmware_result_ok"
internal const val EXIT_BOOT_TAG = "firmware_exit_boot"
internal const val ERROR_RETRY_TAG = "firmware_error_retry"
internal const val ERROR_SUPPORT_TAG = "firmware_error_support"
internal const val LOST_RECONNECT_TAG = "firmware_lost_reconnect"
internal const val LOST_DISCONNECT_TAG = "firmware_lost_disconnect"

@Composable
internal fun Headline(text: String) {
    CmpText(text = text, style = CmpTextStyle.Headline1, textAlign = TextAlign.Center)
}

@Composable
internal fun Message(text: String) {
    CmpText(
        text = text,
        style = CmpTextStyle.Paragraph2,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun LoadingBody() {
    CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))
    Message(stringResource(Strings.string.firmware_update_loading))
}

@Composable
internal fun EnteringBody() {
    Headline(stringResource(Strings.string.firmware_update_title_entering))
    Message(stringResource(Strings.string.firmware_update_msg_entering))
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), strokeWidth = SpinnerStroke)
        CmpText(
            text = stringResource(Strings.string.firmware_update_msg_entering_waiting),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
    CmpBanner(text = stringResource(Strings.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
}

@Composable
internal fun SourceBody(state: FirmwareUpdateState, actions: FirmwareUpdateActions, fileOnly: Boolean = false) {
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        LabeledField(stringResource(Strings.string.firmware_update_label_subsystem)) {
            if (state.moduleTitles.size <= 1) {
                CmpText(
                    text = state.moduleTitles.firstOrNull().orEmpty(),
                    style = CmpTextStyle.Headline2,
                    color = CmpTheme.colors.brandPrimaryDefault,
                )
            } else {
                CmpDropdown(
                    selectedValue = state.moduleTitles.getOrNull(state.selectedModule),
                    placeholder = "",
                    options = state.moduleTitles,
                    onOptionSelect = { index, _ -> actions.onModuleSelected(index) },
                )
            }
        }
        state.currentVersion?.let { version ->
            LabeledField(stringResource(Strings.string.firmware_update_label_version)) {
                VersionText(version, style = CmpTextStyle.Headline2)
            }
        }
        state.blockCan?.let { can ->
            LabeledField(stringResource(Strings.string.firmware_update_label_can)) { BlockCan(can) }
        }
        if (!fileOnly) {
            LabeledField(stringResource(Strings.string.firmware_update_label_channel)) {
                val options = FirmwareChannel.entries.map { stringResource(it.longLabel()) }
                CmpDropdown(
                    selectedValue = options[state.channel.ordinal],
                    placeholder = "",
                    options = options,
                    onOptionSelect = { index, _ -> actions.onChannelSelected(FirmwareChannel.entries[index]) },
                )
            }
        }
        if (!fileOnly && state.channel.isTest) {
            CmpBanner(text = stringResource(Strings.string.firmware_common_msg_test_channel))
        }
        if (state.exitError) {
            CmpBanner(
                text = stringResource(Strings.string.firmware_update_error_exit),
                tone = CmpBannerTone.Error,
                icon = CmpIcons.LinkOff,
            )
        }
        state.sourceError?.let { error ->
            CmpBanner(
                text = error.text(Strings.string.firmware_update_error_source),
                tone = CmpBannerTone.Error,
                icon = CmpIcons.CloudOff,
            )
        }
        state.fileError?.let { error ->
            CmpBanner(
                text = stringResource(
                    when (error) {
                        FileError.TooLarge -> Strings.string.firmware_update_error_file_too_large
                        FileError.Failed -> Strings.string.firmware_update_error_file
                    },
                ),
                tone = CmpBannerTone.Error,
                icon = CmpIcons.ErrorOutline,
            )
        }
        if (fileOnly) {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_file),
                onClick = actions.onFileClick,
                fullWidth = true,
                icon = CmpIcons.FolderOpen,
                modifier = Modifier.testTag(SOURCE_FILE_TAG),
            )
        } else {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_download),
                onClick = actions.onDownloadClick,
                fullWidth = true,
                icon = CmpIcons.Download,
                modifier = Modifier.testTag(SOURCE_DOWNLOAD_TAG),
            )
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_archive),
                onClick = actions.onArchiveClick,
                type = CmpButtonType.Secondary,
                fullWidth = true,
                icon = CmpIcons.History,
                modifier = Modifier.testTag(SOURCE_ARCHIVE_TAG),
            )
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_file),
                onClick = actions.onFileClick,
                type = CmpButtonType.Secondary,
                fullWidth = true,
                icon = CmpIcons.FolderOpen,
                modifier = Modifier.testTag(SOURCE_FILE_TAG),
            )
        }
        if (state.canExitBoot) {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_exit_boot),
                onClick = actions.onExitBootClick,
                type = CmpButtonType.Secondary,
                fullWidth = true,
                modifier = Modifier.testTag(EXIT_BOOT_TAG),
            )
        }
    }
}

private fun FirmwareChannel.longLabel(): StringResource = when (this) {
    FirmwareChannel.RELEASE -> Strings.string.firmware_common_value_channel_release
    FirmwareChannel.BETA -> Strings.string.firmware_common_value_channel_beta
    FirmwareChannel.ALPHA -> Strings.string.firmware_common_value_channel_alpha
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(text = label, style = CmpTextStyle.Headline3, color = CmpTheme.colors.foregroundOnDarkSecondaryDefault)
        content()
    }
}

@Composable
internal fun DataErrorBody(onRetry: () -> Unit) {
    Message(stringResource(Strings.string.firmware_update_error_data))
    CmpButton(
        text = stringResource(Strings.string.firmware_common_btn_retry),
        onClick = onRetry,
        fullWidth = true,
        icon = CmpIcons.Refresh,
        modifier = Modifier.testTag(ERROR_RETRY_TAG),
    )
}

@Composable
internal fun DownloadBody() {
    Headline(stringResource(Strings.string.firmware_update_title_download))
    Message(stringResource(Strings.string.firmware_update_msg_download))
    CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))
}

@Composable
internal fun ConfirmBody(confirm: ConfirmUi, testChannel: Boolean, onUpdate: () -> Unit, onCancel: () -> Unit) {
    val colors = CmpTheme.colors
    Headline(stringResource(Strings.string.firmware_update_title_confirm))
    CmpCard(modifier = Modifier.fillMaxWidth()) {
        CmpColumn(modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8)) {
            confirm.currentVersion?.let { current ->
                VersionRow(stringResource(Strings.string.firmware_update_label_confirm_current), current)
                CmpDivider()
            }
            VersionRow(stringResource(Strings.string.firmware_update_label_confirm_found), confirm.newVersion)
        }
    }
    confirm.can?.let { ConfirmCan(it) }
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = colors.backgroundSurfaceHighlight,
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(
                text = stringResource(Strings.string.firmware_update_label_confirm_whats_new),
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
            CmpText(
                text = confirm.description.ifBlank {
                    stringResource(Strings.string.firmware_update_msg_confirm_no_description)
                },
                style = CmpTextStyle.Paragraph2,
                modifier = Modifier.heightIn(max = WhatsNewMaxHeight).verticalScroll(rememberScrollState()),
            )
        }
    }
    if (confirm.localFile) {
        CmpBanner(
            text = stringResource(Strings.string.firmware_update_msg_confirm_local_file),
            icon = CmpIcons.FolderOpen,
        )
    }
    if (confirm.older) CmpBanner(text = stringResource(Strings.string.firmware_update_msg_confirm_older))
    if (testChannel) CmpBanner(text = stringResource(Strings.string.firmware_common_msg_test_channel))
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpButton(
            text = stringResource(Strings.string.firmware_update_btn_update),
            onClick = onUpdate,
            modifier = Modifier.weight(1f).testTag(UPDATE_CONFIRM_TAG),
        )
        CmpButton(
            text = stringResource(Strings.string.firmware_common_btn_cancel),
            onClick = onCancel,
            type = CmpButtonType.Secondary,
            modifier = Modifier.weight(1f).testTag(UPDATE_CANCEL_TAG),
        )
    }
}

@Composable
private fun VersionRow(label: String, version: String) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph1,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.weight(1f),
        )
        VersionText(version, style = CmpTextStyle.Headline2)
    }
}

@Composable
internal fun ProgressBody(phase: UpdatePhase) {
    val colors = CmpTheme.colors
    val step = when (phase) {
        UpdatePhase.Prepare -> 0
        is UpdatePhase.Transfer -> 1
        UpdatePhase.Verify -> 2
        else -> 3
    }
    val percent = (phase as? UpdatePhase.Transfer)?.percent
    CmpColumn(
        modifier = Modifier.fillMaxWidth().testTag(UPDATE_PROGRESS_TAG),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
    ) {
        CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            ProgressSteps.forEachIndexed { index, label ->
                CmpColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    CmpSurface(
                        modifier = Modifier.fillMaxWidth().height(StepBarHeight),
                        shape = RoundedCornerShape(CmpRadius.round),
                        color = if (index <= step) colors.brandPrimaryDefault else colors.backgroundSurfaceHighlight,
                    ) {}
                    CmpText(
                        text = stringResource(label),
                        style = if (index == step) CmpTextStyle.Additional0 else CmpTextStyle.Additional1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (index == step) {
                            colors.foregroundOnDarkPrimaryDefault
                        } else {
                            colors.foregroundOnDarkSecondaryDefault
                        },
                    )
                }
            }
        }
        CmpColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CmpText(
                text = stringResource(phase.statusRes()),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            // Высота под процент зарезервирована: строка не прыгает, когда процент появляется.
            CmpRow(modifier = Modifier.height(PercentRowHeight), verticalAlignment = Alignment.CenterVertically) {
                if (percent != null) {
                    CmpText(text = "$percent%", style = CmpTextStyle.Accent3, color = colors.brandPrimaryDefault)
                }
            }
        }
        CmpLinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(ProgressHeight),
            progress = percent?.let { it / PERCENT_MAX },
            color = colors.brandPrimaryDefault,
        )
        CmpBanner(text = stringResource(Strings.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
    }
}

private const val PERCENT_MAX = 100f

private fun UpdatePhase.statusRes(): StringResource = when (this) {
    UpdatePhase.Prepare -> Strings.string.firmware_update_status_prepare

    is UpdatePhase.Transfer -> if (reconnecting) {
        Strings.string.firmware_update_status_reconnecting
    } else {
        Strings.string.firmware_update_status_transfer
    }

    UpdatePhase.Verify -> Strings.string.firmware_update_status_verify

    else -> Strings.string.firmware_update_status_apply
}

@Composable
internal fun SuccessBody(inBoot: Boolean, canExitBoot: Boolean, onOk: () -> Unit) {
    Headline(stringResource(Strings.string.firmware_update_title_success))
    Message(
        stringResource(
            when {
                !inBoot -> Strings.string.firmware_update_msg_success
                canExitBoot -> Strings.string.firmware_update_msg_success_boot_ble
                else -> Strings.string.firmware_update_msg_success_boot_usb
            },
        ),
    )
    CmpButton(
        text = stringResource(Strings.string.firmware_update_btn_ok),
        onClick = onOk,
        fullWidth = true,
        modifier = Modifier.testTag(RESULT_OK_TAG),
    )
}

@Composable
internal fun FinishingBody() {
    Headline(stringResource(Strings.string.firmware_update_title_finishing))
    Message(stringResource(Strings.string.firmware_update_msg_finishing))
    CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))
}

@Composable
internal fun ErrorBody(failure: FailureKind, onRetry: () -> Unit, onSupport: (() -> Unit)?) {
    Headline(stringResource(Strings.string.firmware_update_error_title))
    CmpBanner(text = stringResource(failure.messageRes()), tone = CmpBannerTone.Error, icon = CmpIcons.AlertCircle)
    CmpText(
        text = stringResource(Strings.string.firmware_update_error_msg),
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        style = CmpTextStyle.Paragraph2,
        textAlign = TextAlign.Center,
    )
    CmpButton(
        text = stringResource(Strings.string.firmware_common_btn_retry),
        onClick = onRetry,
        fullWidth = true,
        icon = CmpIcons.Refresh,
        modifier = Modifier.testTag(ERROR_RETRY_TAG),
    )
    if (onSupport != null) {
        CmpButton(
            text = stringResource(Strings.string.firmware_update_btn_support),
            onClick = onSupport,
            type = CmpButtonType.Secondary,
            fullWidth = true,
            icon = CmpIcons.SupportAgent,
            modifier = Modifier.testTag(ERROR_SUPPORT_TAG),
        )
    }
}

private fun FailureKind.messageRes(): StringResource = when (this) {
    FailureKind.Crc -> Strings.string.firmware_update_error_failure_crc
    FailureKind.Timeout -> Strings.string.firmware_update_error_failure_timeout
    FailureKind.LostPacket -> Strings.string.firmware_update_error_failure_lost_packet
    FailureKind.WrongPacketLength -> Strings.string.firmware_update_error_failure_wrong_packet_length
    FailureKind.DeviceNotReady -> Strings.string.firmware_update_error_failure_device_not_ready
    FailureKind.ApplyingError -> Strings.string.firmware_update_error_failure_applying
    FailureKind.Preparation -> Strings.string.firmware_update_error_failure_preparation
    FailureKind.Unknown -> Strings.string.firmware_update_error_failure_unknown
}

@Composable
internal fun LostBody(updating: Boolean, onReconnect: () -> Unit, onDisconnect: () -> Unit) {
    Headline(stringResource(Strings.string.firmware_update_title_lost))
    Message(
        stringResource(
            if (updating) {
                Strings.string.firmware_update_msg_lost_updating
            } else {
                Strings.string.firmware_update_msg_lost_idle
            },
        ),
    )
    CmpButton(
        text = stringResource(Strings.string.firmware_update_btn_reconnect),
        onClick = onReconnect,
        fullWidth = true,
        icon = CmpIcons.Sync,
        modifier = Modifier.testTag(LOST_RECONNECT_TAG),
    )
    CmpButton(
        text = stringResource(Strings.string.firmware_update_btn_disconnect),
        onClick = onDisconnect,
        type = CmpButtonType.Secondary,
        fullWidth = true,
        icon = CmpIcons.LinkOff,
        modifier = Modifier.testTag(LOST_DISCONNECT_TAG),
    )
}

@Composable
private fun BlockCan(can: BlockCanUi) {
    val code = can.code
    if (code == null) {
        CmpText(
            text = stringResource(Strings.string.firmware_update_value_can_factory),
            style = CmpTextStyle.Paragraph2,
        )
        return
    }
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VersionText(stringResource(Strings.string.firmware_archive_label_can, code), style = CmpTextStyle.Headline2)
        can.profile?.let { profile ->
            CmpText(
                text = stringResource(Strings.string.firmware_archive_label_can_profile) + " " + profile,
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun ConfirmCan(can: ConfirmCanUi) {
    val colors = CmpTheme.colors
    val content = colors.foregroundOnDarkPrimaryDefault
    CmpCard(
        modifier = Modifier.fillMaxWidth().testTag(UPDATE_CONFIRM_CAN_TAG),
        containerColor = if (can.same) colors.backgroundSurfaceCard else colors.brandPrimaryHighlight,
        border = if (can.same) {
            BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight)
        } else {
            BorderStroke(HighlightBorder, colors.brandPrimaryDefault)
        },
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.firmware_update_label_confirm_can),
                    style = CmpTextStyle.Paragraph1,
                    color = content,
                )
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = can.blockCode ?: stringResource(
                            Strings.string.firmware_update_value_confirm_can_factory,
                        ),
                        style = CmpTextStyle.Headline2,
                        color = content,
                    )
                    if (!can.same) {
                        CmpIcon(
                            icon = CmpIcons.ArrowForward,
                            contentDescription = null,
                            tint = colors.brandPrimaryDefault,
                            modifier = Modifier.size(CmpSize.iconM),
                        )
                        CmpText(
                            text = stringResource(Strings.string.firmware_update_label_confirm_can_will_be),
                            style = CmpTextStyle.Paragraph2,
                            color = content,
                        )
                        CmpText(text = can.newCode, style = CmpTextStyle.Headline2, color = content)
                    }
                }
            }
            val message = stringResource(
                if (can.same) {
                    Strings.string.firmware_update_msg_confirm_can_same
                } else {
                    Strings.string.firmware_update_msg_confirm_can_other
                },
            )
            CmpText(
                text = can.newProfile?.let {
                    stringResource(Strings.string.firmware_update_format_confirm_can_profile, message, it)
                } ?: message,
                style = CmpTextStyle.Paragraph2,
                color = content,
            )
        }
    }
}

internal const val UPDATE_CONFIRM_CAN_TAG = "firmware_update_confirm_can"
