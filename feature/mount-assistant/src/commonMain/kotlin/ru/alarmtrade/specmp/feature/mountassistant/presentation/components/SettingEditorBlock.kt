package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_a11y_decrease
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_a11y_increase
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_btn_next
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_label_range
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_value_off
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_value_on
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpStepButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingEditor
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.numberText

internal const val EDITOR_NEXT_TAG = "mountassistant_editor_next"

/** Правка настройки в ленте: переключатель, список или число; «Далее» — к подтверждению записи. */
@Composable
internal fun SettingEditorBlock(
    setting: FoundSetting,
    current: AssistantSettingValue,
    draft: AssistantSettingValue,
    onDraft: (AssistantSettingValue) -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = Modifier.fillMaxWidth()
            .background(colors.surfaceContainerLow, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, colors.outlineVariant), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val editor = setting.editor) {
            is SettingEditor.Switch -> SwitchEditor(draft as? AssistantSettingValue.Switch, onDraft)
            is SettingEditor.Options -> OptionsEditor(editor, draft as? AssistantSettingValue.Option, onDraft)
            is SettingEditor.Number -> NumberEditor(editor, draft as? AssistantSettingValue.Number, onDraft)
            null -> Unit
        }
        CmpRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpTextButton(
                text = stringResource(Strings.string.mountassistant_editor_btn_cancel),
                onClick = onCancel,
                modifier = Modifier.heightIn(min = 48.dp),
            )
            CmpButton(
                text = stringResource(Strings.string.mountassistant_editor_btn_next),
                onClick = onNext,
                enabled = draft != current,
                modifier = Modifier.heightIn(min = 48.dp).testTag(EDITOR_NEXT_TAG),
            )
        }
    }
}

@Composable
private fun SwitchEditor(draft: AssistantSettingValue.Switch?, onDraft: (AssistantSettingValue) -> Unit) {
    val enabled = draft?.enabled == true
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .toggleable(value = enabled, role = Role.Switch) { onDraft(AssistantSettingValue.Switch(it)) }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(
                if (enabled) {
                    Strings.string.mountassistant_editor_value_on
                } else {
                    Strings.string.mountassistant_editor_value_off
                },
            ),
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.Paragraph1,
        )
        CmpSwitch(checked = enabled, onCheckedChange = null)
    }
}

@Composable
private fun OptionsEditor(
    editor: SettingEditor.Options,
    draft: AssistantSettingValue.Option?,
    onDraft: (AssistantSettingValue) -> Unit,
) {
    CmpColumn {
        editor.options.forEachIndexed { index, option ->
            val selected = draft?.index == index
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(selected = selected, role = Role.RadioButton) {
                        onDraft(AssistantSettingValue.Option(index))
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpRadioButton(selected = selected, onClick = null)
                CmpText(text = option, style = CmpTextStyle.Paragraph1)
            }
        }
    }
}

@Composable
private fun NumberEditor(
    editor: SettingEditor.Number,
    draft: AssistantSettingValue.Number?,
    onDraft: (AssistantSettingValue) -> Unit,
) {
    val colors = cmpThemeColors()
    val value = draft?.value ?: editor.range.first
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpStepButton(
                icon = CmpIcons.Remove,
                contentDescription = stringResource(Strings.string.mountassistant_editor_a11y_decrease),
                onStep = { onDraft(AssistantSettingValue.Number((value - editor.step).coerceIn(editor.range))) },
                enabled = value > editor.range.first,
                size = 56.dp,
            )
            CmpBox(
                modifier = Modifier.weight(1f).height(56.dp)
                    .background(colors.surfaceContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(text = numberText(value, editor.units), style = CmpTextStyle.Headline1)
            }
            CmpStepButton(
                icon = CmpIcons.Add,
                contentDescription = stringResource(Strings.string.mountassistant_editor_a11y_increase),
                onStep = { onDraft(AssistantSettingValue.Number((value + editor.step).coerceIn(editor.range))) },
                enabled = value < editor.range.last,
                size = 56.dp,
            )
        }
        CmpText(
            text = stringResource(
                Strings.string.mountassistant_editor_label_range,
                numberText(editor.range.first, editor.units),
                numberText(editor.range.last, editor.units),
            ),
            style = CmpTextStyle.Paragraph2,
            color = colors.onSurfaceVariant,
        )
    }
}
