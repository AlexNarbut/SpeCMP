package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareCenteredBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLoadError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.RollbackUi

private val InstallMinWidth = 240.dp
private val SpinnerStroke = 2.5.dp
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
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
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
    val carTitle = filter?.carTitle
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        CmpStateBox(
            icon = if (canCode != null) CmpIcons.SearchOff else CmpIcons.Inventory2,
            title = canCode?.let { stringResource(Strings.string.firmware_archive_title_empty_can, it) }
                ?: stringResource(Strings.string.firmware_archive_title_empty),
            text = when {
                filter == null || canCode == null -> null
                filter.appliedIsDevice -> stringResource(Strings.string.firmware_archive_msg_empty_can_device)
                carTitle != null -> stringResource(Strings.string.firmware_archive_msg_empty_can_car, carTitle)
                else -> stringResource(Strings.string.firmware_archive_msg_empty_can_other)
            },
        ) {
            if (filter == null || canCode == null) {
                CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_refresh),
                    onClick = onRefresh,
                    type = CmpButtonType.Secondary,
                    icon = CmpIcons.Refresh,
                    fullWidth = true,
                )
                return@CmpStateBox
            }
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_all_versions),
                onClick = onShowAll,
                icon = CmpIcons.FilterAltOff,
                fullWidth = true,
            )
            when {
                filter.appliedIsDevice && filter.canPickVehicle -> CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_pick_vehicle),
                    onClick = onPickVehicle,
                    type = CmpButtonType.Secondary,
                    icon = CmpIcons.DirectionsCar,
                    fullWidth = true,
                )

                filter.canResetToDevice -> CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_can_reset),
                    onClick = onResetToDevice,
                    type = CmpButtonType.Secondary,
                    icon = CmpIcons.Replay,
                    fullWidth = true,
                )
            }
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
            modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.input),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), strokeWidth = SpinnerStroke)
            CmpText(
                text = stringResource(Strings.string.firmware_archive_loading_more),
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
            )
        }

        moreError != null -> InfoBanner(
            text = moreError.text(Strings.string.firmware_archive_error_more),
            tone = BannerTone.Error,
            icon = CmpIcons.CloudOff,
            action = {
                CmpButton(
                    text = stringResource(Strings.string.firmware_common_btn_retry),
                    onClick = onRetryMore,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
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
    val colors = CmpTheme.colors
    CmpCard(modifier = modifier.fillMaxWidth()) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space24),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpFlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    VersionText(row.version, style = CmpTextStyle.Title1)
                    VersionBadge(row.kind)
                }
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    style = CmpTextStyle.Additional0,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
            canFilter?.let { DetailCan(row, it) }
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    style = CmpTextStyle.Headline3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
                CmpText(
                    text = row.description.ifBlank {
                        stringResource(Strings.string.firmware_check_msg_dialog_no_description)
                    },
                    style = CmpTextStyle.Paragraph2,
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
                icon = CmpIcons.SystemUpdate,
                modifier = Modifier.widthIn(min = InstallMinWidth).testTag(ARCHIVE_INSTALL_TAG),
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
        title = {
            CmpText(
                text = stringResource(Strings.string.firmware_archive_title_rollback),
                style = CmpTextStyle.Headline1,
            )
        },
        text = {
            CmpText(
                text = stringResource(
                    Strings.string.firmware_archive_msg_rollback,
                    rollback.version,
                    rollback.installedVersion,
                ),
                style = CmpTextStyle.Paragraph2,
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_archive_btn_install),
                onClick = onConfirm,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(ARCHIVE_ROLLBACK_CONFIRM_TAG),
            )
        },
    )
}

@Composable
private fun DetailCan(row: ArchiveRowUi, filter: CanFilterUi) {
    val colors = CmpTheme.colors
    val deviceCode = filter.deviceCode
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = colors.backgroundSurfaceHighlight,
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.firmware_archive_label_can_panel),
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
            val code = row.canCode
            if (code == null) {
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_msg_can_none),
                    style = CmpTextStyle.Paragraph2,
                )
                return@CmpColumn
            }
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.firmware_archive_label_can, code),
                    style = CmpTextStyle.Headline1,
                )
                row.canVersion?.let { profile ->
                    CmpText(
                        text = stringResource(Strings.string.firmware_archive_label_can_profile) + " " + profile,
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
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
            note?.let {
                CmpText(text = it, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault)
            }
        }
    }
}

@Composable
private fun canLabel(code: String, profile: String?): String =
    profile?.let { stringResource(Strings.string.firmware_archive_format_can_tag, code, it) }
        ?: stringResource(Strings.string.firmware_archive_label_can, code)

@Composable
private fun CanPill(mine: Boolean) {
    CmpTag(
        text = stringResource(
            if (mine) {
                Strings.string.firmware_archive_status_can_same
            } else {
                Strings.string.firmware_archive_status_can_other
            },
        ),
        tone = if (mine) CmpTagTone.Brand else CmpTagTone.Outline,
        icon = if (mine) CmpIcons.Link else null,
    )
}
