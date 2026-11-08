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
import ru.alarmtrade.specmp.core.localization.resources.settings_can_btn_single_update
import ru.alarmtrade.specmp.core.localization.resources.settings_can_msg_single
import ru.alarmtrade.specmp.core.localization.resources.settings_can_title_single
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_btn_discard
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_btn_write
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_exit_title
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_btn_confirm
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_msg_confirm_changes
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_msg_error
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_msg_progress
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_title_error
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_title_progress
import ru.alarmtrade.specmp.core.localization.resources.settings_write_msg_error
import ru.alarmtrade.specmp.core.localization.resources.settings_write_msg_progress
import ru.alarmtrade.specmp.core.localization.resources.settings_write_title_error
import ru.alarmtrade.specmp.core.localization.resources.settings_write_title_progress
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags

/** Описание настройки или раздела (по ⓘ). */
@Composable
internal fun DescriptionDialog(node: SettingNode, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(
                text = if (node.number.isEmpty()) node.name else "${node.number} ${node.name}",
                onClose = onDismiss,
            )
        },
        text = { CmpText(text = node.description.orEmpty()) },
        confirmButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_ok), onClick = onDismiss)
        },
    )
}

/** Выход с незаписанными изменениями: записать и выйти / выйти без записи / отмена. */
@Composable
internal fun ExitDialog(changed: Int, onWrite: () -> Unit, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(text = stringResource(Strings.string.settings_exit_title, changed), onClose = onDismiss)
        },
        text = { CmpText(text = stringResource(Strings.string.settings_exit_msg)) },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpButton(
                    text = stringResource(Strings.string.settings_exit_btn_write),
                    onClick = onWrite,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_WRITE),
                )
                CmpOutlinedButton(
                    text = stringResource(Strings.string.settings_exit_btn_discard),
                    onClick = onDiscard,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_DISCARD),
                )
                CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
            }
        },
    )
}

/** Блокирующий прогресс записи: SDK не отдаёт процентов, индикатор неопределённый. */
@Composable
internal fun WriteProgressDialog() {
    CmpDialog(
        onDismissRequest = {},
        title = { CmpText(text = stringResource(Strings.string.settings_write_title_progress)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                CmpText(text = stringResource(Strings.string.settings_write_msg_progress))
            }
        },
        confirmButton = {},
    )
}

@Composable
internal fun WriteErrorDialog(changed: Int, onRetry: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_write_title_error), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_write_msg_error, changed)) },
        confirmButton = {
            CmpButton(text = stringResource(Strings.string.settings_common_btn_retry), onClick = onRetry)
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_close), onClick = onDismiss)
        },
    )
}

/** Подтверждение сброса блока к заводским: незаписанные изменения пропадут, отменить нельзя. */
@Composable
internal fun ResetConfirmDialog(changed: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(text = stringResource(Strings.string.settings_reset_title_confirm), onClose = onDismiss)
        },
        text = {
            CmpText(
                text = if (changed > 0) {
                    stringResource(Strings.string.settings_reset_msg_confirm_changes, changed)
                } else {
                    stringResource(Strings.string.settings_reset_msg_confirm)
                },
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.settings_reset_btn_confirm),
                onClick = onConfirm,
                modifier = Modifier.testTag(SettingsTestTags.RESET_CONFIRM),
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
        },
    )
}

/** Блокирующий прогресс сброса (SDK не отдаёт процентов). */
@Composable
internal fun ResetProgressDialog() {
    CmpDialog(
        onDismissRequest = {},
        title = { CmpText(text = stringResource(Strings.string.settings_reset_title_progress)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                CmpText(text = stringResource(Strings.string.settings_reset_msg_progress))
            }
        },
        confirmButton = {},
    )
}

@Composable
internal fun ResetErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_reset_title_error), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_reset_msg_error)) },
        confirmButton = {
            CmpButton(text = stringResource(Strings.string.settings_common_btn_retry), onClick = onRetry)
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_close), onClick = onDismiss)
        },
    )
}

/** Единая CAN-прошивка: CAN-код меняется прошивкой CAN под автомобиль — переход к обновлению прошивки. */
@Composable
internal fun SingleCanFirmwareDialog(canCode: String, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(text = stringResource(Strings.string.settings_can_title_single), onClose = onDismiss) },
        text = { CmpText(text = stringResource(Strings.string.settings_can_msg_single, canCode)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.settings_can_btn_single_update),
                onClick = onUpdate,
                modifier = Modifier.testTag(SettingsTestTags.CAN_SINGLE_UPDATE),
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
        },
    )
}
