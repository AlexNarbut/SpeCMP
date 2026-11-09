package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_all_versions
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_can_reset
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_install
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_pick_vehicle
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_refresh
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_reinstall
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_more
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_msg
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_error_title
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_format_can_tag
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can_panel
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_label_can_profile
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_loading
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_loading_more
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_none
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_note_factory
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_note_other
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_can_note_profile
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_empty_can_car
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_empty_can_device
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_empty_can_other
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_older
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_rollback
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_msg_same
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_can_other
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_can_same
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_empty
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_empty_can
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_title_rollback
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_dialog_full_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_dialog_no_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareCenteredBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLoadError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.RollbackUi

private val ButtonHeight = 48.dp
private val DetailDescriptionMaxHeight = 300.dp

internal const val ARCHIVE_INSTALL_TAG = "firmware_archive_install"
internal const val ARCHIVE_ROLLBACK_CONFIRM_TAG = "firmware_archive_rollback_confirm"
internal const val ARCHIVE_REFRESH_TAG = "firmware_archive_refresh"

@Composable
internal fun ArchiveLoading() {
    FirmwareCenteredBody {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Strings.string.firmware_archive_loading),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ArchiveEmpty(
    onRefresh: () -> Unit,
    filter: CanFilterUi? = null,
    onShowAll: () -> Unit = {},
    onPickVehicle: () -> Unit = {},
    onResetToDevice: () -> Unit = {},
) {
    val canCode = filter?.applied
    FirmwareCenteredBody {
        FirmwareHero(
            icon = if (canCode != null) CmpIcons.SearchOff else CmpIcons.Inventory2,
            tone = HeroTone.Neutral,
            size = 88.dp,
        )
        CmpText(
            text = canCode?.let { stringResource(Strings.string.firmware_archive_title_empty_can, it) }
                ?: stringResource(Strings.string.firmware_archive_title_empty),
            style = CmpTextStyle.Title,
            textAlign = TextAlign.Center,
        )
        if (filter == null || canCode == null) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_refresh),
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
            return@FirmwareCenteredBody
        }
        val carTitle = filter.carTitle
        CmpText(
            text = when {
                filter.appliedIsDevice -> stringResource(Strings.string.firmware_archive_msg_empty_can_device)
                carTitle != null -> stringResource(Strings.string.firmware_archive_msg_empty_can_car, carTitle)
                else -> stringResource(Strings.string.firmware_archive_msg_empty_can_other)
            },
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CmpButton(
            text = stringResource(Strings.string.firmware_archive_btn_all_versions),
            onClick = onShowAll,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
        when {
            filter.appliedIsDevice && filter.canPickVehicle -> CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_pick_vehicle),
                onClick = onPickVehicle,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )

            filter.canResetToDevice -> CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_can_reset),
                onClick = onResetToDevice,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
        }
    }
}

@Composable
internal fun ArchiveError(error: FirmwareError, onRetry: () -> Unit) {
    FirmwareLoadError(
        icon = CmpIcons.CloudOff,
        title = error.text(Strings.string.firmware_archive_error_title),
        message = stringResource(Strings.string.firmware_archive_error_msg),
        onRetry = onRetry,
    )
}

@Composable
internal fun ArchiveFooter(loadingMore: Boolean, moreError: FirmwareError?, onRetryMore: () -> Unit) {
    when {
        loadingMore -> CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            CmpText(
                text = stringResource(Strings.string.firmware_archive_loading_more),
                color = cmpThemeColors().onSurfaceVariant,
                style = CmpTextStyle.Body,
            )
        }

        moreError != null -> InfoBanner(
            text = moreError.text(Strings.string.firmware_archive_error_more),
            tone = BannerTone.Error,
            icon = CmpIcons.CloudOff,
            action = {
                CmpTextButton(
                    text = stringResource(Strings.string.firmware_common_btn_retry),
                    onClick = onRetryMore,
                    modifier = Modifier.height(ButtonHeight),
                )
            },
        )
    }
}

