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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpDropdown
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FailureKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase
import specmp.feature.device.firmware.generated.resources.Res
import specmp.feature.device.firmware.generated.resources.firmware_common_btn_cancel
import specmp.feature.device.firmware.generated.resources.firmware_common_btn_retry
import specmp.feature.device.firmware.generated.resources.firmware_common_channel_alpha
import specmp.feature.device.firmware.generated.resources.firmware_common_channel_beta
import specmp.feature.device.firmware.generated.resources.firmware_common_channel_release
import specmp.feature.device.firmware.generated.resources.firmware_common_msg_test_channel
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_archive
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_disconnect
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_download
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_exit_boot
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_file
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_ok
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_reconnect
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_support
import specmp.feature.device.firmware.generated.resources.firmware_update_btn_update
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_label_current
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_label_found
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_label_whats_new
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_msg_local_file
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_msg_no_description
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_msg_older
import specmp.feature.device.firmware.generated.resources.firmware_update_confirm_title
import specmp.feature.device.firmware.generated.resources.firmware_update_download_msg
import specmp.feature.device.firmware.generated.resources.firmware_update_download_title
import specmp.feature.device.firmware.generated.resources.firmware_update_entering_msg
import specmp.feature.device.firmware.generated.resources.firmware_update_entering_title
import specmp.feature.device.firmware.generated.resources.firmware_update_entering_waiting
import specmp.feature.device.firmware.generated.resources.firmware_update_error_data
import specmp.feature.device.firmware.generated.resources.firmware_update_error_exit
import specmp.feature.device.firmware.generated.resources.firmware_update_error_file
import specmp.feature.device.firmware.generated.resources.firmware_update_error_file_too_large
import specmp.feature.device.firmware.generated.resources.firmware_update_error_msg
import specmp.feature.device.firmware.generated.resources.firmware_update_error_source
import specmp.feature.device.firmware.generated.resources.firmware_update_error_title
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_applying
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_crc
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_device_not_ready
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_lost_packet
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_preparation
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_timeout
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_unknown
import specmp.feature.device.firmware.generated.resources.firmware_update_failure_wrong_packet_length
import specmp.feature.device.firmware.generated.resources.firmware_update_finishing_msg
import specmp.feature.device.firmware.generated.resources.firmware_update_finishing_title
import specmp.feature.device.firmware.generated.resources.firmware_update_label_channel
import specmp.feature.device.firmware.generated.resources.firmware_update_label_subsystem
import specmp.feature.device.firmware.generated.resources.firmware_update_label_version
import specmp.feature.device.firmware.generated.resources.firmware_update_loading
import specmp.feature.device.firmware.generated.resources.firmware_update_lost_msg_idle
import specmp.feature.device.firmware.generated.resources.firmware_update_lost_msg_updating
import specmp.feature.device.firmware.generated.resources.firmware_update_lost_title
import specmp.feature.device.firmware.generated.resources.firmware_update_msg_power
import specmp.feature.device.firmware.generated.resources.firmware_update_status_apply
import specmp.feature.device.firmware.generated.resources.firmware_update_status_prepare
import specmp.feature.device.firmware.generated.resources.firmware_update_status_transfer
import specmp.feature.device.firmware.generated.resources.firmware_update_status_verify
import specmp.feature.device.firmware.generated.resources.firmware_update_step_apply
import specmp.feature.device.firmware.generated.resources.firmware_update_step_prepare
import specmp.feature.device.firmware.generated.resources.firmware_update_step_transfer
import specmp.feature.device.firmware.generated.resources.firmware_update_step_verify
import specmp.feature.device.firmware.generated.resources.firmware_update_success_msg
import specmp.feature.device.firmware.generated.resources.firmware_update_success_msg_boot_ble
import specmp.feature.device.firmware.generated.resources.firmware_update_success_msg_boot_usb
import specmp.feature.device.firmware.generated.resources.firmware_update_success_title

private val BigButtonHeight = 52.dp

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
    CmpText(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
}

@Composable
internal fun Message(text: String) {
    CmpText(text = text, color = cmpThemeColors().onSurfaceVariant, textAlign = TextAlign.Center)
}

