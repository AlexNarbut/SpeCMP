package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_engine_block
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_keyless
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_a11y_copy
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_btn_archive
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_label_can
import ru.alarmtrade.specmp.core.localization.resources.vehicle_card_label_years_trim
import ru.alarmtrade.specmp.core.localization.resources.vehicle_notes_btn_collapse
import ru.alarmtrade.specmp.core.localization.resources.vehicle_notes_btn_expand
import ru.alarmtrade.specmp.core.localization.resources.vehicle_notes_btn_expand_count
import ru.alarmtrade.specmp.core.localization.resources.vehicle_notes_title_important
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpHtmlText
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CapBadge
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CapTileMinHeight
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CarPhoto
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PHOTO_RATIO
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.yearsText
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CapabilityKind
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions

private const val NOTE_COLLAPSE_CHARS = 140
private const val NOTE_COLLAPSED_LINES = 3

private val PhotoWidthWide = 200.dp
private val PhotoWidthCompact = 120.dp
private val CanStripMinHeight = 64.dp

@Composable
internal fun CardHeader(
    card: CarCard,
    wide: Boolean,
    notesExpanded: Boolean,
    actions: CardActions,
    modifier: Modifier = Modifier,
) {
    val summary = card.summary
    val colors = CmpTheme.colors
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarPhoto(
                url = summary.imageUrl,
                modifier = Modifier.width(if (wide) PhotoWidthWide else PhotoWidthCompact).aspectRatio(PHOTO_RATIO),
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = "${summary.brand} ${summary.model}",
                    style = if (wide) CmpTextStyle.Title2 else CmpTextStyle.Headline1,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
                CmpText(
                    text = stringResource(
                        Strings.string.vehicle_card_label_years_trim,
                        yearsText(summary.years),
                        summary.name,
                    ),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        val engine = stringResource(Strings.string.vehicle_cap_label_engine_block)
        val keyless = stringResource(Strings.string.vehicle_cap_label_keyless)
        if (wide) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CanCode(
                    code = summary.canCode,
                    tile = true,
                    onCopy = actions.onCopyCanCode,
                    modifier = Modifier.weight(1f),
                )
                CapBadge(
                    label = engine,
                    capability = card.engineBlockByCan,
                    onNote = { actions.onCapabilityNote(CapabilityKind.ENGINE_BLOCK) },
                    tile = true,
                    modifier = Modifier.weight(1f).testTag(VehicleTestTags.capability("lock")),
                )
                CapBadge(
                    label = keyless,
                    capability = card.keylessBypass,
                    onNote = { actions.onCapabilityNote(CapabilityKind.KEYLESS_BYPASS) },
                    tile = true,
                    modifier = Modifier.weight(1f).testTag(VehicleTestTags.capability("keyless")),
                )
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CanCode(
                    code = summary.canCode,
                    tile = false,
                    onCopy = actions.onCopyCanCode,
                    modifier = Modifier.fillMaxWidth(),
                )
                CapBadge(
                    label = engine,
                    capability = card.engineBlockByCan,
                    onNote = { actions.onCapabilityNote(CapabilityKind.ENGINE_BLOCK) },
                    modifier = Modifier.fillMaxWidth().testTag(VehicleTestTags.capability("lock")),
                )
                CapBadge(
                    label = keyless,
                    capability = card.keylessBypass,
                    onNote = { actions.onCapabilityNote(CapabilityKind.KEYLESS_BYPASS) },
                    modifier = Modifier.fillMaxWidth().testTag(VehicleTestTags.capability("keyless")),
                )
            }
        }
        actions.onOpenFirmwareArchive?.let { onArchive ->
            CmpButton(
                text = stringResource(Strings.string.vehicle_card_btn_archive, summary.canCode),
                onClick = { onArchive(summary.canCode) },
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                icon = CmpIcons.History,
                maxLines = 2,
                uppercase = false,
            )
        }
        if (card.notes.isNotEmpty()) {
            Notes(
                notes = card.notes,
                expanded = notesExpanded,
                onToggle = actions.onNotesToggle,
            )
        }
    }
}

@Composable
private fun CanCode(code: String, tile: Boolean, onCopy: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val label = stringResource(Strings.string.vehicle_card_label_can).uppercase()
    val codeText = AnnotatedString(code, SpanStyle(fontFamily = FontFamily.Monospace))
    val codeStyle = if (tile) CmpTextStyle.Title1 else CmpTextStyle.Title2
    val copy: @Composable () -> Unit = {
        CmpIconButton(
            onClick = onCopy,
            icon = CmpIcons.ContentCopy,
            contentDescription = stringResource(Strings.string.vehicle_card_a11y_copy, code),
            tint = colors.brandPrimaryDefault,
            modifier = Modifier.testTag(VehicleTestTags.CAN_COPY),
        )
    }
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    val surface = Modifier
        .background(colors.brandPrimaryHighlight, shape)
        .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
    if (tile) {
        CmpBox(modifier = modifier.heightIn(min = CapTileMinHeight).then(surface)) {
            CmpColumn(
                modifier = Modifier.padding(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpText(text = label, style = CmpTextStyle.Additional0, color = colors.brandPrimaryDefault)
                CmpText(text = codeText, style = codeStyle, color = colors.foregroundOnDarkPrimaryDefault)
            }
            CmpBox(modifier = Modifier.align(Alignment.TopEnd)) { copy() }
        }
    } else {
        CmpRow(
            modifier = modifier
                .heightIn(min = CanStripMinHeight)
                .then(surface)
                .padding(start = CmpSpacing.space16, end = CmpSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(text = label, style = CmpTextStyle.Additional0, color = colors.brandPrimaryDefault)
            CmpText(
                text = codeText,
                style = codeStyle,
                color = colors.foregroundOnDarkPrimaryDefault,
                modifier = Modifier.weight(1f),
            )
            copy()
        }
    }
}

@Composable
private fun Notes(notes: List<String>, expanded: Boolean, onToggle: () -> Unit) {
    val colors = CmpTheme.colors
    val collapsible = notes.size > 1 || notes.first().length > NOTE_COLLAPSE_CHARS
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warningHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = CmpIcons.PriorityHigh, contentDescription = null, tint = colors.warningDefault)
            CmpText(
                text = stringResource(Strings.string.vehicle_notes_title_important),
                style = CmpTextStyle.Headline2,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
        }
        if (expanded || !collapsible) {
            notes.forEach { CmpHtmlText(html = it, color = colors.foregroundOnDarkPrimaryDefault) }
        } else {
            CmpHtmlText(
                html = notes.first(),
                maxLines = NOTE_COLLAPSED_LINES,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
        }
        if (collapsible) {
            CmpButton(
                text = when {
                    expanded -> stringResource(Strings.string.vehicle_notes_btn_collapse)
                    notes.size > 1 -> stringResource(Strings.string.vehicle_notes_btn_expand_count, notes.size)
                    else -> stringResource(Strings.string.vehicle_notes_btn_expand)
                },
                onClick = onToggle,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                maxLines = 2,
                uppercase = false,
                modifier = Modifier.testTag(VehicleTestTags.NOTES_TOGGLE),
            )
        }
    }
}
