package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_apply_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_apply_title
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_cloud
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_file
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_btn_save
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_btn_save_as
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_btn_dont_save
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_msg_save
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_msg_save_as
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_title_save
import ru.alarmtrade.specmp.core.localization.resources.settings_form_title_new
import ru.alarmtrade.specmp.core.localization.resources.settings_load_btn_other
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_file
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_file_title
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_mismatch
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_mismatch_title
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_other
import ru.alarmtrade.specmp.core.localization.resources.settings_load_error_other_title
import ru.alarmtrade.specmp.core.localization.resources.settings_load_hint
import ru.alarmtrade.specmp.core.localization.resources.settings_load_subtitle_cloud
import ru.alarmtrade.specmp.core.localization.resources.settings_load_subtitle_defaults
import ru.alarmtrade.specmp.core.localization.resources.settings_load_subtitle_file
import ru.alarmtrade.specmp.core.localization.resources.settings_load_subtitle_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_load_title
import ru.alarmtrade.specmp.core.localization.resources.settings_load_title_defaults
import ru.alarmtrade.specmp.core.localization.resources.settings_mismatch_btn_continue
import ru.alarmtrade.specmp.core.localization.resources.settings_mismatch_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_mismatch_title
import ru.alarmtrade.specmp.core.localization.resources.settings_overwrite_btn
import ru.alarmtrade.specmp.core.localization.resources.settings_overwrite_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_overwrite_title
import ru.alarmtrade.specmp.core.localization.resources.settings_save_error_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_save_error_title
import ru.alarmtrade.specmp.core.localization.resources.settings_save_subtitle_file
import ru.alarmtrade.specmp.core.localization.resources.settings_save_subtitle_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_save_title
import ru.alarmtrade.specmp.core.localization.resources.settings_saving_title
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationInfoDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationOptionRow
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationOptionsPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadErrorKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadSourceKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveForm
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveTarget

@Composable
internal fun LoadSourcesPanel(
    deviceName: String,
    wide: Boolean,
    onPick: (LoadSourceKind) -> Unit,
    onDismiss: () -> Unit,
) {
    ConfigurationOptionsPanel(
        title = stringResource(Strings.string.settings_load_title),
        wide = wide,
        onDismiss = onDismiss,
        footer = stringResource(Strings.string.settings_load_hint),
    ) {
        ConfigurationOptionRow(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.settings_common_value_source_mine),
            subtitle = stringResource(Strings.string.settings_load_subtitle_mine, deviceName),
            onClick = { onPick(LoadSourceKind.MINE) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.MINE)),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Cloud,
            title = stringResource(Strings.string.settings_common_value_source_cloud),
            subtitle = stringResource(Strings.string.settings_load_subtitle_cloud),
            onClick = { onPick(LoadSourceKind.CLOUD) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.CLOUD)),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Description,
            title = stringResource(Strings.string.settings_common_value_source_file),
            subtitle = stringResource(Strings.string.settings_load_subtitle_file),
            onClick = { onPick(LoadSourceKind.FILE) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.FILE)),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Factory,
            title = stringResource(Strings.string.settings_load_title_defaults),
            subtitle = stringResource(Strings.string.settings_load_subtitle_defaults, deviceName),
            onClick = { onPick(LoadSourceKind.DEFAULTS) },
            modifier = Modifier.testTag(SettingsTestTags.loadSource(LoadSourceKind.DEFAULTS)),
        )
    }
}