@Composable
internal fun LoadingBody() {
    CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
    Message(stringResource(Res.string.firmware_update_loading))
}

@Composable
internal fun EnteringBody() {
    Headline(stringResource(Res.string.firmware_update_entering_title))
    Message(stringResource(Res.string.firmware_update_entering_msg))
    CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
        CmpText(
            text = stringResource(Res.string.firmware_update_entering_waiting),
            color = cmpThemeColors().onSurfaceVariant
        )
    }
    InfoBanner(text = stringResource(Res.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
}

/** Выбор источника: подсистема, версия блока, канал, «Скачать прошивку», «Архив прошивок». */
@Composable
internal fun SourceBody(state: FirmwareUpdateState, actions: FirmwareUpdateActions) {
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabeledField(stringResource(Res.string.firmware_update_label_subsystem)) {
            if (state.moduleTitles.size <= 1) {
                CmpText(
                    text = state.moduleTitles.firstOrNull().orEmpty(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = cmpThemeColors().primary
                )
            } else {
                CmpDropdown(
                    selectedValue = state.moduleTitles.getOrNull(state.selectedModule),
                    placeholder = "",
                    options = state.moduleTitles,
                    onOptionSelect = { index, _ -> actions.onModuleSelected(index) }
                )
            }
        }
        state.currentVersion?.let { version ->
            LabeledField(stringResource(Res.string.firmware_update_label_version)) {
                VersionText(version, fontSize = 20.sp)
            }
        }
        LabeledField(stringResource(Res.string.firmware_update_label_channel)) {
            val options = FirmwareChannel.entries.map { stringResource(it.longLabel()) }
            CmpDropdown(
                selectedValue = options[state.channel.ordinal],
                placeholder = "",
                options = options,
                onOptionSelect = { index, _ -> actions.onChannelSelected(FirmwareChannel.entries[index]) }
            )
        }
        if (state.channel.isTest) InfoBanner(text = stringResource(Res.string.firmware_common_msg_test_channel))
        if (state.exitError) {
            InfoBanner(
                text = stringResource(Res.string.firmware_update_error_exit),
                tone = BannerTone.Error,
                icon = CmpIcons.LinkOff
            )
        }
        if (state.sourceError) {
            InfoBanner(
                text = stringResource(Res.string.firmware_update_error_source),
                tone = BannerTone.Error,
                icon = CmpIcons.CloudOff
            )
        }
        state.fileError?.let { error ->
            InfoBanner(
                text = stringResource(
                    when (error) {
                        FileError.TooLarge -> Res.string.firmware_update_error_file_too_large
                        FileError.Failed -> Res.string.firmware_update_error_file
                    }
                ),
                tone = BannerTone.Error,
                icon = CmpIcons.ErrorOutline
            )
        }
        CmpButton(
            text = stringResource(Res.string.firmware_update_btn_download),
            onClick = actions.onDownloadClick,
            modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_DOWNLOAD_TAG)
        )
        CmpOutlinedButton(
            text = stringResource(Res.string.firmware_update_btn_archive),
            onClick = actions.onArchiveClick,
            modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_ARCHIVE_TAG)
        )
        CmpOutlinedButton(
            text = stringResource(Res.string.firmware_update_btn_file),
            onClick = actions.onFileClick,
            modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_FILE_TAG)
        )
        if (state.canExitBoot) {
            CmpOutlinedButton(
                text = stringResource(Res.string.firmware_update_btn_exit_boot),
                onClick = actions.onExitBootClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(EXIT_BOOT_TAG)
            )
        }
    }
}

private fun FirmwareChannel.longLabel(): StringResource = when (this) {
    FirmwareChannel.RELEASE -> Res.string.firmware_common_channel_release
    FirmwareChannel.BETA -> Res.string.firmware_common_channel_beta
    FirmwareChannel.ALPHA -> Res.string.firmware_common_channel_alpha
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpText(text = label, fontWeight = FontWeight.Medium, color = cmpThemeColors().onSurfaceVariant)
        content()
    }
}

