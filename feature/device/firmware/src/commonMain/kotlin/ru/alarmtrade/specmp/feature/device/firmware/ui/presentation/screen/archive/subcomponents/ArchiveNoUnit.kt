package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.ChannelChip
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveExportError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveSavedUi

private val ButtonHeight = 48.dp
private val CONTEXT_ONE_ROW_MIN_WIDTH = 420.dp

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
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        // Узко — канал переносится под подсистему (ей нужна вся ширина), широко — в одну строку.
        BoxWithConstraints {
            if (maxWidth < CONTEXT_ONE_ROW_MIN_WIDTH) {
                CmpColumn(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModelButton(system, subsystem, systemName, onChangeModel, Modifier.fillMaxWidth())
                    ChannelChip(channel, onChannelSelect, Modifier.padding(start = 4.dp, bottom = 4.dp))
                }
            } else {
                CmpRow(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    val colors = cmpThemeColors()
    val a11y = stringResource(Strings.string.firmware_archive_a11y_change_model)
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp).testTag(ARCHIVE_CHANGE_MODEL_TAG),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.Memory,
                contentDescription = a11y,
                tint = colors.primary,
                modifier = Modifier.size(26.dp),
            )
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(text = system, fontSize = 13.sp, color = colors.onSurfaceVariant)
                CmpText(text = subsystem, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                systemName?.takeIf { it != subsystem }?.let {
                    CmpText(text = it, fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
            }
            CmpText(
                text = stringResource(Strings.string.firmware_archive_btn_change_model),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
            )
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
internal fun RowExportActions(key: String, busy: Boolean, onDownload: () -> Unit, onShare: (() -> Unit)?) {
    CmpRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onShare != null) {
            CmpIconButton(
                onClick = onShare,
                icon = CmpIcons.Share,
                contentDescription = stringResource(Strings.string.firmware_archive_btn_share),
                enabled = !busy,
                modifier = Modifier.testTag(archiveShareTag(key)),
            )
        }
        CmpOutlinedButton(
            text = downloadLabel(busy),
            onClick = onDownload,
            enabled = !busy,
            modifier = Modifier.height(ButtonHeight).testTag(archiveDownloadTag(key)),
        )
    }
}

@Composable
internal fun DetailExportActions(busy: Boolean, onDownload: () -> Unit, onShare: (() -> Unit)?) {
    CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpButton(
            text = downloadLabel(busy),
            onClick = onDownload,
            enabled = !busy,
            modifier = Modifier.widthIn(min = 200.dp).height(52.dp),
        )
        if (onShare != null) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_share),
                onClick = onShare,
                enabled = !busy,
                modifier = Modifier.widthIn(min = 180.dp).height(52.dp),
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
        CmpColumn(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FirmwareHero(icon = CmpIcons.Inventory2, tone = HeroTone.Neutral, size = 88.dp)
            CmpText(
                text = stringResource(Strings.string.firmware_archive_title_no_model_firmware, model),
                style = CmpTextStyle.Title,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.firmware_archive_msg_no_model_firmware),
                color = cmpThemeColors().onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_other_model),
                onClick = onChangeModel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
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
    InfoBanner(
        text = text,
        tone = BannerTone.Error,
        icon = CmpIcons.ErrorOutline,
        modifier = modifier,
        action = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight),
            )
        },
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
    val colors = cmpThemeColors()
    val primary: (@Composable () -> Unit)? = when {
        canOpenFolder -> {
            {
                CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_open_folder),
                    onClick = onOpenFolder,
                )
            }
        }

        canShare -> {
            { CmpButton(text = stringResource(Strings.string.firmware_archive_btn_share), onClick = onShare) }
        }

        else -> null
    }
    val done: @Composable () -> Unit = {
        CmpTextButton(
            text = stringResource(Strings.string.firmware_archive_btn_done),
            onClick = onDismiss,
            modifier = Modifier.height(ButtonHeight),
        )
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(ARCHIVE_SAVED_DIALOG_TAG),
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.TaskAlt.imageVector(),
        title = { CmpText(text = stringResource(Strings.string.firmware_archive_title_saved)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpText(
                    text = stringResource(
                        when (target) {
                            FirmwareSaveTarget.Downloads -> Strings.string.firmware_archive_msg_saved_downloads
                            FirmwareSaveTarget.Folder -> Strings.string.firmware_archive_msg_saved_folder
                            FirmwareSaveTarget.Chosen -> Strings.string.firmware_archive_msg_saved_chosen
                        },
                    ),
                    style = CmpTextStyle.Body,
                )
                if (target == FirmwareSaveTarget.Folder) {
                    saved.folder?.let { CmpText(text = it, fontSize = 14.sp, color = colors.onSurfaceVariant) }
                }
                CmpSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceContainerHighest,
                ) {
                    CmpText(
                        text = saved.fileName,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_msg_saved_hint),
                    style = CmpTextStyle.Body,
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
