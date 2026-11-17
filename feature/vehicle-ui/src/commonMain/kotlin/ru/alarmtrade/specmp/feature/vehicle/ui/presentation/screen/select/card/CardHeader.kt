package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
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
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CarPhoto
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PHOTO_RATIO
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.yearsText
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CapabilityKind
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions

private const val NOTE_COLLAPSE_CHARS = 140
private const val NOTE_COLLAPSED_LINES = 3

@Composable
internal fun CardHeader(
    card: CarCard,
    wide: Boolean,
    notesExpanded: Boolean,
    actions: CardActions,
    modifier: Modifier = Modifier,
) {
    val summary = card.summary
    val colors = cmpThemeColors()
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CarPhoto(
                url = summary.imageUrl,
                radius = 14.dp,
                modifier = Modifier.width(if (wide) 200.dp else 120.dp).aspectRatio(PHOTO_RATIO),
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CmpText(
                    text = "${summary.brand} ${summary.model}",
                    style = if (wide) CmpTextStyle.Title2 else CmpTextStyle.Headline1,
                    color = colors.onSurface,
                )
                CmpText(
                    text = stringResource(
                        Strings.string.vehicle_card_label_years_trim,
                        yearsText(summary.years),
                        summary.name,
                    ),
                    style = CmpTextStyle.Paragraph1,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        val engine = stringResource(Strings.string.vehicle_cap_label_engine_block)
        val keyless = stringResource(Strings.string.vehicle_cap_label_keyless)
        if (wide) {
            CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        actions.onOpenFirmwareArchive?.let { open ->
            CmpTextButton(onClick = { open(summary.canCode) }, modifier = Modifier.heightIn(min = 48.dp)) {
                CmpIcon(icon = CmpIcons.History, contentDescription = null, modifier = Modifier.size(20.dp))
                CmpText(
                    text = stringResource(Strings.string.vehicle_card_btn_archive, summary.canCode),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
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
    val colors = cmpThemeColors()
    val label = stringResource(Strings.string.vehicle_card_label_can).uppercase()
    val codeText = AnnotatedString(code, SpanStyle(fontFamily = FontFamily.Monospace))
    val codeStyle = if (tile) CmpTextStyle.Title1 else CmpTextStyle.Title2
    val copy: @Composable () -> Unit = {
        CmpIconButton(
            onClick = onCopy,
            icon = CmpIcons.ContentCopy,
            contentDescription = stringResource(Strings.string.vehicle_card_a11y_copy, code),
            tint = colors.onPrimaryContainer,
            modifier = Modifier.testTag(VehicleTestTags.CAN_COPY),
        )
    }
    val shape = RoundedCornerShape(14.dp)
    if (tile) {
        CmpBox(modifier = modifier.heightIn(min = 112.dp).background(colors.primaryContainer, shape)) {
            CmpColumn(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpText(
                    text = label,
                    style = CmpTextStyle.Additional0,
                    color = colors.onPrimaryContainer,
                )
                CmpText(text = codeText, style = codeStyle, color = colors.onPrimaryContainer)
            }
            CmpBox(modifier = Modifier.align(Alignment.TopEnd)) { copy() }
        }
    } else {
        CmpRow(
            modifier = modifier.heightIn(
                min = 64.dp,
            ).background(colors.primaryContainer, shape).padding(start = 16.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = label,
                style = CmpTextStyle.Additional0,
                color = colors.onPrimaryContainer,
            )
            CmpText(
                text = codeText,
                style = codeStyle,
                color = colors.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            copy()
        }
    }
}

@Composable
private fun Notes(notes: List<String>, expanded: Boolean, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    val collapsible = notes.size > 1 || notes.first().length > NOTE_COLLAPSE_CHARS
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.tertiaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(icon = CmpIcons.PriorityHigh, contentDescription = null, tint = colors.onTertiaryContainer)
            CmpText(
                text = stringResource(Strings.string.vehicle_notes_title_important),
                style = CmpTextStyle.Headline2,
                color = colors.onTertiaryContainer,
            )
        }
        if (expanded || !collapsible) {
            notes.forEach { CmpHtmlText(html = it, color = colors.onSurface) }
        } else {
            CmpHtmlText(html = notes.first(), maxLines = NOTE_COLLAPSED_LINES, color = colors.onSurface)
        }
        if (collapsible) {
            CmpTextButton(
                text = when {
                    expanded -> stringResource(Strings.string.vehicle_notes_btn_collapse)
                    notes.size > 1 -> stringResource(Strings.string.vehicle_notes_btn_expand_count, notes.size)
                    else -> stringResource(Strings.string.vehicle_notes_btn_expand)
                },
                onClick = onToggle,
                modifier = Modifier.heightIn(min = 48.dp).testTag(VehicleTestTags.NOTES_TOGGLE),
            )
        }
    }
}