@Composable
internal fun ArchiveDetail(
    row: ArchiveRowUi,
    installedVersion: String?,
    onInstall: (() -> Unit)?,
    modifier: Modifier = Modifier,
    canFilter: CanFilterUi? = null,
    onDownload: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    busy: Boolean = false,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VersionText(row.version, fontSize = 34.sp)
                    VersionBadge(row.kind)
                }
                CmpText(text = formatArchiveDate(row.createdOn), color = colors.onSurfaceVariant)
            }
            canFilter?.let { DetailCan(row, it) }
            CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant,
                )
                CmpText(
                    text = row.description.ifBlank {
                        stringResource(Strings.string.firmware_check_msg_dialog_no_description)
                    },
                    modifier = Modifier
                        .heightIn(max = DetailDescriptionMaxHeight)
                        .verticalScroll(rememberScrollState()),
                )
            }
            if (onDownload != null) {
                DetailExportActions(busy, onDownload, onShare)
                return@CmpColumn
            }
            if (onInstall == null) return@CmpColumn
            when (row.kind) {
                ArchiveRowKind.Older -> installedVersion?.let {
                    InfoBanner(text = stringResource(Strings.string.firmware_archive_msg_older, it))
                }

                ArchiveRowKind.Installed -> InfoBanner(
                    text = stringResource(Strings.string.firmware_archive_msg_same),
                    tone = BannerTone.Ok,
                    icon = CmpIcons.Info,
                )

                ArchiveRowKind.New -> Unit
            }
            CmpButton(
                text = stringResource(
                    if (row.kind == ArchiveRowKind.Installed) {
                        Strings.string.firmware_archive_btn_reinstall
                    } else {
                        Strings.string.firmware_archive_btn_install
                    },
                ),
                onClick = onInstall,
                modifier = Modifier.widthIn(min = 240.dp).height(52.dp).testTag(ARCHIVE_INSTALL_TAG),
            )
        }
    }
}

@Composable
internal fun RollbackDialog(rollback: RollbackUi, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.Warning.imageVector(),
        title = { CmpText(text = stringResource(Strings.string.firmware_archive_title_rollback)) },
        text = {
            CmpText(
                text = stringResource(
                    Strings.string.firmware_archive_msg_rollback,
                    rollback.version,
                    rollback.installedVersion,
                ),
                style = CmpTextStyle.Body,
            )
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight),
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_install),
                onClick = onConfirm,
                modifier = Modifier.height(ButtonHeight).testTag(ARCHIVE_ROLLBACK_CONFIRM_TAG),
            )
        },
    )
}

@Composable
private fun DetailCan(row: ArchiveRowUi, filter: CanFilterUi) {
    val colors = cmpThemeColors()
    val deviceCode = filter.deviceCode
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerHigh,
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpText(
                text = stringResource(Strings.string.firmware_archive_label_can_panel),
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
            )
            val code = row.canCode
            if (code == null) {
                CmpText(text = stringResource(Strings.string.firmware_archive_msg_can_none))
                return@CmpColumn
            }
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_label_can, code),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                row.canVersion?.let { profile ->
                    CmpText(
                        text = stringResource(Strings.string.firmware_archive_label_can_profile) + " " + profile,
                        color = colors.onSurfaceVariant,
                    )
                }
                if (deviceCode != null) CanPill(row.canMatchesDevice)
            }
            val note = when {
                filter.deviceCode == null && filter.deviceFactory ->
                    stringResource(Strings.string.firmware_archive_msg_can_note_factory, code)

                deviceCode != null && !row.canMatchesDevice -> stringResource(
                    Strings.string.firmware_archive_msg_can_note_other,
                    canLabel(deviceCode, filter.deviceProfile),
                    canLabel(code, row.canVersion),
                )

                row.canProfileDiffers && filter.deviceProfile != null ->
                    stringResource(Strings.string.firmware_archive_msg_can_note_profile, filter.deviceProfile)

                else -> null
            }
            note?.let { CmpText(text = it, style = CmpTextStyle.Body, color = colors.onSurfaceVariant) }
        }
    }
}

@Composable
private fun canLabel(code: String, profile: String?): String =
    profile?.let { stringResource(Strings.string.firmware_archive_format_can_tag, code, it) }
        ?: stringResource(Strings.string.firmware_archive_label_can, code)

@Composable
private fun CanPill(mine: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(
        shape = RoundedCornerShape(percent = PILL_PERCENT),
        color = if (mine) colors.primary else Color.Transparent,
        border = if (mine) null else BorderStroke(1.dp, colors.outline),
    ) {
        CmpText(
            text = if (mine) {
                stringResource(Strings.string.firmware_archive_status_can_same)
            } else {
                stringResource(Strings.string.firmware_archive_status_can_other)
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (mine) colors.onPrimary else colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

private const val PILL_PERCENT = 50