@Composable
internal fun SaveTargetsPanel(deviceName: String, wide: Boolean, onPick: (SaveTarget) -> Unit, onDismiss: () -> Unit) {
    ConfigurationOptionsPanel(
        title = stringResource(Strings.string.settings_save_title),
        wide = wide,
        onDismiss = onDismiss,
    ) {
        ConfigurationOptionRow(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.settings_common_value_source_mine),
            subtitle = stringResource(Strings.string.settings_save_subtitle_mine, deviceName),
            onClick = { onPick(SaveTarget.MINE) },
            modifier = Modifier.testTag(SettingsTestTags.saveTarget(SaveTarget.MINE)),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Description,
            title = stringResource(Strings.string.settings_common_value_source_file),
            subtitle = stringResource(Strings.string.settings_save_subtitle_file),
            onClick = { onPick(SaveTarget.FILE) },
            modifier = Modifier.testTag(SettingsTestTags.saveTarget(SaveTarget.FILE)),
        )
    }
}

@Composable
internal fun SaveFormDialog(form: SaveForm, actions: TransferActions) {
    ConfigurationInfoDialog(
        title = stringResource(Strings.string.settings_form_title_new),
        name = form.name,
        description = form.description,
        deviceName = form.deviceName,
        onChange = actions.onSaveFormChange,
        onConfirm = actions.onSaveFormSubmit,
        onDismiss = actions.onSaveFormDismiss,
    )
}

@Composable
internal fun ModelMismatchDialog(
    sourceDeviceName: String,
    deviceName: String,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_mismatch_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_mismatch_msg, sourceDeviceName, deviceName)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.settings_mismatch_btn_continue),
                onClick = onContinue,
                modifier = Modifier.testTag(SettingsTestTags.MISMATCH_CONTINUE),
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
        },
    )
}

@Composable
internal fun LoadErrorDialog(kind: LoadErrorKind, onOtherSource: () -> Unit, onDismiss: () -> Unit) {
    val (title, text) = when (kind) {
        LoadErrorKind.MISMATCH ->
            Strings.string.settings_load_error_mismatch_title to Strings.string.settings_load_error_mismatch

        LoadErrorKind.FILE -> Strings.string.settings_load_error_file_title to Strings.string.settings_load_error_file

        LoadErrorKind.OTHER ->
            Strings.string.settings_load_error_other_title to
                Strings.string.settings_load_error_other
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(text)) },
        confirmButton = {
            CmpButton(text = stringResource(Strings.string.settings_load_btn_other), onClick = onOtherSource)
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_close), onClick = onDismiss)
        },
    )
}

@Composable
internal fun SaveErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_save_error_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_save_error_msg)) },
        confirmButton = {
            CmpButton(text = stringResource(Strings.string.settings_common_btn_retry), onClick = onRetry)
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_close), onClick = onDismiss)
        },
    )
}

@Composable
internal fun OverwriteDialog(fileName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_overwrite_title), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_overwrite_msg, fileName)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.settings_overwrite_btn),
                onClick = onConfirm,
                modifier = Modifier.testTag(SettingsTestTags.OVERWRITE_CONFIRM),
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
        },
    )
}

@Composable
internal fun ExitSaveDialog(
    changed: Int,
    saveAs: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_exit_title_save), onClose = onDismiss) },
        text = {
            CmpText(
                text = stringResource(
                    if (saveAs) Strings.string.settings_exit_msg_save_as else Strings.string.settings_exit_msg_save,
                    changed,
                ),
            )
        },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpButton(
                    text = stringResource(
                        if (saveAs) {
                            Strings.string.settings_editor_btn_save_as
                        } else {
                            Strings.string.settings_editor_btn_save
                        },
                    ),
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_SAVE),
                )
                CmpOutlinedButton(
                    text = stringResource(Strings.string.settings_exit_btn_dont_save),
                    onClick = onDiscard,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_DISCARD),
                )
                CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
            }
        },
    )
}

@Composable
internal fun TransferProgressDialog(applying: Boolean) {
    CmpDialog(
        onDismissRequest = {},
        title = {
            CmpText(
                text = stringResource(
                    if (applying) Strings.string.settings_apply_title else Strings.string.settings_saving_title,
                ),
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (applying) CmpText(text = stringResource(Strings.string.settings_apply_msg))
            }
        },
        confirmButton = {},
    )
}
