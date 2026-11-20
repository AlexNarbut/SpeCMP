package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_label_no_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_subtitle_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_pick
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_replace
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_btn_skip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_format_problem
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_exists
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_imported
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_new
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_problems
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_reason_schema
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_reason_unreadable
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_reason_write
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_replaced
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_skipped
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_summary
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_summary_exists
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_unknown_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_with_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_label_zip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_import
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_import_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_importing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_not_archive
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_replace
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_skip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_duplicates
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_import_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_importing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_not_archive
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_title_reading
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportFailure
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ImportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferActions

private val DropMinHeight = 140.dp
private val ItemMinHeight = 64.dp
private const val SEPARATOR = " · "

/** Импорт: выбор файла → чтение → сводка с решением о дубликатах → импорт → итог; «не архив» и ошибки — плашкой. */
@Composable
internal fun ImportBody(phase: ImportPhase, actions: PassportTransferActions, modifier: Modifier = Modifier) {
    when (phase) {
        ImportPhase.Pick -> PickFile(actions.onPickFile, modifier)

        is ImportPhase.Reading -> TransferStatus(
            mark = StatusMark.PROGRESS,
            title = stringResource(Strings.string.mountpassport_transfer_title_reading),
            subtitle = phase.fileName,
            modifier = modifier,
        )

        is ImportPhase.NotArchive -> ErrorNote(
            title = stringResource(Strings.string.mountpassport_transfer_title_not_archive),
            text = stringResource(Strings.string.mountpassport_transfer_msg_not_archive, phase.fileName),
            modifier = modifier.padding(vertical = CmpSpacing.space8),
        )

        is ImportPhase.NothingToImport -> Column(
            modifier = modifier.padding(vertical = CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            ErrorNote(
                title = stringResource(Strings.string.mountpassport_transfer_title_nothing),
                text = stringResource(Strings.string.mountpassport_transfer_msg_nothing, phase.fileName),
            )
            Problems(phase.problems)
        }

        is ImportPhase.Failed -> ErrorNote(
            title = stringResource(Strings.string.mountpassport_transfer_title_import_error),
            text = stringResource(Strings.string.mountpassport_transfer_msg_import_error, phase.fileName),
            modifier = modifier.padding(vertical = CmpSpacing.space8),
        )

        is ImportPhase.Summary -> Summary(phase, actions.onReplaceChange, modifier)

        is ImportPhase.Importing -> TransferStatus(
            mark = StatusMark.PROGRESS,
            title = stringResource(Strings.string.mountpassport_transfer_title_importing),
            subtitle = pluralStringResource(
                Strings.plurals.mountpassport_transfer_msg_importing,
                phase.count,
                phase.count,
            ),
            modifier = modifier,
        )

        is ImportPhase.Done -> Done(phase, modifier)
    }
}

@Composable
private fun PickFile(onPick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_transfer_msg_import),
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DropMinHeight)
                .clip(shape)
                .border(CmpBorderWidth.focus, colors.borderTertiary, shape)
                .clickable(role = Role.Button, onClick = onPick)
                .padding(CmpSpacing.space16)
                .testTag(MountPassportTags.TRANSFER_PICK),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterVertically),
        ) {
            CmpIcon(
                icon = CmpIcons.Inventory2,
                contentDescription = null,
                tint = colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconXl),
            )
            CmpText(
                text = stringResource(Strings.string.mountpassport_transfer_btn_pick),
                style = CmpTextStyle.Headline3,
                color = colors.brandPrimaryDefault,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.mountpassport_transfer_label_zip),
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Summary(summary: ImportPhase.Summary, onReplaceChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(
                icon = CmpIcons.Inventory2,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
            CmpText(
                text = summary.fileName,
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(start = CmpSpacing.space8),
            )
        }
        val count = summary.items.size
        CmpText(
            text = if (summary.existing > 0) {
                pluralStringResource(
                    Strings.plurals.mountpassport_transfer_label_summary_exists,
                    count,
                    count,
                    summary.existing,
                )
            } else {
                pluralStringResource(Strings.plurals.mountpassport_transfer_label_summary, count, count)
            },
            style = CmpTextStyle.Headline2,
            color = colors.foregroundOnDarkPrimaryDefault,
        )
        val shape = RoundedCornerShape(CmpRadius.cardMedium)
        Column(
            modifier = Modifier.fillMaxWidth().clip(
                shape,
            ).border(CmpBorderWidth.listItem, colors.borderHighlight, shape),
        ) {
            summary.items.forEachIndexed { index, item ->
                if (index > 0) CmpDivider()
                ImportItemRow(item)
            }
        }
        Problems(summary.problems)
        if (summary.existing > 0) Duplicates(summary.replace, onReplaceChange)
    }
}

