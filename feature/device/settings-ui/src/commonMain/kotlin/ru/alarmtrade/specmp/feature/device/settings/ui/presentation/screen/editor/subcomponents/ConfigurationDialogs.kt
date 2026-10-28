package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ConfigurationInfoDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ConfigurationOptionRow
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ConfigurationOptionsPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadErrorKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadSourceKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveForm
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveTarget
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_apply_msg
import specmp.feature.device.settings_ui.generated.resources.settings_apply_title
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_cancel
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_close
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_retry
import specmp.feature.device.settings_ui.generated.resources.settings_editor_btn_save
import specmp.feature.device.settings_ui.generated.resources.settings_editor_btn_save_as
import specmp.feature.device.settings_ui.generated.resources.settings_exit_btn_dont_save
import specmp.feature.device.settings_ui.generated.resources.settings_exit_save_msg
import specmp.feature.device.settings_ui.generated.resources.settings_exit_save_msg_as
import specmp.feature.device.settings_ui.generated.resources.settings_exit_save_title
import specmp.feature.device.settings_ui.generated.resources.settings_form_title_new
import specmp.feature.device.settings_ui.generated.resources.settings_load_btn_other
import specmp.feature.device.settings_ui.generated.resources.settings_load_cloud_sub
import specmp.feature.device.settings_ui.generated.resources.settings_load_defaults_sub
import specmp.feature.device.settings_ui.generated.resources.settings_load_defaults_title
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_file
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_file_title
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_mismatch
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_mismatch_title
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_other
import specmp.feature.device.settings_ui.generated.resources.settings_load_error_other_title
import specmp.feature.device.settings_ui.generated.resources.settings_load_file_sub
import specmp.feature.device.settings_ui.generated.resources.settings_load_hint
import specmp.feature.device.settings_ui.generated.resources.settings_load_mine_sub
import specmp.feature.device.settings_ui.generated.resources.settings_load_title
import specmp.feature.device.settings_ui.generated.resources.settings_mismatch_btn_continue
import specmp.feature.device.settings_ui.generated.resources.settings_mismatch_msg
import specmp.feature.device.settings_ui.generated.resources.settings_mismatch_title
import specmp.feature.device.settings_ui.generated.resources.settings_overwrite_btn
import specmp.feature.device.settings_ui.generated.resources.settings_overwrite_msg
import specmp.feature.device.settings_ui.generated.resources.settings_overwrite_title
import specmp.feature.device.settings_ui.generated.resources.settings_save_error_msg
import specmp.feature.device.settings_ui.generated.resources.settings_save_error_title
import specmp.feature.device.settings_ui.generated.resources.settings_save_file_sub
import specmp.feature.device.settings_ui.generated.resources.settings_save_mine_sub
import specmp.feature.device.settings_ui.generated.resources.settings_save_title
import specmp.feature.device.settings_ui.generated.resources.settings_saving_title
import specmp.feature.device.settings_ui.generated.resources.settings_source_cloud
import specmp.feature.device.settings_ui.generated.resources.settings_source_file
import specmp.feature.device.settings_ui.generated.resources.settings_source_mine

/** K7 «Загрузить из…»: «Мои» (под модель блока), облако, файл, заводские настройки модели. */
@Composable
internal fun LoadSourcesPanel(
    deviceName: String,
    wide: Boolean,
    onPick: (LoadSourceKind) -> Unit,
    onDismiss: () -> Unit
) {
    ConfigurationOptionsPanel(
        title = stringResource(Res.string.settings_load_title),
        wide = wide,
        onDismiss = onDismiss,
        footer = stringResource(Res.string.settings_load_hint)
    ) {
        ConfigurationOptionRow(
            icon = CmpIcons.Inventory2,
            title = stringResource(Res.string.settings_source_mine),
            subtitle = stringResource(Res.string.settings_load_mine_sub, deviceName),
            onClick = { onPick(LoadSourceKind.MINE) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.MINE))
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Cloud,
            title = stringResource(Res.string.settings_source_cloud),
            subtitle = stringResource(Res.string.settings_load_cloud_sub),
            onClick = { onPick(LoadSourceKind.CLOUD) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.CLOUD))
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Description,
            title = stringResource(Res.string.settings_source_file),
            subtitle = stringResource(Res.string.settings_load_file_sub),
            onClick = { onPick(LoadSourceKind.FILE) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.FILE))
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Factory,
            title = stringResource(Res.string.settings_load_defaults_title),
            subtitle = stringResource(Res.string.settings_load_defaults_sub, deviceName),
            onClick = { onPick(LoadSourceKind.DEFAULTS) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.DEFAULTS))
        )
    }
}

