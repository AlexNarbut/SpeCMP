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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
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
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.BlockCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FailureKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase

/** Шаги обновления на экране прогресса (подготовка → передача → проверка → применение). */
private val ProgressSteps = listOf(
    Strings.string.firmware_update_item_step_prepare,
    Strings.string.firmware_update_item_step_transfer,
    Strings.string.firmware_update_item_step_verify,
    Strings.string.firmware_update_item_step_apply,
)

internal val BigButtonHeight = 52.dp

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
    Message(stringResource(Strings.string.firmware_update_loading))
}

@Composable
internal fun EnteringBody() {
    Headline(stringResource(Strings.string.firmware_update_title_entering))
    Message(stringResource(Strings.string.firmware_update_msg_entering))
    CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
        CmpText(
            text = stringResource(Strings.string.firmware_update_msg_entering_waiting),
            color = cmpThemeColors().onSurfaceVariant,
        )
    }
    InfoBanner(text = stringResource(Strings.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
}

/**
 * Выбор источника: подсистема, версия блока, канал, «Скачать прошивку», «Архив прошивок», «Выбрать файл…».
 * [fileOnly] — только «Выбрать файл…» (без канала и сервера).
 */
@Composable
internal fun SourceBody(state: FirmwareUpdateState, actions: FirmwareUpdateActions, fileOnly: Boolean = false) {
    CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LabeledField(stringResource(Strings.string.firmware_update_label_subsystem)) {
            if (state.moduleTitles.size <= 1) {
                CmpText(
                    text = state.moduleTitles.firstOrNull().orEmpty(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = cmpThemeColors().primary,
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
                VersionText(version, fontSize = 20.sp)
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
            InfoBanner(text = stringResource(Strings.string.firmware_common_msg_test_channel))
        }
        if (state.exitError) {
            InfoBanner(
                text = stringResource(Strings.string.firmware_update_error_exit),
                tone = BannerTone.Error,
                icon = CmpIcons.LinkOff,
            )
        }
        state.sourceError?.let { error ->
            InfoBanner(
                text = error.text(Strings.string.firmware_update_error_source),
                tone = BannerTone.Error,
                icon = CmpIcons.CloudOff,
            )
        }
        state.fileError?.let { error ->
            InfoBanner(
                text = stringResource(
                    when (error) {
                        FileError.TooLarge -> Strings.string.firmware_update_error_file_too_large
                        FileError.Failed -> Strings.string.firmware_update_error_file
                    },
                ),
                tone = BannerTone.Error,
                icon = CmpIcons.ErrorOutline,
            )
        }
        if (fileOnly) {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_file),
                onClick = actions.onFileClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_FILE_TAG),
            )
        } else {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_download),
                onClick = actions.onDownloadClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_DOWNLOAD_TAG),
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_update_btn_archive),
                onClick = actions.onArchiveClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_ARCHIVE_TAG),
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_update_btn_file),
                onClick = actions.onFileClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(SOURCE_FILE_TAG),
            )
        }
        if (state.canExitBoot) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_update_btn_exit_boot),
                onClick = actions.onExitBootClick,
                modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(EXIT_BOOT_TAG),
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
    CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpText(text = label, fontWeight = FontWeight.Medium, color = cmpThemeColors().onSurfaceVariant)
        content()
    }
}

@Composable
internal fun DataErrorBody(onRetry: () -> Unit) {
    Message(stringResource(Strings.string.firmware_update_error_data))
    CmpButton(
        text = stringResource(Strings.string.firmware_common_btn_retry),
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_RETRY_TAG),
    )
}

@Composable
internal fun DownloadBody() {
    Headline(stringResource(Strings.string.firmware_update_title_download))
    Message(stringResource(Strings.string.firmware_update_msg_download))
    CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
}

/** Подтверждение версии: текущая и найденная, «Что нового», предупреждения, «Обновить» / «Отмена». */
@Composable
internal fun ConfirmBody(confirm: ConfirmUi, testChannel: Boolean, onUpdate: () -> Unit, onCancel: () -> Unit) {
    val colors = cmpThemeColors()
    Headline(stringResource(Strings.string.firmware_update_title_confirm))
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp)) {
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
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerHigh,
    ) {
        CmpColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(
                text = stringResource(Strings.string.firmware_update_label_confirm_whats_new),
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
            )
            CmpText(
                text = confirm.description.ifBlank {
                    stringResource(Strings.string.firmware_update_msg_confirm_no_description)
                },
                style = CmpTextStyle.Body,
                modifier = Modifier.heightIn(max = WhatsNewMaxHeight).verticalScroll(rememberScrollState()),
            )
        }
    }
    if (confirm.localFile) {
        InfoBanner(
            text = stringResource(Strings.string.firmware_update_msg_confirm_local_file),
            icon = CmpIcons.FolderOpen,
        )
    }
    if (confirm.older) InfoBanner(text = stringResource(Strings.string.firmware_update_msg_confirm_older))
    if (testChannel) InfoBanner(text = stringResource(Strings.string.firmware_common_msg_test_channel))
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpButton(
            text = stringResource(Strings.string.firmware_update_btn_update),
            onClick = onUpdate,
            modifier = Modifier.weight(1f).height(BigButtonHeight).testTag(UPDATE_CONFIRM_TAG),
        )
        CmpOutlinedButton(
            text = stringResource(Strings.string.firmware_common_btn_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(BigButtonHeight).testTag(UPDATE_CANCEL_TAG),
        )
    }
}