@Composable
internal fun DataErrorBody(onRetry: () -> Unit) {
    Message(stringResource(Res.string.firmware_update_error_data))
    CmpButton(
        text = stringResource(Res.string.firmware_common_btn_retry),
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_RETRY_TAG)
    )
}

@Composable
internal fun DownloadBody() {
    Headline(stringResource(Res.string.firmware_update_download_title))
    Message(stringResource(Res.string.firmware_update_download_msg))
    CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
}

/** Подтверждение версии: текущая и найденная, «Что нового», предупреждения, «Обновить» / «Отмена». */
@Composable
internal fun ConfirmBody(confirm: ConfirmUi, testChannel: Boolean, onUpdate: () -> Unit, onCancel: () -> Unit) {
    val colors = cmpThemeColors()
    Headline(stringResource(Res.string.firmware_update_confirm_title))
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp)) {
            confirm.currentVersion?.let { current ->
                VersionRow(stringResource(Res.string.firmware_update_confirm_label_current), current)
                CmpDivider()
            }
            VersionRow(stringResource(Res.string.firmware_update_confirm_label_found), confirm.newVersion)
        }
    }
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerHigh
    ) {
        CmpColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(
                text = stringResource(Res.string.firmware_update_confirm_label_whats_new),
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant
            )
            CmpText(
                text = confirm.description.ifBlank {
                    stringResource(Res.string.firmware_update_confirm_msg_no_description)
                },
                style = CmpTextStyle.Body,
                modifier = Modifier.heightIn(max = WhatsNewMaxHeight).verticalScroll(rememberScrollState())
            )
        }
    }
    if (confirm.localFile) {
        InfoBanner(text = stringResource(Res.string.firmware_update_confirm_msg_local_file), icon = CmpIcons.FolderOpen)
    }
    if (confirm.older) InfoBanner(text = stringResource(Res.string.firmware_update_confirm_msg_older))
    if (testChannel) InfoBanner(text = stringResource(Res.string.firmware_common_msg_test_channel))
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpButton(
            text = stringResource(Res.string.firmware_update_btn_update),
            onClick = onUpdate,
            modifier = Modifier.weight(1f).height(BigButtonHeight).testTag(UPDATE_CONFIRM_TAG)
        )
        CmpOutlinedButton(
            text = stringResource(Res.string.firmware_common_btn_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(BigButtonHeight).testTag(UPDATE_CANCEL_TAG)
        )
    }
}

@Composable
private fun VersionRow(label: String, version: String) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CmpText(text = label, color = cmpThemeColors().onSurfaceVariant)
        VersionText(version, fontSize = 20.sp)
    }
}

/** Процесс обновления: степпер из четырёх шагов, статус, процент передачи, индикатор и постоянное предупреждение. */
@Composable
internal fun ProgressBody(phase: UpdatePhase) {
    val colors = cmpThemeColors()
    val step = when (phase) {
        UpdatePhase.Prepare -> 0
        is UpdatePhase.Transfer -> 1
        UpdatePhase.Verify -> 2
        else -> 3
    }
    val steps = listOf(
        Res.string.firmware_update_step_prepare,
        Res.string.firmware_update_step_transfer,
        Res.string.firmware_update_step_verify,
        Res.string.firmware_update_step_apply
    )
    val percent = (phase as? UpdatePhase.Transfer)?.percent
    CmpColumn(
        modifier = Modifier.fillMaxWidth().testTag(UPDATE_PROGRESS_TAG),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            steps.forEachIndexed { index, label ->
                CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CmpSurface(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = if (index <= step) colors.primary else colors.surfaceVariant
                    ) {}
                    CmpText(
                        text = stringResource(label),
                        fontSize = 12.sp,
                        maxLines = 1,
                        fontWeight = if (index == step) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == step) colors.onSurface else colors.onSurfaceVariant
                    )
                }
            }
        }
        CmpColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CmpText(
                text = stringResource(phase.statusRes()),
                style = CmpTextStyle.Title,
                textAlign = TextAlign.Center
            )
            // Высота под процент зарезервирована: строка не прыгает, когда процент появляется.
            CmpRow(modifier = Modifier.height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                if (percent != null) {
                    CmpText(text = "$percent%", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                }
            }
        }
        CmpLinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(12.dp),
            progress = percent?.let { it / PERCENT_MAX },
            color = colors.primary
        )
        InfoBanner(text = stringResource(Res.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
    }
}

