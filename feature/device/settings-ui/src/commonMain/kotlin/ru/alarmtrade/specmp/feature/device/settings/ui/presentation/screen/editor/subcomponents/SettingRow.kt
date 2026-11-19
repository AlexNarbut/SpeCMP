package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_description
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.highlightedText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.isSegmented
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.numberedTitleParts
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.opensEditor
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText

private const val TEXT_STACK_FROM = 12
private const val OPTION_STACK_FROM = 16
private val SegmentsWideWidth = 300.dp
private val ValueMaxWidth = 220.dp
private val ToggleBoxWidth = 64.dp
private val LinkSeparatorHeight = 36.dp

@Immutable
internal class SettingRowActions(
    val onOpenSection: (sectionId: Int) -> Unit,
    val onEdit: (settingId: Int) -> Unit,
    val onValueChange: (settingId: Int, value: SettingValue) -> Unit,
    val onDescription: (nodeId: Int) -> Unit,
)

@Composable
internal fun SettingNodeRow(
    node: SettingNode,
    changed: Boolean,
    sectionChanges: Int,
    wide: Boolean,
    actions: SettingRowActions,
    modifier: Modifier = Modifier,
    path: String? = null,
    note: String? = null,
    highlights: SearchHighlights? = null,
) {
    when (node) {
        is SettingsSection -> SectionRow(node, sectionChanges, path, highlights, actions, modifier)

        is Setting -> if (node.isChannel) {
            ChannelRow(
                setting = node,
                changed = changed,
                path = path,
                highlights = highlights,
                actions = actions,
                infoButton = {
                    InfoButton(enabled = node.description != null, onClick = { actions.onDescription(node.id) })
                },
                modifier = modifier,
            )
        } else {
            LeafRow(node, changed, wide, path, note, highlights, actions, modifier)
        }
    }
}