@Composable
private fun VersionRow(label: String, version: String) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
    val percent = (phase as? UpdatePhase.Transfer)?.percent
    CmpColumn(
        modifier = Modifier.fillMaxWidth().testTag(UPDATE_PROGRESS_TAG),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProgressSteps.forEachIndexed { index, label ->
                CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CmpSurface(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = if (index <= step) colors.primary else colors.surfaceVariant,
                    ) {}
                    CmpText(
                        text = stringResource(label),
                        fontSize = 12.sp,
                        maxLines = 1,
                        fontWeight = if (index == step) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == step) colors.onSurface else colors.onSurfaceVariant,
                    )
                }
            }
        }
        CmpColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CmpText(
                text = stringResource(phase.statusRes()),
                style = CmpTextStyle.Title,
                textAlign = TextAlign.Center,
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
            color = colors.primary,
        )
        InfoBanner(text = stringResource(Strings.string.firmware_update_msg_power), icon = CmpIcons.PowerSettingsNew)
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

/** Успех: блок остался в загрузчике (подсказка по способу выйти зависит от связи) или уже вернулся в рабочий режим. */
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
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(RESULT_OK_TAG),
    )
}

@Composable
internal fun FinishingBody() {
    Headline(stringResource(Strings.string.firmware_update_title_finishing))
    Message(stringResource(Strings.string.firmware_update_msg_finishing))
    CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
}

@Composable
internal fun ErrorBody(failure: FailureKind, onRetry: () -> Unit, onSupport: (() -> Unit)?) {
    Headline(stringResource(Strings.string.firmware_update_error_title))
    InfoBanner(text = stringResource(failure.messageRes()), tone = BannerTone.Error, icon = CmpIcons.AlertCircle)
    CmpText(
        text = stringResource(Strings.string.firmware_update_error_msg),
        color = cmpThemeColors().onSurfaceVariant,
        style = CmpTextStyle.Body,
        textAlign = TextAlign.Center,
    )
    CmpButton(
        text = stringResource(Strings.string.firmware_common_btn_retry),
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_RETRY_TAG),
    )
    if (onSupport != null) {
        CmpOutlinedButton(
            text = stringResource(Strings.string.firmware_update_btn_support),
            onClick = onSupport,
            modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(ERROR_SUPPORT_TAG),
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
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(LOST_RECONNECT_TAG),
    )
    CmpOutlinedButton(
        text = stringResource(Strings.string.firmware_update_btn_disconnect),
        onClick = onDisconnect,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(LOST_DISCONNECT_TAG),
    )
}

/** «CAN 2154  профиль 1.03» или «Заводские CAN-настройки» под версией ПО на выборе источника. */
@Composable
private fun BlockCan(can: BlockCanUi) {
    val code = can.code
    if (code == null) {
        CmpText(text = stringResource(Strings.string.firmware_update_value_can_factory), fontSize = 17.sp)
        return
    }
    CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        VersionText(stringResource(Strings.string.firmware_archive_label_can, code), fontSize = 20.sp)
        can.profile?.let { profile ->
            CmpText(
                text = stringResource(Strings.string.firmware_archive_label_can_profile) + " " + profile,
                color = cmpThemeColors().onSurfaceVariant,
            )
        }
    }
}

/**
 * «CAN блока 2154 → будет 3312» с пояснением, что другой CAN допустим; при совпадении — «Версия собрана под CAN-код
 * блока». Информация перед установкой, не запрет.
 */
@Composable
private fun ConfirmCan(can: ConfirmCanUi) {
    val colors = cmpThemeColors()
    val content = if (can.same) colors.onSurface else colors.onSecondaryContainer
    CmpSurface(
        modifier = Modifier.fillMaxWidth().testTag(UPDATE_CONFIRM_CAN_TAG),
        shape = RoundedCornerShape(16.dp),
        color = if (can.same) colors.surfaceContainerLow else colors.secondaryContainer,
        border = if (can.same) BorderStroke(1.dp, colors.outlineVariant) else BorderStroke(2.dp, colors.primary),
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(text = stringResource(Strings.string.firmware_update_label_confirm_can), color = content)
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = can.blockCode ?: stringResource(
                            Strings.string.firmware_update_value_confirm_can_factory,
                        ),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = content,
                    )
                    if (!can.same) {
                        CmpIcon(
                            icon = CmpIcons.ArrowForward,
                            contentDescription = null,
                            tint = content,
                            modifier = Modifier.size(20.dp),
                        )
                        CmpText(
                            text = stringResource(Strings.string.firmware_update_label_confirm_can_will_be),
                            fontWeight = FontWeight.Medium,
                            color = content,
                        )
                        CmpText(text = can.newCode, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = content)
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
                style = CmpTextStyle.Body,
                color = content,
            )
        }
    }
}

internal const val UPDATE_CONFIRM_CAN_TAG = "firmware_update_confirm_can"
