package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
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
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.accepts
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.valueText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.EditingSetting
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_edit_a11y_collapse_description
import specmp.feature.device.settings_ui.generated.resources.settings_edit_a11y_expand_description
import specmp.feature.device.settings_ui.generated.resources.settings_edit_btn_done
import specmp.feature.device.settings_ui.generated.resources.settings_edit_btn_reset
import specmp.feature.device.settings_ui.generated.resources.settings_edit_label_was

/** Ширина окна редактирования на широком экране: 50 % окна, 480–600 dp. */
private val EditorDialogWidth = CmpDialogWidth(fraction = 0.5f, min = 480.dp, max = 600.dp)

@Immutable
internal class EditorActions(
    val onDraftChange: (SettingValue) -> Unit,
    val onReset: () -> Unit,
    val onApply: () -> Unit,
    val onDismiss: () -> Unit,
    val onCarSearch: () -> Unit,
    val onCarPick: (carId: Int) -> Unit,
    val onPickFromCatalog: () -> Unit
)

/**
 * Окно редактирования настройки: нижняя шторка на компактном экране, диалог — на широком. Шапка (номер, название,
 * описание, «Было: …»), содержимое по типу, «Сбросить» / «Готово». Значение уходит в сессию только по «Готово».
 */
@Composable
internal fun EditorPanel(editing: EditingSetting, cars: Map<Int, CarSummary>, wide: Boolean, actions: EditorActions) {
    if (wide) {
        CmpDialog(
            onDismissRequest = actions.onDismiss,
            width = EditorDialogWidth,
            title = { EditorHeader(editing, actions.onDraftChange, actions.onDismiss) },
            text = { EditorBody(editing, cars, wide, actions) },
            confirmButton = { EditorButtons(editing, actions) }
        )
    } else {
        CmpBottomSheet(onDismissRequest = actions.onDismiss) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val colors = cmpThemeColors()
    channelHeaderOf(setting)?.let { (channel, editablePolarity) ->
        CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChannelEditorHeader(
                number = setting.number,
                title = if (channel.name.isBlank() || channel.name == setting.name) {
                    setting.name
                } else {
                    "${channel.name} · ${setting.name}"
                },
                channel = channel,
                draft = editing.draft,
                editablePolarity = editablePolarity,
                onDraftChange = onDraftChange,
                onClose = onClose
            )
            setting.description?.let { ChannelDescription(setting.id, it) }
            WasChip(editing)
        }
        return
    }
    var expanded by rememberSaveable(setting.id) { mutableStateOf(false) }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(verticalAlignment = Alignment.Top) {
            CmpColumn(modifier = Modifier.weight(1f)) {
                if (setting.number.isNotEmpty()) {
                    CmpText(text = setting.number, style = CmpTextStyle.Caption, color = colors.onSurfaceVariant)
                }
                CmpText(text = setting.name, style = CmpTextStyle.Title, color = colors.onSurface)
            }
            if (setting.description != null) {
                CmpIconButton(
                    onClick = { expanded = !expanded },
                    icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                    contentDescription = stringResource(
                        if (expanded) {
                            Res.string.settings_edit_a11y_collapse_description
                        } else {
                            Res.string.settings_edit_a11y_expand_description
                        }
                    )
                )
            }
            CloseButton(onClose)
        }
        setting.description?.let { description ->
            CmpText(
                text = description,
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WasChip(editing)
    }
}

/** Описание канала: две строки, по нажатию — целиком. */
@Composable
private fun ChannelDescription(settingId: Int, text: String) {
    var expanded by rememberSaveable(settingId) { mutableStateOf(false) }
    CmpText(
        text = text,
        style = CmpTextStyle.Body,
        color = cmpThemeColors().onSurfaceVariant,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clickable(role = Role.Button) { expanded = !expanded }
    )
}

/** «Было: …» — место занято всегда, чтобы содержимое не прыгало; выделяется, когда значение отличается. */
@Composable
private fun WasChip(editing: EditingSetting) {
    val colors = cmpThemeColors()
    val differs = editing.draft != editing.original
    CmpText(
        text = stringResource(Res.string.settings_edit_label_was, valueText(editing.setting, editing.original)),
        style = CmpTextStyle.Caption,
        color = if (differs) colors.onTertiaryContainer else colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(if (differs) colors.tertiaryContainer else colors.surfaceVariant, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Suppress("CyclomaticComplexMethod") // плоский выбор редактора по типу настройки
@Composable
private fun EditorBody(editing: EditingSetting, cars: Map<Int, CarSummary>, wide: Boolean, actions: EditorActions) {
    val onDraftChange = actions.onDraftChange
    val setting = editing.setting
    val draft = editing.draft
    CmpColumn(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
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
                onPickFromCatalog = actions.onPickFromCatalog
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
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpOutlinedButton(
            text = stringResource(Res.string.settings_edit_btn_reset),
            onClick = actions.onReset,
            enabled = editing.draft != editing.original,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag(SettingsTestTags.EDIT_RESET)
        )
        CmpButton(
            text = stringResource(Res.string.settings_edit_btn_done),
            onClick = actions.onApply,
            enabled = editing.setting.accepts(editing.draft),
            modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag(SettingsTestTags.EDIT_DONE)
        )
    }
}
