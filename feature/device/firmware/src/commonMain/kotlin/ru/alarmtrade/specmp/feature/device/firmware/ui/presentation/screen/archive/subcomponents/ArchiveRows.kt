package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_download
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_more
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_select
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_share
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_installed
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_older
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_dialog_full_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_dialog_no_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_close
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi

private val ButtonHeight = 48.dp
private const val COLLAPSED_LINES = 2
private const val DAY_MONTH_DIGITS = 2
private const val YEAR_DIGITS = 4

internal fun archiveItemTag(key: String): String = "firmware_archive_item_$key"

internal fun archiveMoreTag(key: String): String = "firmware_archive_more_$key"

internal fun archiveSelectTag(key: String): String = "firmware_archive_select_$key"

/** Дата как её отдаёт сервер, в виде `дд.мм.гггг` (не зависит от языка приложения). */
internal fun formatArchiveDate(date: LocalDate): String =
    "${date.dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
        "${date.monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
        date.year.toString().padStart(YEAR_DIGITS, '0')

@Composable
internal fun VersionBadge(kind: ArchiveRowKind, modifier: Modifier = Modifier) {
    if (kind == ArchiveRowKind.New) return
    val colors = cmpThemeColors()
    val installed = kind == ArchiveRowKind.Installed
    CmpSurface(
        modifier = modifier.height(26.dp),
        shape = CircleShape,
        color = if (installed) colors.primary else colors.surfaceVariant,
        contentColor = if (installed) colors.onPrimary else colors.onSurfaceVariant,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (installed) CmpIcon(icon = CmpIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            CmpText(
                text = stringResource(
                    if (installed) {
                        Strings.string.firmware_archive_status_installed
                    } else {
                        Strings.string.firmware_archive_status_older
                    },
                ),
                style = CmpTextStyle.Additional1,
                maxLines = 1,
            )
        }
    }
}

/** Нажатие по «Показать больше» версию не выбирает; [onPick] = `null` — просмотр без выбора. */
@Composable
internal fun ArchiveRow(
    row: ArchiveRowUi,
    master: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    onPick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onDownload: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    busy: Boolean = false,
) {
    val colors = cmpThemeColors()
    var details by remember(row.key) { mutableStateOf(false) }
    val highlighted = selected || row.kind == ArchiveRowKind.Installed
    CmpClickableSurface(
        onClick = onSelect,
        enabled = master,
        modifier = modifier.fillMaxWidth().testTag(archiveItemTag(row.key)),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = if (highlighted) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VersionText(
                        row.version,
                        style = CmpTextStyle.Headline1,
                        color = if (selected) colors.onPrimaryContainer else colors.primary,
                    )
                    VersionBadge(row.kind)
                }
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    style = CmpTextStyle.Paragraph3,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            ArchiveRowCan(row)
            if (row.description.isNotBlank()) {
                CmpText(
                    text = row.description,
                    style = CmpTextStyle.Paragraph2,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = COLLAPSED_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RowActions(
                row = row,
                master = master,
                onDetails = { details = true },
                onPick = onPick,
                export = onDownload?.let { download -> { RowExportActions(row.key, busy, download, onShare) } },
            )
        }
    }
    if (details) {
        ArchiveVersionDialog(
            row = row,
            onDismiss = { details = false },
            onPick = onPick?.let { pick ->
                {
                    details = false
                    pick()
                }
            },
            onDownload = onDownload?.let { download ->
                {
                    details = false
                    download()
                }
            },
            onShare = onShare?.let { share ->
                {
                    details = false
                    share()
                }
            },
            busy = busy,
        )
    }
}

@Composable
private fun RowActions(
    row: ArchiveRowUi,
    master: Boolean,
    onDetails: () -> Unit,
    onPick: (() -> Unit)?,
    export: (@Composable () -> Unit)? = null,
) {
    val hasDescription = row.description.isNotBlank()
    val showExport = !master && export != null
    val showPick = !master && onPick != null && !showExport
    if (!hasDescription && !showPick && !showExport) return
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasDescription) {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_archive_btn_more),
                onClick = onDetails,
                modifier = Modifier.height(ButtonHeight).testTag(archiveMoreTag(row.key)),
            )
        } else {
            CmpColumn {}
        }
        if (showExport && export != null) export()
        if (showPick && onPick != null) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_select),
                onClick = onPick,
                modifier = Modifier.height(ButtonHeight).testTag(archiveSelectTag(row.key)),
            )
        }
    }
}

@Composable
private fun ArchiveVersionDialog(
    row: ArchiveRowUi,
    onDismiss: () -> Unit,
    onPick: (() -> Unit)?,
    onDownload: (() -> Unit)?,
    onShare: (() -> Unit)?,
    busy: Boolean,
) {
    val colors = cmpThemeColors()
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(ARCHIVE_VERSION_DIALOG_TAG),
        width = CmpDialogWidth.Wide,
        title = {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VersionText(row.version, style = CmpTextStyle.Headline1)
                VersionBadge(row.kind)
            }
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.onSurfaceVariant,
                )
                ArchiveRowCan(row)
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.onSurfaceVariant,
                )
                CmpText(
                    text = row.description.ifBlank {
                        stringResource(Strings.string.firmware_check_msg_dialog_no_description)
                    },
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier
                        .heightIn(max = DialogDescriptionMaxHeight)
                        .verticalScroll(rememberScrollState()),
                )
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_common_btn_close),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight),
            )
        },
        confirmButton = {
            when {
                onDownload != null -> CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onShare?.let {
                        CmpTextButton(
                            text = stringResource(Strings.string.firmware_archive_btn_share),
                            onClick = it,
                            enabled = !busy,
                            modifier = Modifier.height(ButtonHeight),
                        )
                    }
                    CmpButton(
                        text = stringResource(Strings.string.firmware_archive_btn_download),
                        onClick = onDownload,
                        enabled = !busy,
                        modifier = Modifier.height(ButtonHeight),
                    )
                }

                onPick != null -> CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_select),
                    onClick = onPick,
                    modifier = Modifier.height(ButtonHeight),
                )

                else -> Unit
            }
        },
    )
}

internal const val ARCHIVE_VERSION_DIALOG_TAG = "firmware_archive_version"

private val DialogDescriptionMaxHeight = 420.dp