/** K6 «Сохранить в…»: «Мои конфигурации» / файл. */
@Composable
internal fun SaveTargetsPanel(deviceName: String, wide: Boolean, onPick: (SaveTarget) -> Unit, onDismiss: () -> Unit) {
    ConfigurationOptionsPanel(
        title = stringResource(Res.string.settings_save_title),
        wide = wide,
        onDismiss = onDismiss
    ) {
        ConfigurationOptionRow(
            icon = CmpIcons.Inventory2,
            title = stringResource(Res.string.settings_source_mine),
            subtitle = stringResource(Res.string.settings_save_mine_sub, deviceName),
            onClick = { onPick(SaveTarget.MINE) },
            modifier = Modifier.testTag(SettingsTestTags.saveTarget(SaveTarget.MINE))
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Description,
            title = stringResource(Res.string.settings_source_file),
            subtitle = stringResource(Res.string.settings_save_file_sub),
            onClick = { onPick(SaveTarget.FILE) },
            modifier = Modifier.testTag(SettingsTestTags.saveTarget(SaveTarget.FILE))
        )
    }
}

@Composable
internal fun SaveFormDialog(form: SaveForm, actions: TransferActions) {
    ConfigurationInfoDialog(
        title = stringResource(Res.string.settings_form_title_new),
        name = form.name,
        description = form.description,
        deviceName = form.deviceName,
        onChange = actions.onSaveFormChange,
        onConfirm = actions.onSaveFormSubmit,
        onDismiss = actions.onSaveFormDismiss
    )
}

/** Модель конфигурации ≠ модели блока: предупреждение перед сравнением. */
@Composable
internal fun ModelMismatchDialog(
    sourceDeviceName: String,
    deviceName: String,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Res.string.settings_mismatch_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Res.string.settings_mismatch_msg, sourceDeviceName, deviceName)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Res.string.settings_mismatch_btn_continue),
                onClick = onContinue,
                modifier = Modifier.testTag(SettingsTestTags.MISMATCH_CONTINUE)
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.settings_common_btn_cancel), onClick = onDismiss)
        }
    )
}

/** Не загрузилось: не подходит для блока / файл повреждён / нет связи с сервером. */
@Composable
internal fun LoadErrorDialog(kind: LoadErrorKind, onOtherSource: () -> Unit, onDismiss: () -> Unit) {
    val (title, text) = when (kind) {
        LoadErrorKind.MISMATCH ->
            Res.string.settings_load_error_mismatch_title to Res.string.settings_load_error_mismatch

        LoadErrorKind.FILE -> Res.string.settings_load_error_file_title to Res.string.settings_load_error_file

        LoadErrorKind.OTHER -> Res.string.settings_load_error_other_title to Res.string.settings_load_error_other
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(text)) },
        confirmButton = {
            CmpButton(text = stringResource(Res.string.settings_load_btn_other), onClick = onOtherSource)
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.settings_common_btn_close), onClick = onDismiss)
        }
    )
}

@Composable
internal fun SaveErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Res.string.settings_save_error_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Res.string.settings_save_error_msg)) },
        confirmButton = { CmpButton(text = stringResource(Res.string.settings_common_btn_retry), onClick = onRetry) },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.settings_common_btn_close), onClick = onDismiss)
        }
    )
}

/** «Сохранить» файла перезапишет его. */
@Composable
internal fun OverwriteDialog(fileName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Res.string.settings_overwrite_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Res.string.settings_overwrite_msg, fileName)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Res.string.settings_overwrite_btn),
                onClick = onConfirm,
                modifier = Modifier.testTag(SettingsTestTags.OVERWRITE_CONFIRM)
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.settings_common_btn_cancel), onClick = onDismiss)
        }
    )
}

/** Выход из конфигурации с несохранёнными правками: «Сохранить» (или «как…») / «Не сохранять» / «Отмена». */
@Composable
internal fun ExitSaveDialog(
    changed: Int,
    saveAs: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Res.string.settings_exit_save_title), onClose = onDismiss) },
        text = {
            CmpText(
                text = stringResource(
                    if (saveAs) Res.string.settings_exit_save_msg_as else Res.string.settings_exit_save_msg,
                    changed
                )
            )
        },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CmpButton(
                    text = stringResource(
                        if (saveAs) Res.string.settings_editor_btn_save_as else Res.string.settings_editor_btn_save
                    ),
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_SAVE)
                )
                CmpOutlinedButton(
                    text = stringResource(Res.string.settings_exit_btn_dont_save),
                    onClick = onDiscard,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_DISCARD)
                )
                CmpTextButton(text = stringResource(Res.string.settings_common_btn_cancel), onClick = onDismiss)
            }
        }
    )
}

/** Блокирующий прогресс: подставляем значения конфигурации / сохраняем. */
@Composable
internal fun TransferProgressDialog(applying: Boolean) {
    CmpDialog(
        onDismissRequest = {},
        title = {
            CmpText(
                text = stringResource(
                    if (applying) Res.string.settings_apply_title else Res.string.settings_saving_title
                )
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (applying) CmpText(text = stringResource(Res.string.settings_apply_msg))
            }
        },
        confirmButton = {}
    )
}
