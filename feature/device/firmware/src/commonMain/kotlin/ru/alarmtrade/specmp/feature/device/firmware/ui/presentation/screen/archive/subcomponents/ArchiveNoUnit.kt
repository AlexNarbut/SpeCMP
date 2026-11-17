package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_a11y_change_model
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_change_model
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_done
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_open_folder
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_other_model
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_share
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_permission
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_save
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_share
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_loading_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_no_model_firmware
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_saved_chosen
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_saved_downloads
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_saved_folder
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_saved_hint
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_no_model_firmware
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_saved
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_cancel
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.ChannelChip
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveExportError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveSavedUi

private val CONTEXT_ONE_ROW_MIN_WIDTH = 420.dp
private val DownloadMinWidth = 200.dp
private val ShareMinWidth = 180.dp

internal const val ARCHIVE_CHANGE_MODEL_TAG = "firmware_archive_change_model"
internal const val ARCHIVE_SAVED_DIALOG_TAG = "firmware_archive_saved"

internal fun archiveDownloadTag(key: String): String = "firmware_archive_download_$key"

internal fun archiveShareTag(key: String): String = "firmware_archive_share_$key"

@Composable
internal fun ArchiveNoUnitContext(
    system: String,
    subsystem: String,
    systemName: String?,
    channel: FirmwareChannel,
    onChangeModel: () -> Unit,
    onChannelSelect: (FirmwareChannel) -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpCard(modifier = modifier.fillMaxWidth()) {
        // Узко — канал переносится под подсистему (ей нужна вся ширина), широко — в одну строку.
        BoxWithConstraints {
            if (maxWidth < CONTEXT_ONE_ROW_MIN_WIDTH) {
                CmpColumn(
                    modifier = Modifier.padding(CmpSpacing.space8),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    ModelButton(system, subsystem, systemName, onChangeModel, Modifier.fillMaxWidth())
                    ChannelChip(channel, onChannelSelect, Modifier.padding(start = CmpSpacing.space4))
                }
            } else {
                CmpRow(
                    modifier = Modifier.padding(CmpSpacing.space8),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModelButton(system, subsystem, systemName, onChangeModel, Modifier.weight(1f))
                    ChannelChip(channel, onChannelSelect)
                }
            }
        }
    }
}

@Composable
private fun ModelButton(
    system: String,
    subsystem: String,
    systemName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val a11y = stringResource(Strings.string.firmware_archive_a11y_change_model)
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = CmpSize.input).testTag(ARCHIVE_CHANGE_MODEL_TAG),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = Color.Transparent,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = CmpSpacing.space8, vertical = CmpSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.Memory,
                contentDescription = a11y,
                tint = colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconL),
            )
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(
                    text = system,
                    style = CmpTextStyle.Additional1,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
                CmpText(text = subsystem, style = CmpTextStyle.Headline2)
                systemName?.takeIf { it != subsystem }?.let {
                    CmpText(
                        text = it,
                        style = CmpTextStyle.Additional1,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
            CmpText(
                text = stringResource(Strings.string.firmware_archive_btn_change_model),
                style = CmpTextStyle.Headline3,
                color = colors.brandPrimaryDefault,
            )
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }
    }
}

@Composable
internal fun RowExportActions(key: String, busy: Boolean, onDownload: () -> Unit, onShare: (() -> Unit)?) {
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onShare != null) {
            CmpIconButton(
                onClick = onShare,
                icon = CmpIcons.Share,
                contentDescription = stringResource(Strings.string.firmware_archive_btn_share),
                enabled = !busy,
                modifier = Modifier.testTag(archiveShareTag(key)),
            )
        }
        CmpButton(
            text = downloadLabel(busy),
            onClick = onDownload,
            enabled = !busy,
            type = CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            icon = CmpIcons.Download,
            modifier = Modifier.testTag(archiveDownloadTag(key)),
        )
    }
}

