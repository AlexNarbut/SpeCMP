package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_collapse_description
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_expand_description
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_btn_done
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_btn_reset
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_label_was
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartScheduleSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.settingTitle
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.EditingSetting
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

private val EditorDialogWidth = CmpDialogWidth(fraction = 0.5f, min = 480.dp, max = 600.dp)

@Immutable
internal class EditorActions(
    val onDraftChange: (SettingValue) -> Unit,
    val onReset: () -> Unit,
    val onApply: () -> Unit,
    val onDismiss: () -> Unit,
    val onCarSearch: () -> Unit,
    val onCarPick: (carId: Int) -> Unit,
    val onPickFromCatalog: () -> Unit,
)

/** Значение уходит в сессию только по «Готово». */
@Composable
internal fun EditorPanel(editing: EditingSetting, cars: Map<Int, CarSummary>, wide: Boolean, actions: EditorActions) {
    if (wide) {
        CmpDialog(
            onDismissRequest = actions.onDismiss,
            width = EditorDialogWidth,
            title = { EditorHeader(editing, actions.onDraftChange, actions.onDismiss) },
            text = { EditorBody(editing, cars, wide, actions) },
            confirmButton = { EditorButtons(editing, actions) },
        )
    } else {
        CmpBottomSheet(onDismissRequest = actions.onDismiss) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.screenPaddingH,
                    end = CmpSpacing.screenPaddingH,
                    bottom = CmpSpacing.screenPaddingBottom,
                ),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                EditorHeader(editing, actions.onDraftChange, actions.onDismiss)
                EditorBody(editing, cars, wide, actions)
                EditorButtons(editing, actions)
            }
        }
    }
}

@Composable
private fun EditorHeader(editing: EditingSetting, onDraftChange: (SettingValue) -> Unit, onClose: () -> Unit) {
    val setting = editing.setting
    val colors = CmpTheme.colors
    channelHeaderOf(setting)?.let { (channel, editablePolarity) ->
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            ChannelEditorHeader(
                number = setting.number,
                title = settingTitle(setting.name, channel),
                channel = channel,
                draft = editing.draft,
                editablePolarity = editablePolarity,
                onDraftChange = onDraftChange,
                onClose = onClose,
            )
            setting.description?.let { ChannelDescription(setting.id, it) }
            WasChip(editing)
        }
        return
    }
    var expanded by rememberSaveable(setting.id) { mutableStateOf(false) }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpRow(verticalAlignment = Alignment.Top) {
            CmpColumn(modifier = Modifier.weight(1f)) {
                if (setting.number.isNotEmpty()) {
                    CmpText(
                        text = setting.number,
                        style = CmpTextStyle.Additional0,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                CmpText(
                    text = setting.name,
                    style = CmpTextStyle.Headline1,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
            }
            if (setting.description != null) {
                CmpIconButton(
                    onClick = { expanded = !expanded },
                    icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                    contentDescription = stringResource(
                        if (expanded) {
                            Strings.string.settings_edit_a11y_collapse_description
                        } else {
                            Strings.string.settings_edit_a11y_expand_description
                        },
                    ),
                )
            }
            CloseButton(onClose)
        }
        setting.description?.let { description ->
            CmpText(
                text = description,
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        WasChip(editing)
    }
}

@Composable
private fun ChannelDescription(settingId: Int, text: String) {
    var expanded by rememberSaveable(settingId) { mutableStateOf(false) }
    CmpText(
        text = text,
        style = CmpTextStyle.Paragraph2,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded },
    )
}

/** «Было: …» — место занято всегда, чтобы содержимое не прыгало; выделяется, когда значение отличается. */
@Composable
private fun WasChip(editing: EditingSetting) {
    val colors = CmpTheme.colors
    val differs = editing.draft != editing.original
    CmpText(
        text = stringResource(Strings.string.settings_edit_label_was, valueText(editing.setting, editing.original)),
        style = CmpTextStyle.Paragraph3,
        color = if (differs) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(
                if (differs) colors.brandPrimaryHighlight else colors.backgroundSurfaceHighlight,
                RoundedCornerShape(CmpRadius.tags),
            )
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
    )
}

@Suppress("CyclomaticComplexMethod") // плоский выбор редактора по типу настройки
@Composable
private fun EditorBody(editing: EditingSetting, cars: Map<Int, CarSummary>, wide: Boolean, actions: EditorActions) {
    val onDraftChange = actions.onDraftChange
    val setting = editing.setting
    val draft = editing.draft
    CmpColumn(modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin)) {
        when {
            setting is IntegerSetting && draft is SettingValue.Integer ->
                IntegerEditor(setting, draft.value) { onDraftChange(SettingValue.Integer(it)) }

            setting is DecimalSetting && draft is SettingValue.Decimal ->
                DecimalEditor(setting, draft.value) { onDraftChange(SettingValue.Decimal(it)) }

            setting is OptionSetting && draft is SettingValue.Option ->
                OptionEditor(setting, draft.index) { onDraftChange(SettingValue.Option(it)) }

            setting is TextSetting && draft is SettingValue.Text ->
                TextEditor(setting, draft.value) { onDraftChange(SettingValue.Text(it)) }

            setting is TimeSetting && draft is SettingValue.Time ->
                TimeEditor(draft.value) { onDraftChange(SettingValue.Time(it)) }

            setting is AutoStartScheduleSetting && draft is SettingValue.AutoStart ->
                ScheduleEditor(draft.value) { onDraftChange(SettingValue.AutoStart(it)) }

            setting is SirenSetting && draft is SettingValue.Siren ->
                SirenEditor(setting, draft.value) { onDraftChange(SettingValue.Siren(it)) }

            setting is CanCodeSetting && draft is SettingValue.Can -> CanCodeEditor(
                value = draft.value,
                carSearch = editing.carSearch,
                cars = cars,
                onCodeChange = { code -> onDraftChange(SettingValue.Can(CanCode(code, carId = null))) },
                onSearch = actions.onCarSearch,
                onPick = actions.onCarPick,
                onPickFromCatalog = actions.onPickFromCatalog,
            )

            setting is InputSetting && draft is SettingValue.Input ->
                ChannelFunctionsEditor(setting.availableLogics, draft.value.logicsState, searchable = wide) { state ->
                    onDraftChange(SettingValue.Input(draft.value.copy(logicsState = state)))
                }

            setting is OutputSetting && draft is SettingValue.Output ->
                ChannelFunctionsEditor(setting.availableLogics, draft.value.logicsState, searchable = wide) { state ->
                    onDraftChange(SettingValue.Output(draft.value.copy(logicsState = state)))
                }

            else -> Unit
        }
    }
}

@Composable
private fun EditorButtons(editing: EditingSetting, actions: EditorActions) {
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpButton(
            text = stringResource(Strings.string.settings_edit_btn_reset),
            onClick = actions.onReset,
            enabled = editing.draft != editing.original,
            type = CmpButtonType.Secondary,
            modifier = Modifier.weight(1f).testTag(SettingsTestTags.EDIT_RESET),
        )
        CmpButton(
            text = stringResource(Strings.string.settings_edit_btn_done),
            onClick = actions.onApply,
            enabled = editing.canApply,
            modifier = Modifier.weight(1f).testTag(SettingsTestTags.EDIT_DONE),
        )
    }
}
