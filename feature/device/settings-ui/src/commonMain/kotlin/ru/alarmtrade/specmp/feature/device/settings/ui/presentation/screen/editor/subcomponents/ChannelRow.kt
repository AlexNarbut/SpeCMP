package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_not_assigned
import ru.alarmtrade.specmp.core.localization.resources.settings_io_a11y_locked
import ru.alarmtrade.specmp.core.localization.resources.settings_io_a11y_toggle_contact
import ru.alarmtrade.specmp.core.localization.resources.settings_io_a11y_toggle_polarity
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.channelOrNull
import ru.alarmtrade.specmp.feature.device.settings.domain.model.isEnabled
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ChangedChip
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ContactBadge
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.PolarityBadge
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.WireTag
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.HighlightedPart
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.highlightedText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.settingTitleParts

private const val CHANGED_TINT_ALPHA = 0.5f
private const val FUNCTIONS_MAX_LINES = 2

internal val Setting.isChannel: Boolean
    get() = this is InputSetting || this is OutputSetting || this is SwitchOutputSetting

@Composable
internal fun ChannelRow(
    setting: Setting,
    changed: Boolean,
    path: String?,
    actions: SettingRowActions,
    infoButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    highlights: SearchHighlights? = null,
) {
    val colors = cmpThemeColors()
    val channel = setting.channelOrNull ?: return
    val highlightStyle = searchHighlightStyle()
    val shown = highlights ?: SearchHighlights()
    val number = remember(setting.number, shown, highlightStyle) {
        highlightedText(listOf(HighlightedPart(setting.number, shown.number)), highlightStyle)
    }
    val title = remember(setting.name, channel, shown, highlightStyle) {
        highlightedText(settingTitleParts(setting.name, channel, shown), highlightStyle)
    }
    val toggle = (setting as? SwitchOutputSetting)?.let { SettingValue.SwitchOutput(!it.value.enabled) }
    val onClick: () -> Unit = if (toggle != null) {
        { actions.onValueChange(setting.id, toggle) }
    } else {
        { actions.onEdit(setting.id) }
    }
    CmpRow(
        modifier = modifier
            .testTag(SettingsTestTags.row(setting.id))
            .fillMaxWidth()
            .background(if (changed) colors.tertiaryContainer.copy(alpha = CHANGED_TINT_ALPHA) else colors.surface)
            .heightIn(min = 56.dp)
            .clickable(role = if (toggle != null) Role.Switch else Role.Button, onClick = onClick)
            .padding(end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        infoButton()
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                if (setting.number.isNotEmpty()) {
                    CmpText(text = number, style = CmpTextStyle.Paragraph1, color = colors.onSurfaceVariant)
                }
                CmpText(
                    text = title,
                    style = CmpTextStyle.Headline2,
                    color = colors.onSurface,
                )
                ChannelBadge(setting)
                if (changed) ChangedChip()
                channel.wires.forEach { wire -> WireTag(wire) }
            }
            functionsOf(setting)?.let { functions ->
                CmpText(
                    text = functions.ifEmpty { stringResource(Strings.string.settings_common_value_not_assigned) },
                    style = CmpTextStyle.Paragraph1,
                    color = if (functions.isEmpty()) colors.onSurfaceVariant else colors.primary,
                    maxLines = FUNCTIONS_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (path != null) {
                CmpText(text = path, style = CmpTextStyle.Paragraph2, color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
        Trailing(setting, actions)
    }
}

@Composable
private fun ChannelBadge(setting: Setting) {
    when (setting) {
        is InputSetting -> {
            PolarityBadge(setting.defaultPolarity)
            ContactBadge(setting.value.value.type)
        }

        is OutputSetting -> PolarityBadge(setting.shownPolarity)

        is SwitchOutputSetting -> PolarityBadge(setting.polarity)

        else -> Unit
    }
}

@Composable
private fun Trailing(setting: Setting, actions: SettingRowActions) {
    val colors = cmpThemeColors()
    when (setting) {
        is SwitchOutputSetting -> CmpBox(
            modifier = Modifier.size(width = 64.dp, height = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            CmpSwitch(checked = setting.value.enabled, onCheckedChange = null)
        }

        is InputSetting -> if (setting.canEditContactType) {
            CmpIconButton(
                onClick = { actions.onValueChange(setting.id, SettingValue.Input(setting.value.value.toggled())) },
                icon = CmpIcons.SwapHoriz,
                contentDescription = stringResource(Strings.string.settings_io_a11y_toggle_contact),
                tint = colors.primary,
                modifier = Modifier.size(48.dp).testTag(SettingsTestTags.channelToggle(setting.id)),
            )
        }

        is OutputSetting -> if (setting.canEditPolarity) {
            CmpIconButton(
                onClick = { actions.onValueChange(setting.id, SettingValue.Output(setting.value.value.toggled())) },
                icon = CmpIcons.SwapHoriz,
                contentDescription = stringResource(Strings.string.settings_io_a11y_toggle_polarity),
                tint = colors.primary,
                modifier = Modifier.size(48.dp).testTag(SettingsTestTags.channelToggle(setting.id)),
            )
        } else {
            CmpBox(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = CmpIcons.Lock,
                    contentDescription = stringResource(Strings.string.settings_io_a11y_locked),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        else -> Unit
    }
}

/** Неизменяемая — по умолчанию из устройства; изменяемая — текущая, а если не задана — по умолчанию. */
internal val OutputSetting.shownPolarity: Polarity
    get() = when {
        !canEditPolarity -> defaultPolarity
        value.value.polarity == Polarity.UNDEFINED -> defaultPolarity
        else -> value.value.polarity
    }

private fun functionsOf(setting: Setting): String? = when (setting) {
    is InputSetting -> setting.availableLogics.filter { setting.value.value.isEnabled(it) }.joinToString { it.name }
    is OutputSetting -> setting.availableLogics.filter { setting.value.value.isEnabled(it) }.joinToString { it.name }
    is SwitchOutputSetting -> setting.enabledLogics.joinToString { it.name }.takeIf { it.isNotEmpty() }
    else -> null
}

/** Флага в SDK нет: не заданный тип (`UNDEFINED`) значит, что у входа нет NO/NC. */
internal val InputSetting.canEditContactType: Boolean
    get() = value.value.type != InputContactType.UNDEFINED

private fun InputValue.toggled() = copy(
    type = when (type) {
        InputContactType.NORMALLY_OPEN -> InputContactType.NORMALLY_CLOSED
        else -> InputContactType.NORMALLY_OPEN
    },
)

/** − ↔ +; не заданная полярность становится «+» (после «−» по умолчанию). */
private fun OutputValue.toggled() = copy(
    polarity = if (polarity == Polarity.MINUS) Polarity.PLUS else Polarity.MINUS,
)