@Composable
internal fun DetailExportActions(busy: Boolean, onDownload: () -> Unit, onShare: (() -> Unit)?) {
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpButton(
            text = downloadLabel(busy),
            onClick = onDownload,
            enabled = !busy,
            loading = busy,
            icon = CmpIcons.Download,
            modifier = Modifier.widthIn(min = DownloadMinWidth),
        )
        if (onShare != null) {
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_share),
                onClick = onShare,
                enabled = !busy,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Share,
                modifier = Modifier.widthIn(min = ShareMinWidth),
            )
        }
    }
}

@Composable
internal fun ArchiveNoModelFirmware(model: String, onChangeModel: () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        CmpStateBox(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.firmware_archive_title_no_model_firmware, model),
            text = stringResource(Strings.string.firmware_archive_msg_no_model_firmware),
        ) {
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_other_model),
                onClick = onChangeModel,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.SwapHoriz,
                fullWidth = true,
            )
        }
    }
}

@Composable
internal fun ArchiveExportBanner(error: ArchiveExportError, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val text = when (error) {
        is ArchiveExportError.Download -> error.error.text(Strings.string.firmware_archive_error_download)
        ArchiveExportError.Permission -> stringResource(Strings.string.firmware_archive_error_permission)
        ArchiveExportError.Save -> stringResource(Strings.string.firmware_archive_error_save)
        ArchiveExportError.Share -> stringResource(Strings.string.firmware_archive_error_share)
    }
    CmpBanner(
        text = text,
        tone = CmpBannerTone.Error,
        icon = CmpIcons.ErrorOutline,
        modifier = modifier,
        actionLabel = stringResource(Strings.string.firmware_common_btn_cancel),
        onAction = onDismiss,
    )
}

@Composable
internal fun ArchiveSavedDialog(
    saved: ArchiveSavedUi,
    target: FirmwareSaveTarget,
    canShare: Boolean,
    canOpenFolder: Boolean,
    onShare: () -> Unit,
    onOpenFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = CmpTheme.colors
    val primary: (@Composable () -> Unit)? = when {
        canOpenFolder -> {
            {
                CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_open_folder),
                    onClick = onOpenFolder,
                    size = CmpButtonSize.S,
                )
            }
        }

        canShare -> {
            {
                CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_share),
                    onClick = onShare,
                    size = CmpButtonSize.S,
                )
            }
        }

        else -> null
    }
    val done: @Composable () -> Unit = {
        CmpButton(
            text = stringResource(Strings.string.firmware_archive_btn_done),
            onClick = onDismiss,
            type = CmpButtonType.Tertiary,
            size = CmpButtonSize.S,
        )
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(ARCHIVE_SAVED_DIALOG_TAG),
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.TaskAlt.imageVector(),
        title = {
            CmpText(text = stringResource(Strings.string.firmware_archive_title_saved), style = CmpTextStyle.Headline1)
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpText(
                    text = stringResource(
                        when (target) {
                            FirmwareSaveTarget.Downloads -> Strings.string.firmware_archive_msg_saved_downloads
                            FirmwareSaveTarget.Folder -> Strings.string.firmware_archive_msg_saved_folder
                            FirmwareSaveTarget.Chosen -> Strings.string.firmware_archive_msg_saved_chosen
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                )
                if (target == FirmwareSaveTarget.Folder) {
                    saved.folder?.let {
                        CmpText(
                            text = it,
                            style = CmpTextStyle.Paragraph3,
                            color = colors.foregroundOnDarkSecondaryDefault,
                        )
                    }
                }
                CmpSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CmpRadius.cardSmall),
                    color = colors.backgroundSurfaceHighlight,
                ) {
                    CmpText(
                        text = saved.fileName,
                        style = CmpTextStyle.Headline3,
                        modifier = Modifier.padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space12),
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_msg_saved_hint),
                    style = CmpTextStyle.Paragraph2,
                )
            }
        },
        dismissButton = if (primary != null) done else null,
        confirmButton = primary ?: done,
    )
}

@Composable
private fun downloadLabel(busy: Boolean): String = if (busy) {
    stringResource(Strings.string.firmware_archive_loading_download)
} else {
    stringResource(Strings.string.firmware_archive_btn_download)
}