private const val PERCENT_MAX = 100f

private fun UpdatePhase.statusRes(): StringResource = when (this) {
    UpdatePhase.Prepare -> Res.string.firmware_update_status_prepare
    is UpdatePhase.Transfer -> Res.string.firmware_update_status_transfer
    UpdatePhase.Verify -> Res.string.firmware_update_status_verify
    else -> Res.string.firmware_update_status_apply
}

/** Успех: блок остался в загрузчике (подсказка по способу выйти зависит от связи) или уже вернулся в рабочий режим. */
@Composable
internal fun SuccessBody(inBoot: Boolean, canExitBoot: Boolean, onOk: () -> Unit) {
    Headline(stringResource(Res.string.firmware_update_success_title))
    Message(
        stringResource(
            when {
                !inBoot -> Res.string.firmware_update_success_msg
                canExitBoot -> Res.string.firmware_update_success_msg_boot_ble
                else -> Res.string.firmware_update_success_msg_boot_usb
            }
        )
    )
    CmpButton(
        text = stringResource(Res.string.firmware_update_btn_ok),
        onClick = onOk,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(RESULT_OK_TAG)
    )
}

@Composable
internal fun FinishingBody() {
    Headline(stringResource(Res.string.firmware_update_finishing_title))
    Message(stringResource(Res.string.firmware_update_finishing_msg))
    CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
}

@Composable
internal fun ErrorBody(failure: FailureKind, onRetry: () -> Unit, onSupport: () -> Unit) {
    Headline(stringResource(Res.string.firmware_update_error_title))
    InfoBanner(text = stringResource(failure.messageRes()), tone = BannerTone.Error, icon = CmpIcons.AlertCircle)
    CmpText(
        text = stringResource(Res.string.firmware_update_error_msg),
        color = cmpThemeColors().onSurfaceVariant,
        style = CmpTextStyle.Body,
        textAlign = TextAlign.Center
    )
    CmpButton(
        text = stringResource(Res.string.firmware_common_btn_retry),
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_RETRY_TAG)
    )
    CmpOutlinedButton(
        text = stringResource(Res.string.firmware_update_btn_support),
        onClick = onSupport,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_SUPPORT_TAG)
    )
}

private fun FailureKind.messageRes(): StringResource = when (this) {
    FailureKind.Crc -> Res.string.firmware_update_failure_crc
    FailureKind.Timeout -> Res.string.firmware_update_failure_timeout
    FailureKind.LostPacket -> Res.string.firmware_update_failure_lost_packet
    FailureKind.WrongPacketLength -> Res.string.firmware_update_failure_wrong_packet_length
    FailureKind.DeviceNotReady -> Res.string.firmware_update_failure_device_not_ready
    FailureKind.ApplyingError -> Res.string.firmware_update_failure_applying
    FailureKind.Preparation -> Res.string.firmware_update_failure_preparation
    FailureKind.Unknown -> Res.string.firmware_update_failure_unknown
}

@Composable
internal fun LostBody(updating: Boolean, onReconnect: () -> Unit, onDisconnect: () -> Unit) {
    Headline(stringResource(Res.string.firmware_update_lost_title))
    Message(
        stringResource(
            if (updating) Res.string.firmware_update_lost_msg_updating else Res.string.firmware_update_lost_msg_idle
        )
    )
    CmpButton(
        text = stringResource(Res.string.firmware_update_btn_reconnect),
        onClick = onReconnect,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(LOST_RECONNECT_TAG)
    )
    CmpOutlinedButton(
        text = stringResource(Res.string.firmware_update_btn_disconnect),
        onClick = onDisconnect,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(LOST_DISCONNECT_TAG)
    )
}
