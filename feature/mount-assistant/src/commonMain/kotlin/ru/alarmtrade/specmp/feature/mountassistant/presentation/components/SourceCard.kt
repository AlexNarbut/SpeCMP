package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingsSourceKind

/** Откуда загрузить настройки; выбор открывает редактор, где загрузка и показ изменений — как обычно. */
@Composable
internal fun SourceCard(entry: JournalEntry.SourcePicker, online: Boolean, onPick: (SettingsSourceKind) -> Unit) {
    CardShell(modifier = Modifier.testTag(resultTag(entry.id))) {
        CardHeader(icon = CmpIcons.Download, title = stringResource(Strings.string.mountassistant_source_title_card))
        CmpText(
            text = stringResource(Strings.string.mountassistant_source_msg_card),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
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
    val colors = CmpTheme.colors
    val selected = used == kind
    CmpListItem(
        title = stringResource(kind.title),
        modifier = Modifier.fillMaxWidth().testTag("mountassistant_source_${kind.name.lowercase()}"),
        subtitle = stringResource(
            if (blocked) Strings.string.mountassistant_source_error_no_network else kind.subtitle,
        ),
        icon = if (blocked) CmpIcons.CloudOff else kind.icon,
        onClick = onClick.takeIf { used == null && !blocked },
        enabled = used == null || selected,
        selected = selected,
        trailing = {
            CmpIcon(
                icon = when {
                    selected -> CmpIcons.Check
                    blocked -> CmpIcons.Lock
                    else -> CmpIcons.ChevronRight
                },
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconM),
                tint = if (selected) colors.brandPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
            )
        },
    )
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
