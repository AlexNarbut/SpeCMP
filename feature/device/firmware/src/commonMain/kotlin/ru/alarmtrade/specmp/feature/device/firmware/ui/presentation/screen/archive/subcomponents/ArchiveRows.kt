package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi

private val HighlightBorder = 2.dp
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
    when (kind) {
        ArchiveRowKind.New -> Unit

        ArchiveRowKind.Installed -> CmpTag(
            text = stringResource(Strings.string.firmware_archive_status_installed),
            modifier = modifier,
            tone = CmpTagTone.Brand,
            icon = CmpIcons.Check,
        )

        ArchiveRowKind.Older -> CmpTag(
            text = stringResource(Strings.string.firmware_archive_status_older),
            modifier = modifier,
            tone = CmpTagTone.Neutral,
        )
    }
}

@Composable
private fun RowFrame(
    master: Boolean,
    selected: Boolean,
    highlighted: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    val border = if (highlighted) {
        BorderStroke(HighlightBorder, colors.brandPrimaryDefault)
    } else {
        BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight)
    }
    val container = if (selected) colors.brandPrimaryHighlight else colors.backgroundSurfaceCard
    if (master) {
        CmpClickableCard(
            onClick = onSelect,
            modifier = modifier,
            border = border,
            containerColor = container,
            content = content,
        )
    } else {
        CmpCard(modifier = modifier, border = border, containerColor = container, content = content)
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
    val colors = CmpTheme.colors
    var details by remember(row.key) { mutableStateOf(false) }
    RowFrame(
        master = master,
        selected = selected,
        highlighted = selected || row.kind == ArchiveRowKind.Installed,
        onSelect = onSelect,
        modifier = modifier.fillMaxWidth().testTag(archiveItemTag(row.key)),
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpFlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    VersionText(row.version, style = CmpTextStyle.Headline1)
                    VersionBadge(row.kind)
                }
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    style = CmpTextStyle.Additional0,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
            ArchiveRowCan(row)
            if (row.description.isNotBlank()) {
                RowDescription(row, onDetails = { details = true })
            }
            if (!master) {
                when {
                    onDownload != null -> RowExportActions(row.key, busy, onDownload, onShare)

                    onPick != null -> CmpButton(
                        text = stringResource(Strings.string.firmware_archive_btn_select),
                        onClick = onPick,
                        type = CmpButtonType.Secondary,
                        size = CmpButtonSize.S,
                        fullWidth = true,
                        modifier = Modifier.testTag(archiveSelectTag(row.key)),
                    )
                }
            }
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
private fun RowDescription(row: ArchiveRowUi, onDetails: () -> Unit) {
    CmpColumn {
        CmpText(
            text = row.description,
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            maxLines = COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        CmpButton(
            text = stringResource(Strings.string.firmware_archive_btn_more),
            onClick = onDetails,
            type = CmpButtonType.Tertiary,
            size = CmpButtonSize.S,
            modifier = Modifier.testTag(archiveMoreTag(row.key)),
        )
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
    val colors = CmpTheme.colors
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(ARCHIVE_VERSION_DIALOG_TAG),
        width = CmpDialogWidth.Wide,
        title = {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                VersionText(row.version, style = CmpTextStyle.Title2)
                VersionBadge(row.kind)
            }
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    style = CmpTextStyle.Additional0,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
                ArchiveRowCan(row)
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    style = CmpTextStyle.Headline3,
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
            CmpButton(
                text = stringResource(Strings.string.firmware_common_btn_close),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            when {
                onDownload != null -> CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    onShare?.let {
                        CmpButton(
                            text = stringResource(Strings.string.firmware_archive_btn_share),
                            onClick = it,
                            enabled = !busy,
                            type = CmpButtonType.Tertiary,
                            size = CmpButtonSize.S,
                        )
                    }
                    CmpButton(
                        text = stringResource(Strings.string.firmware_archive_btn_download),
                        onClick = onDownload,
                        enabled = !busy,
                        loading = busy,
                        size = CmpButtonSize.S,
                    )
                }

                onPick != null -> CmpButton(
                    text = stringResource(Strings.string.firmware_archive_btn_select),
                    onClick = onPick,
                    size = CmpButtonSize.S,
                )

                else -> Unit
            }
        },
    )
}

internal const val ARCHIVE_VERSION_DIALOG_TAG = "firmware_archive_version"

private val DialogDescriptionMaxHeight = 420.dp