@Composable
private fun SectionRow(
    section: SettingsSection,
    changes: Int,
    path: String?,
    highlights: SearchHighlights?,
    actions: SettingRowActions,
    modifier: Modifier,
) {
    // Пустой раздел без номера — связка между условиями («И», «ИЛИ»): разделитель с подписью.
    if (section.children.isEmpty() && section.number.isEmpty()) {
        LinkSeparator(section.name, modifier)
        return
    }
    val enterable = section.children.isNotEmpty()
    CmpRow(
        modifier = modifier
            .testTag(SettingsTestTags.section(section.id))
            .fillMaxWidth()
            .heightIn(min = CmpSize.input)
            .then(
                if (enterable) {
                    Modifier.clickable(role = Role.Button) {
                        actions.onOpenSection(section.id)
                    }
                } else {
                    Modifier
                },
            )
            .padding(
                start = CmpSpacing.space16,
                end = CmpSpacing.space12,
                top = CmpSpacing.space4,
                bottom = CmpSpacing.space4,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpColumn(modifier = Modifier.weight(1f)) {
            Title(
                number = section.number,
                name = section.name,
                changed = false,
                bold = true,
                dimmed = !enterable,
                highlights = highlights,
            )
            if (path != null) Subtitle(path)
        }
        if (changes > 0) CmpCountPill(count = changes)
        if (enterable) {
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod") // раскладка строки по типу настройки
@Composable
private fun LeafRow(
    setting: Setting,
    changed: Boolean,
    wide: Boolean,
    path: String?,
    note: String?,
    highlights: SearchHighlights?,
    actions: SettingRowActions,
    modifier: Modifier,
) {
    val colors = CmpTheme.colors
    val segmented = (setting as? OptionSetting)?.takeIf { it.isSegmented }
    val toggle = toggleOf(setting)
    val value = if (toggle == null && segmented == null) valueText(setting) else null
    val stacked = value != null && when (setting) {
        is TextSetting -> value.length > TEXT_STACK_FROM
        is OptionSetting -> value.length > OPTION_STACK_FROM
        else -> false
    }
    val onClick: (() -> Unit)? = when {
        toggle != null -> { -> actions.onValueChange(setting.id, toggle.second) }
        setting.opensEditor -> { -> actions.onEdit(setting.id) }
        else -> null
    }

    CmpRow(
        modifier = modifier
            .testTag(SettingsTestTags.row(setting.id))
            .fillMaxWidth()
            .background(if (changed) colors.brandPrimaryHighlight else Color.Transparent)
            .heightIn(min = CmpSize.input)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(end = CmpSpacing.space4, top = CmpSpacing.space4, bottom = CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InfoButton(enabled = setting.description != null, onClick = { actions.onDescription(setting.id) })
        CmpColumn(
            modifier = Modifier.weight(1f).padding(vertical = CmpSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            Title(
                number = setting.number,
                name = setting.name,
                changed = changed,
                bold = false,
                highlights = highlights,
            )
            when {
                path != null -> Subtitle(path)

                note != null -> Subtitle(note)

                setting is SwitchSetting && (setting.onTitle != null || setting.offTitle != null) ->
                    Subtitle(valueText(setting))

                else -> Unit
            }
            value?.takeIf { stacked }?.let { ValueLabel(it, maxLines = Int.MAX_VALUE) }
            if (segmented != null && !wide) {
                Segments(segmented, actions, Modifier.fillMaxWidth().padding(end = CmpSpacing.space8))
            }
        }
        when {
            toggle != null -> CmpBox(
                Modifier.size(width = ToggleBoxWidth, height = CmpSize.touchMin),
                contentAlignment = Alignment.Center,
            ) {
                CmpSwitch(checked = !toggle.first, onCheckedChange = null)
            }

            segmented != null -> if (wide) Segments(segmented, actions, Modifier.width(SegmentsWideWidth))

            else -> CmpRow(verticalAlignment = Alignment.CenterVertically) {
                if (!stacked && value != null) {
                    ValueLabel(
                        value,
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = ValueMaxWidth).padding(start = CmpSpacing.space8),
                    )
                }
                val chevronTint = if (onClick != null) {
                    colors.foregroundOnDarkSecondaryDefault
                } else {
                    colors.foregroundOnDarkPrimaryDisabled
                }
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = chevronTint,
                    modifier = Modifier.padding(horizontal = CmpSpacing.space8),
                )
            }
        }
    }
}

private fun toggleOf(setting: Setting): Pair<Boolean, SettingValue>? = when (setting) {
    is SwitchSetting -> !setting.value.enabled to SettingValue.Switch(!setting.value.enabled)
    is SwitchOutputSetting -> !setting.value.enabled to SettingValue.SwitchOutput(!setting.value.enabled)
    else -> null
}

@Composable
private fun Segments(setting: OptionSetting, actions: SettingRowActions, modifier: Modifier) {
    CmpSegmentedButton(
        options = setting.options,
        selectedIndex = setting.value.index,
        onSelect = { index -> actions.onValueChange(setting.id, SettingValue.Option(index)) },
        modifier = modifier,
    )
}

@Composable
private fun InfoButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = CmpTheme.colors
    CmpIconButton(
        onClick = onClick,
        icon = CmpIcons.Info,
        contentDescription = stringResource(Strings.string.settings_common_a11y_description),
        enabled = enabled,
        tint = if (enabled) colors.brandPrimaryDefault else colors.foregroundOnDarkPrimaryDisabled,
        modifier = Modifier.size(CmpSize.touchMin),
    )
}

@Composable
private fun Title(
    number: String,
    name: String,
    changed: Boolean,
    bold: Boolean,
    dimmed: Boolean = false,
    highlights: SearchHighlights? = null,
) {
    val colors = CmpTheme.colors
    val style = searchHighlightStyle()
    val text = remember(number, name, highlights, style) {
        highlightedText(numberedTitleParts(number, name, highlights ?: SearchHighlights()), style)
    }
    CmpRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(
            text = text,
            style = if (bold) CmpTextStyle.Headline2 else CmpTextStyle.Paragraph1,
            color = if (dimmed) colors.foregroundOnDarkSecondaryDefault else colors.foregroundOnDarkPrimaryDefault,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (changed) CmpDot()
    }
}

@Composable
internal fun searchHighlightStyle(): SpanStyle {
    val colors = CmpTheme.colors
    return remember(colors.brandPrimaryHighlight, colors.foregroundOnDarkPrimaryDefault) {
        SpanStyle(
            background = colors.brandPrimaryHighlight,
            color = colors.foregroundOnDarkPrimaryDefault,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Subtitle(text: String) {
    CmpText(
        text = text,
        style = CmpTextStyle.Paragraph3,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ValueLabel(text: String, maxLines: Int, modifier: Modifier = Modifier) {
    CmpText(
        text = text,
        style = CmpTextStyle.Headline3,
        color = CmpTheme.colors.brandPrimaryDefault,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun LinkSeparator(text: String, modifier: Modifier) {
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = LinkSeparatorHeight).padding(horizontal = CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpDivider(modifier = Modifier.weight(1f))
        CmpText(
            text = text,
            style = CmpTextStyle.Additional0,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        CmpDivider(modifier = Modifier.weight(1f))
    }
}
