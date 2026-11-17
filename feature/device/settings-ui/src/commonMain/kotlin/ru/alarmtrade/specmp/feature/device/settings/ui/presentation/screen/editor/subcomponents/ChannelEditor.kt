package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_nc
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_no
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_minus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_plus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_collapse_description
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_expand_description
import ru.alarmtrade.specmp.core.localization.resources.settings_io_label_only_selected
import ru.alarmtrade.specmp.core.localization.resources.settings_io_label_selected
import ru.alarmtrade.specmp.core.localization.resources.settings_io_msg_nothing_found
import ru.alarmtrade.specmp.core.localization.resources.settings_io_placeholder_search
import ru.alarmtrade.specmp.core.localization.resources.settings_search_a11y_clear
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.ChannelLogic
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.WireTag

private val SegmentsWidth = 132.dp
private val FunctionsMaxHeight = 360.dp
private const val SELECTED_TINT_ALPHA = 0.45f

@Composable
internal fun ChannelEditorHeader(
    number: String,
    title: String,
    channel: Channel,
    draft: SettingValue,
    editablePolarity: Boolean,
    onDraftChange: (SettingValue) -> Unit,
    onClose: () -> Unit,
) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpColumn(modifier = Modifier.weight(1f)) {
                if (number.isNotEmpty()) {
                    CmpText(text = number, style = CmpTextStyle.Paragraph3, color = colors.onSurfaceVariant)
                }
                CmpText(text = title, style = CmpTextStyle.Headline1, color = colors.onSurface)
            }
            ChannelModeSegments(draft, editablePolarity, onDraftChange)
            CloseButton(onClose)
        }
        if (channel.wires.isNotEmpty()) {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                channel.wires.forEach { wire -> WireTag(wire) }
            }
        }
    }
}

@Composable
private fun ChannelModeSegments(draft: SettingValue, editablePolarity: Boolean, onDraftChange: (SettingValue) -> Unit) {
    when (draft) {
        is SettingValue.Input -> if (draft.value.type != InputContactType.UNDEFINED) {
            CmpSegmentedButton(
                options = listOf(
                    stringResource(Strings.string.settings_common_value_contact_no),
                    stringResource(Strings.string.settings_common_value_contact_nc),
                ),
                selectedIndex = when (draft.value.type) {
                    InputContactType.NORMALLY_OPEN -> 0
                    InputContactType.NORMALLY_CLOSED -> 1
                    InputContactType.UNDEFINED -> -1
                },
                onSelect = { index ->
                    val type = if (index == 0) InputContactType.NORMALLY_OPEN else InputContactType.NORMALLY_CLOSED
                    onDraftChange(SettingValue.Input(draft.value.copy(type = type)))
                },
                modifier = Modifier.width(SegmentsWidth),
                minHeight = 44.dp,
            )
        }

        is SettingValue.Output -> if (editablePolarity) {
            CmpSegmentedButton(
                options = listOf(
                    stringResource(Strings.string.settings_common_value_polarity_plus_sign),
                    stringResource(Strings.string.settings_common_value_polarity_minus_sign),
                ),
                selectedIndex = when (draft.value.polarity) {
                    Polarity.PLUS -> 0
                    Polarity.MINUS -> 1
                    Polarity.UNDEFINED -> -1
                },
                onSelect = { index ->
                    val polarity = if (index == 0) Polarity.PLUS else Polarity.MINUS
                    onDraftChange(SettingValue.Output(draft.value.copy(polarity = polarity)))
                },
                modifier = Modifier.width(SegmentsWidth),
                minHeight = 44.dp,
            )
        }

        else -> Unit
    }
}

internal fun channelHeaderOf(setting: Setting): Pair<Channel, Boolean>? = when (setting) {
    is InputSetting -> setting.channel to false
    is OutputSetting -> setting.channel to setting.canEditPolarity
    else -> null
}

@Composable
internal fun ChannelFunctionsEditor(
    logics: List<ChannelLogic>,
    state: List<Boolean>,
    searchable: Boolean,
    onChange: (List<Boolean>) -> Unit,
) {
    val colors = cmpThemeColors()
    var query by rememberSaveable { mutableStateOf("") }
    var onlySelected by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(listOf<Int>()) }
    val isOn = { logic: ChannelLogic -> state.getOrElse(logic.id) { false } }
    val selected = logics.count(isOn)
    val visible = logics.filter { logic ->
        (!onlySelected || isOn(logic)) &&
            (!searchable || query.isBlank() || logic.name.contains(query.trim(), ignoreCase = true))
    }

    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically) {
            CmpText(
                text = stringResource(Strings.string.settings_io_label_selected, selected, logics.size),
                style = CmpTextStyle.Paragraph2,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            CmpRow(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Checkbox) { onlySelected = !onlySelected }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpCheckbox(checked = onlySelected, onCheckedChange = null)
                CmpText(
                    text = stringResource(Strings.string.settings_io_label_only_selected),
                    style = CmpTextStyle.Paragraph2,
                )
            }
        }
        // На телефоне поиска нет: список функций короткий для прокрутки, а клавиатура закрывает пол-экрана.
        if (searchable) FunctionSearchField(query) { query = it }
        if (visible.isEmpty()) {
            CmpText(
                text = stringResource(Strings.string.settings_io_msg_nothing_found),
                style = CmpTextStyle.Paragraph2,
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = FunctionsMaxHeight).hideKeyboardOnScroll()) {
                items(visible, key = { it.id }) { logic ->
                    FunctionRow(
                        logic = logic,
                        on = isOn(logic),
                        descriptionOpen = logic.id in expanded,
                        onToggle = { onChange(state.withLogic(logic.id, !isOn(logic))) },
                        onDescriptionToggle = {
                            expanded = if (logic.id in expanded) expanded - logic.id else expanded + logic.id
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FunctionSearchField(query: String, onQueryChange: (String) -> Unit) {
    CmpTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(Strings.string.settings_io_placeholder_search),
        singleLine = true,
        leadingIcon = { CmpIcon(icon = CmpIcons.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                CmpIconButton(
                    onClick = { onQueryChange("") },
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.settings_search_a11y_clear),
                )
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FunctionRow(
    logic: ChannelLogic,
    on: Boolean,
    descriptionOpen: Boolean,
    onToggle: () -> Unit,
    onDescriptionToggle: () -> Unit,
) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(
                if (on) colors.secondaryContainer.copy(alpha = SELECTED_TINT_ALPHA) else Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .testTag(SettingsTestTags.function(logic.id))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpCheckbox(checked = on, onCheckedChange = null)
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(
                text = logic.name,
                style = if (on) CmpTextStyle.Headline2 else CmpTextStyle.Paragraph1,
                color = colors.onSurface,
            )
            if (descriptionOpen) {
                logic.description?.let { description ->
                    CmpText(text = description, style = CmpTextStyle.Paragraph2, color = colors.onSurfaceVariant)
                }
            }
        }
        if (logic.description != null) {
            CmpIconButton(
                onClick = onDescriptionToggle,
                icon = if (descriptionOpen) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (descriptionOpen) {
                        Strings.string.settings_edit_a11y_collapse_description
                    } else {
                        Strings.string.settings_edit_a11y_expand_description
                    },
                ),
                tint = colors.onSurfaceVariant,
            )
        }
    }
}

/** Состояние функций с [enabled] для функции [logicId]; список дополняется до нужной длины. */
private fun List<Boolean>.withLogic(logicId: Int, enabled: Boolean): List<Boolean> {
    val result = toMutableList()
    while (result.size <= logicId) result += false
    result[logicId] = enabled
    return result
}
