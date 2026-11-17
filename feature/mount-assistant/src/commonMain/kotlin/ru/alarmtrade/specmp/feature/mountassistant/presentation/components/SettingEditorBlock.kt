package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

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
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_a11y_decrease
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_a11y_increase
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_btn_next
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_label_range
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_value_off
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_editor_value_on
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpStepButton
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
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpColumn(
        modifier = Modifier.fillMaxWidth()
            .background(colors.backgroundSurfaceUltraHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        when (val editor = setting.editor) {
            is SettingEditor.Switch -> SwitchEditor(draft as? AssistantSettingValue.Switch, onDraft)
            is SettingEditor.Options -> OptionsEditor(editor, draft as? AssistantSettingValue.Option, onDraft)
            is SettingEditor.Number -> NumberEditor(editor, draft as? AssistantSettingValue.Number, onDraft)
            null -> Unit
        }
        CmpRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpButton(
                text = stringResource(Strings.string.mountassistant_editor_btn_cancel),
                onClick = onCancel,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
            CmpButton(
                text = stringResource(Strings.string.mountassistant_editor_btn_next),
                onClick = onNext,
                modifier = Modifier.testTag(EDITOR_NEXT_TAG),
                enabled = draft != current,
                size = CmpButtonSize.S,
            )
        }
    }
}

@Composable
private fun SwitchEditor(draft: AssistantSettingValue.Switch?, onDraft: (AssistantSettingValue) -> Unit) {
    val enabled = draft?.enabled == true
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin + CmpSpacing.space8)
            .toggleable(value = enabled, role = Role.Switch) { onDraft(AssistantSettingValue.Switch(it)) }
            .padding(horizontal = CmpSpacing.space4),
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
            style = CmpTextStyle.Headline2,
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
                modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin)
                    .selectable(selected = selected, role = Role.RadioButton) {
                        onDraft(AssistantSettingValue.Option(index))
                    },
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
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
    val colors = CmpTheme.colors
    val value = draft?.value ?: editor.range.first
    val box = RoundedCornerShape(CmpRadius.inputs)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpStepButton(
                icon = CmpIcons.Remove,
                contentDescription = stringResource(Strings.string.mountassistant_editor_a11y_decrease),
                onStep = { onDraft(AssistantSettingValue.Number((value - editor.step).coerceIn(editor.range))) },
                enabled = value > editor.range.first,
                size = CmpSize.input,
            )
            CmpBox(
                modifier = Modifier.weight(1f).height(CmpSize.input)
                    .background(colors.backgroundSurfaceUltraHighlight, box)
                    .border(CmpBorderWidth.inputs, colors.borderLight, box),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(text = numberText(value, editor.units), style = CmpTextStyle.Headline1)
            }
            CmpStepButton(
                icon = CmpIcons.Add,
                contentDescription = stringResource(Strings.string.mountassistant_editor_a11y_increase),
                onStep = { onDraft(AssistantSettingValue.Number((value + editor.step).coerceIn(editor.range))) },
                enabled = value < editor.range.last,
                size = CmpSize.input,
            )
        }
        CmpText(
            text = stringResource(
                Strings.string.mountassistant_editor_label_range,
                numberText(editor.range.first, editor.units),
                numberText(editor.range.last, editor.units),
            ),
            style = CmpTextStyle.Paragraph3,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}