@Composable
private fun ImportItemRow(item: PassportImportItem) {
    val colors = CmpTheme.colors
    val tone = if (item.exists) colors.warningDefault else colors.successDefault
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ItemMinHeight)
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = if (item.exists) CmpIcons.ContentCopy else CmpIcons.Add, contentDescription = null, tint = tone)
        Column(modifier = Modifier.weight(1f)) {
            val car = item.carTitle ?: stringResource(Strings.string.mountpassport_common_label_no_car)
            CmpText(
                text = listOfNotNull(car, item.plate).joinToString(SEPARATOR),
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkPrimaryDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val number = stringResource(Strings.string.mountpassport_ready_subtitle_number, item.number)
            val settings = stringResource(Strings.string.mountpassport_transfer_label_with_settings)
            CmpText(
                text = listOfNotNull(number, settings.takeIf { item.hasSettings }).joinToString(SEPARATOR),
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        CmpText(
            text = stringResource(
                if (item.exists) {
                    Strings.string.mountpassport_transfer_label_exists
                } else {
                    Strings.string.mountpassport_transfer_label_new
                },
            ),
            style = CmpTextStyle.Additional0,
            color = if (item.exists) colors.warningDefault else colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun Duplicates(replace: Boolean, onReplaceChange: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_transfer_title_duplicates),
            style = CmpTextStyle.Headline3,
            color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
        )
        DecisionRow(
            selected = !replace,
            title = stringResource(Strings.string.mountpassport_transfer_btn_skip),
            text = stringResource(Strings.string.mountpassport_transfer_msg_skip),
            tag = MountPassportTags.TRANSFER_SKIP,
            onClick = { onReplaceChange(false) },
        )
        DecisionRow(
            selected = replace,
            title = stringResource(Strings.string.mountpassport_transfer_btn_replace),
            text = stringResource(Strings.string.mountpassport_transfer_msg_replace),
            tag = MountPassportTags.TRANSFER_REPLACE,
            onClick = { onReplaceChange(true) },
        )
    }
}

@Composable
private fun DecisionRow(selected: Boolean, title: String, text: String, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ItemMinHeight)
            .clip(RoundedCornerShape(CmpRadius.listItem))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpRadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            CmpText(
                text = title,
                style = CmpTextStyle.Headline3,
                color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
            )
            CmpText(
                text = text,
                style = CmpTextStyle.Paragraph3,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun Done(done: ImportPhase.Done, modifier: Modifier = Modifier) {
    val result = done.result
    val imported = result.imported + result.replaced
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        TransferStatus(
            mark = StatusMark.DONE,
            title = stringResource(Strings.string.mountpassport_transfer_title_done),
            subtitle = pluralStringResource(Strings.plurals.mountpassport_transfer_label_imported, imported, imported),
        )
        val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
        if (result.replaced > 0) {
            CmpText(
                text = stringResource(Strings.string.mountpassport_transfer_label_replaced, result.replaced),
                style = CmpTextStyle.Paragraph2,
                color = muted,
            )
        }
        if (result.skipped > 0) {
            CmpText(
                text = stringResource(Strings.string.mountpassport_transfer_label_skipped, result.skipped),
                style = CmpTextStyle.Paragraph2,
                color = muted,
            )
        }
        Problems(result.problems, Modifier.padding(top = CmpSpacing.space12))
    }
}

@Composable
private fun Problems(problems: List<PassportImportProblem>, modifier: Modifier = Modifier) {
    if (problems.isEmpty()) return
    val colors = CmpTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_transfer_label_problems),
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkPrimaryDefault,
        )
        problems.forEach { problem ->
            val number = problem.number ?: stringResource(Strings.string.mountpassport_transfer_label_unknown_number)
            CmpText(
                text = stringResource(
                    Strings.string.mountpassport_transfer_format_problem,
                    number,
                    problem.reason.label(),
                ),
                style = CmpTextStyle.Paragraph3,
                color = colors.errorDefault,
            )
        }
    }
}

@Composable
private fun PassportImportFailure.label(): String = stringResource(
    when (this) {
        PassportImportFailure.UNSUPPORTED_SCHEMA -> Strings.string.mountpassport_transfer_label_reason_schema
        PassportImportFailure.UNREADABLE -> Strings.string.mountpassport_transfer_label_reason_unreadable
        PassportImportFailure.WRITE_FAILED -> Strings.string.mountpassport_transfer_label_reason_write
    },
)
