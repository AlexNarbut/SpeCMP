package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_error_no_network
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_msg_card
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_subtitle_cloud
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_subtitle_defaults
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_subtitle_file
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_subtitle_mine
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_title_card
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_title_cloud
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_title_defaults
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_title_file
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_source_title_mine
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingsSourceKind

private const val USED_ALPHA = 0.38f

/** Откуда загрузить настройки; выбор открывает редактор, где загрузка и показ изменений — как обычно. */
@Composable
internal fun SourceCard(entry: JournalEntry.SourcePicker, online: Boolean, onPick: (SettingsSourceKind) -> Unit) {
    val colors = cmpThemeColors()
    CardShell(modifier = Modifier.testTag(resultTag(entry.id))) {
        CardHeader(icon = CmpIcons.Download, title = stringResource(Strings.string.mountassistant_source_title_card))
        CmpText(
            text = stringResource(Strings.string.mountassistant_source_msg_card),
            style = CmpTextStyle.Body,
            color = colors.onSurfaceVariant,
        )
        CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsSourceKind.entries.forEach { kind ->
                SourceRow(
                    kind = kind,
                    used = entry.used,
                    blocked = kind == SettingsSourceKind.CLOUD && !online,
                    onClick = { onPick(kind) },
                )
            }
        }
    }
}

@Composable
private fun SourceRow(kind: SettingsSourceKind, used: SettingsSourceKind?, blocked: Boolean, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    val selected = used == kind
    val shape = RoundedCornerShape(12.dp)
    CmpSurface(
        modifier = Modifier.fillMaxWidth().alpha(if (used != null && !selected) USED_ALPHA else 1f)
            .clip(shape).clickable(enabled = used == null && !blocked, onClick = onClick)
            .testTag("mountassistant_source_${kind.name.lowercase()}"),
        shape = shape,
        color = sourceBackground(selected, blocked),
        contentColor = colors.onSurface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
    ) {
        SourceRowContent(kind, selected, blocked)
    }
}

@Composable
private fun sourceBackground(selected: Boolean, blocked: Boolean) = when {
    selected -> cmpThemeColors().secondaryContainer
    blocked -> cmpThemeColors().surfaceContainer
    else -> assistantCardColor()
}

@Composable
private fun SourceRowContent(kind: SettingsSourceKind, selected: Boolean, blocked: Boolean) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBox(
            icon = if (blocked) CmpIcons.CloudOff else kind.icon,
            background = if (blocked) colors.surfaceContainerHighest else colors.primaryContainer,
            tint = if (blocked) colors.onSurfaceVariant else colors.onPrimaryContainer,
        )
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = stringResource(kind.title), style = CmpTextStyle.BodyLarge, fontWeight = FontWeight.Medium)
            CmpText(
                text = stringResource(
                    if (blocked) Strings.string.mountassistant_source_error_no_network else kind.subtitle,
                ),
                style = CmpTextStyle.Body,
                color = if (blocked) colors.tertiary else colors.onSurfaceVariant,
            )
        }
        CmpIcon(
            icon = when {
                selected -> CmpIcons.Check
                blocked -> CmpIcons.Lock
                else -> CmpIcons.ChevronRight
            },
            contentDescription = null,
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
        )
    }
}

private val SettingsSourceKind.icon: CmpIcons
    get() = when (this) {
        SettingsSourceKind.FILE -> CmpIcons.Description
        SettingsSourceKind.MINE -> CmpIcons.FolderOpen
        SettingsSourceKind.CLOUD -> CmpIcons.Cloud
        SettingsSourceKind.DEFAULTS -> CmpIcons.RestartAlt
    }

private val SettingsSourceKind.title
    get() = when (this) {
        SettingsSourceKind.FILE -> Strings.string.mountassistant_source_title_file
        SettingsSourceKind.MINE -> Strings.string.mountassistant_source_title_mine
        SettingsSourceKind.CLOUD -> Strings.string.mountassistant_source_title_cloud
        SettingsSourceKind.DEFAULTS -> Strings.string.mountassistant_source_title_defaults
    }

private val SettingsSourceKind.subtitle
    get() = when (this) {
        SettingsSourceKind.FILE -> Strings.string.mountassistant_source_subtitle_file
        SettingsSourceKind.MINE -> Strings.string.mountassistant_source_subtitle_mine
        SettingsSourceKind.CLOUD -> Strings.string.mountassistant_source_subtitle_cloud
        SettingsSourceKind.DEFAULTS -> Strings.string.mountassistant_source_subtitle_defaults
    }
