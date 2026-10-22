package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
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
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.isEnabled
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ChangedChip
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ContactBadge
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.PolarityBadge
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.WireTag
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_io_a11y_locked
import specmp.feature.device.settings_ui.generated.resources.settings_io_a11y_toggle_contact
import specmp.feature.device.settings_ui.generated.resources.settings_io_a11y_toggle_polarity
import specmp.feature.device.settings_ui.generated.resources.settings_value_not_assigned

private const val CHANGED_TINT_ALPHA = 0.5f
private const val FUNCTIONS_MAX_LINES = 2

/** Входы, выходы и переключаемые выходы — строка канала, а не обычная строка настройки. */
internal val Setting.isChannel: Boolean
    get() = this is InputSetting || this is OutputSetting || this is SwitchOutputSetting

/**
 * Строка канала (вход / выход / переключаемый выход): номер, имя канала, полярность (+/−) или тип контакта (НО/НЗ),
 * провода (цвет изоляции и пин), ниже — назначенные функции. Справа: у переключаемого выхода — переключатель (вся
 * строка переключает), у входа/выхода — смена типа контакта / полярности одним нажатием или замок.
 */
@Composable
internal fun ChannelRow(
    setting: Setting,
    changed: Boolean,
    path: String?,
    actions: SettingRowActions,
    infoButton: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    val channel = channelOf(setting) ?: return
    val toggle = (setting as? SwitchOutputSetting)?.let { SettingValue.SwitchOutput(!it.value.enabled) }
    // Переключаемый выход — переключается всей строкой; вход/выход — открывает окно функций.
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        infoButton()
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                if (setting.number.isNotEmpty()) {
                    CmpText(text = setting.number, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
                }
                CmpText(
                    text = titleOf(setting.name, channel),
                    style = CmpTextStyle.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                ChannelBadge(setting)
                if (changed) ChangedChip()
                channel.wires.forEach { wire -> WireTag(wire) }
            }
            functionsOf(setting)?.let { functions ->
                CmpText(
                    text = functions.ifEmpty { stringResource(Res.string.settings_value_not_assigned) },
                    style = CmpTextStyle.Body,
                    color = if (functions.isEmpty()) colors.onSurfaceVariant else colors.primary,
                    maxLines = FUNCTIONS_MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (path != null) {
                CmpText(text = path, style = CmpTextStyle.Caption, color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
        Trailing(setting, actions)
    }
}

/** Тип контакта входа или полярность выхода. */
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
            contentAlignment = Alignment.Center
        ) {
            CmpSwitch(checked = setting.value.enabled, onCheckedChange = null)
        }

        is InputSetting -> CmpIconButton(
            onClick = { actions.onValueChange(setting.id, SettingValue.Input(setting.value.value.toggled())) },
            icon = CmpIcons.SwapHoriz,
            contentDescription = stringResource(Res.string.settings_io_a11y_toggle_contact),
            tint = colors.primary,
            modifier = Modifier.size(48.dp).testTag(SettingsTestTags.channelToggle(setting.id))
        )

        is OutputSetting -> if (setting.canEditPolarity) {
            CmpIconButton(
                onClick = { actions.onValueChange(setting.id, SettingValue.Output(setting.value.value.toggled())) },
                icon = CmpIcons.SwapHoriz,
                contentDescription = stringResource(Res.string.settings_io_a11y_toggle_polarity),
                tint = colors.primary,
                modifier = Modifier.size(48.dp).testTag(SettingsTestTags.channelToggle(setting.id))
            )
        } else {
            CmpBox(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = CmpIcons.Lock,
                    contentDescription = stringResource(Res.string.settings_io_a11y_locked),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        else -> Unit
    }
}

/**
 * Полярность выхода для показа (как в старом приложении): неизменяемая — по умолчанию из устройства; изменяемая —
 * текущая, а если не задана — по умолчанию.
 */
internal val OutputSetting.shownPolarity: Polarity
    get() = when {
        !canEditPolarity -> defaultPolarity
        value.value.polarity == Polarity.UNDEFINED -> defaultPolarity
        else -> value.value.polarity
    }

private fun channelOf(setting: Setting): Channel? = when (setting) {
    is InputSetting -> setting.channel
    is OutputSetting -> setting.channel
    is SwitchOutputSetting -> setting.channel
    else -> null
}

/** Имя настройки и имя канала, если они различаются («CH1 · Центральный замок»). */
private fun titleOf(name: String, channel: Channel): String = when {
    channel.name.isBlank() || channel.name == name -> name
    name.isBlank() -> channel.name
    else -> "${channel.name} · $name"
}

/**
 * Назначенные функции через запятую; `""` — не назначено. У входа/выхода — по текущему значению (меняется в окне
 * функций), у переключаемого выхода — только показ, пустой список не выводится (`null`).
 */
private fun functionsOf(setting: Setting): String? = when (setting) {
    is InputSetting -> setting.availableLogics.filter { setting.value.value.isEnabled(it) }.joinToString { it.name }
    is OutputSetting -> setting.availableLogics.filter { setting.value.value.isEnabled(it) }.joinToString { it.name }
    is SwitchOutputSetting -> setting.enabledLogics.joinToString { it.name }.takeIf { it.isNotEmpty() }
    else -> null
}

/** НО ↔ НЗ; не заданный тип становится «НО». */
private fun InputValue.toggled() = copy(
    type = when (type) {
        InputContactType.NORMALLY_OPEN -> InputContactType.NORMALLY_CLOSED
        else -> InputContactType.NORMALLY_OPEN
    }
)

/** − ↔ +; не заданная полярность становится «+» (после «−» по умолчанию). */
private fun OutputValue.toggled() = copy(
    polarity = if (polarity == Polarity.MINUS) Polarity.PLUS else Polarity.MINUS
)
