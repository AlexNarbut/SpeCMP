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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_description
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
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
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.isSegmented
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.opensEditor
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText

/** Длиннее — значение выводится под названием с переносом, а не справа. */
private const val TEXT_STACK_FROM = 12
private const val OPTION_STACK_FROM = 16
private const val CHANGED_TINT_ALPHA = 0.5f
private const val DISABLED_ALPHA = 0.38f
private val SegmentsWideWidth = 300.dp

/** Действия строки меню. */
@Immutable
internal class SettingRowActions(
    val onOpenSection: (sectionId: Int) -> Unit,
    val onEdit: (settingId: Int) -> Unit,
    val onValueChange: (settingId: Int, value: SettingValue) -> Unit,
    val onDescription: (nodeId: Int) -> Unit,
)

/**
 * Строка меню: раздел (номер, название, счётчик изменений, стрелка) или настройка (ⓘ, номер и название, значение
 * справа / под названием, переключатель или сегменты прямо в строке). Изменённая настройка подсвечена и с точкой.
 *
 * @param path путь раздела (в результатах поиска).
 * @param wide широкая раскладка: сегменты — справа, а не под названием.
 */
@Composable
internal fun SettingNodeRow(
    node: SettingNode,
    changed: Boolean,
    sectionChanges: Int,
    wide: Boolean,
    actions: SettingRowActions,
    modifier: Modifier = Modifier,
    path: String? = null,
    /** Подпись к значению (автомобиль у CAN-кода). */
    note: String? = null,
) {
    when (node) {
        is SettingsSection -> SectionRow(node, sectionChanges, path, actions, modifier)

        is Setting -> if (node.isChannel) {
            ChannelRow(
                setting = node,
                changed = changed,
                path = path,
                actions = actions,
                infoButton = {
                    InfoButton(enabled = node.description != null, onClick = { actions.onDescription(node.id) })
                },
                modifier = modifier,
            )
        } else {
            LeafRow(node, changed, wide, path, note, actions, modifier)
        }
    }
}

@Composable
private fun SectionRow(
    section: SettingsSection,
    changes: Int,
    path: String?,
    actions: SettingRowActions,
    modifier: Modifier,
) {
    // Пустой раздел без номера — связка между условиями («И», «ИЛИ»): разделитель с подписью.
    if (section.children.isEmpty() && section.number.isEmpty()) {
        LinkSeparator(section.name, modifier)
        return
    }
    // В пустой раздел не заходим: без нажатия, наведения и стрелки, текст приглушён.
    val enterable = section.children.isNotEmpty()
    CmpRow(
        modifier = modifier
            .testTag(SettingsTestTags.section(section.id))
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(
                if (enterable) {
                    Modifier.clickable(role = Role.Button) {
                        actions.onOpenSection(section.id)
                    }
                } else {
                    Modifier
                },
            )
            .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpColumn(modifier = Modifier.weight(1f)) {
            Title(number = section.number, name = section.name, changed = false, bold = true, dimmed = !enterable)
            if (path != null) Subtitle(path)
        }
        if (changes > 0) CmpCountPill(count = changes)
        if (enterable) {
            CmpIcon(icon = CmpIcons.ChevronRight, contentDescription = null, tint = cmpThemeColors().onSurfaceVariant)
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
    actions: SettingRowActions,
    modifier: Modifier,
) {
    val colors = cmpThemeColors()
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
            .background(if (changed) colors.tertiaryContainer.copy(alpha = CHANGED_TINT_ALPHA) else colors.surface)
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = 0.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InfoButton(enabled = setting.description != null, onClick = { actions.onDescription(setting.id) })
        CmpColumn(
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Title(number = setting.number, name = setting.name, changed = changed, bold = false)
            when {
                path != null -> Subtitle(path)

                note != null -> Subtitle(note)

                setting is SwitchSetting && (setting.onTitle != null || setting.offTitle != null) ->
                    Subtitle(valueText(setting))

                else -> Unit
            }
            value?.takeIf { stacked }?.let { ValueLabel(it, maxLines = Int.MAX_VALUE) }
            if (segmented != null && !wide) {
                Segments(segmented, actions, Modifier.fillMaxWidth().padding(end = 8.dp))
            }
        }
        when {
            toggle != null -> CmpBox(
                Modifier.size(width = 64.dp, height = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CmpSwitch(checked = !toggle.first, onCheckedChange = null)
            }

            segmented != null -> if (wide) Segments(segmented, actions, Modifier.width(SegmentsWideWidth))

            else -> CmpRow(verticalAlignment = Alignment.CenterVertically) {
                if (!stacked && value != null) {
                    ValueLabel(value, maxLines = 1, modifier = Modifier.widthIn(max = 220.dp).padding(start = 8.dp))
                }
                val chevronTint = if (onClick != null) {
                    colors.onSurfaceVariant
                } else {
                    colors.onSurfaceVariant.copy(
                        alpha = DISABLED_ALPHA,
                    )
                }
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = chevronTint,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

/** Для переключаемых настроек: (текущее «вкл», значение после переключения), иначе null. */
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
    val colors = cmpThemeColors()
    CmpIconButton(
        onClick = onClick,
        icon = CmpIcons.Info,
        contentDescription = stringResource(Strings.string.settings_common_a11y_description),
        enabled = enabled,
        tint = if (enabled) colors.primary else colors.outline.copy(alpha = DISABLED_ALPHA),
        modifier = Modifier.size(48.dp),
    )
}

@Composable
private fun Title(number: String, name: String, changed: Boolean, bold: Boolean, dimmed: Boolean = false) {
    val colors = cmpThemeColors()
    CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(
            text = if (number.isEmpty()) name else "$number  $name",
            style = CmpTextStyle.BodyLarge,
            fontWeight = if (bold) FontWeight.SemiBold else null,
            color = if (dimmed) colors.onSurfaceVariant else colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (changed) CmpDot()
    }
}

@Composable
private fun Subtitle(text: String) {
    CmpText(
        text = text,
        style = CmpTextStyle.Body,
        color = cmpThemeColors().onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ValueLabel(text: String, maxLines: Int, modifier: Modifier = Modifier) {
    CmpText(
        text = text,
        style = CmpTextStyle.BodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = cmpThemeColors().primary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Связка между условиями («И», «ИЛИ»): подпись по центру между линиями, не нажимается. */
@Composable
private fun LinkSeparator(text: String, modifier: Modifier) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = 36.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpDivider(modifier = Modifier.weight(1f), color = colors.outlineVariant)
        CmpText(
            text = text,
            style = CmpTextStyle.Caption,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurfaceVariant,
        )
        CmpDivider(modifier = Modifier.weight(1f), color = colors.outlineVariant)
    }
}
