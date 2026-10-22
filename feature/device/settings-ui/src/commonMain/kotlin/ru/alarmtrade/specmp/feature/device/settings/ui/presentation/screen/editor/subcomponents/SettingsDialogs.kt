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
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_cancel
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_close
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_ok
import specmp.feature.device.settings_ui.generated.resources.settings_common_btn_retry
import specmp.feature.device.settings_ui.generated.resources.settings_exit_btn_discard
import specmp.feature.device.settings_ui.generated.resources.settings_exit_btn_write
import specmp.feature.device.settings_ui.generated.resources.settings_exit_msg
import specmp.feature.device.settings_ui.generated.resources.settings_exit_title
import specmp.feature.device.settings_ui.generated.resources.settings_write_msg_error
import specmp.feature.device.settings_ui.generated.resources.settings_write_msg_progress
import specmp.feature.device.settings_ui.generated.resources.settings_write_title_error
import specmp.feature.device.settings_ui.generated.resources.settings_write_title_progress

/** Описание настройки или раздела (по ⓘ). */
@Composable
internal fun DescriptionDialog(node: SettingNode, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = if (node.number.isEmpty()) node.name else "${node.number} ${node.name}") },
        text = { CmpText(text = node.description.orEmpty()) },
        confirmButton = { CmpTextButton(text = stringResource(Res.string.settings_common_btn_ok), onClick = onDismiss) }
    )
}

/** Выход с незаписанными изменениями: записать и выйти / выйти без записи / отмена. */
@Composable
internal fun ExitDialog(changed: Int, onWrite: () -> Unit, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = stringResource(Res.string.settings_exit_title, changed)) },
        text = { CmpText(text = stringResource(Res.string.settings_exit_msg)) },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CmpButton(
                    text = stringResource(Res.string.settings_exit_btn_write),
                    onClick = onWrite,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_WRITE)
                )
                CmpOutlinedButton(
                    text = stringResource(Res.string.settings_exit_btn_discard),
                    onClick = onDiscard,
                    modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.EXIT_DISCARD)
                )
                CmpTextButton(text = stringResource(Res.string.settings_common_btn_cancel), onClick = onDismiss)
            }
        }
    )
}

/** Блокирующий прогресс записи: SDK не отдаёт процентов, индикатор неопределённый. */
@Composable
internal fun WriteProgressDialog() {
    CmpDialog(
        onDismissRequest = {},
        title = { CmpText(text = stringResource(Res.string.settings_write_title_progress)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                CmpText(text = stringResource(Res.string.settings_write_msg_progress))
            }
        },
        confirmButton = {}
    )
}

@Composable
internal fun WriteErrorDialog(changed: Int, onRetry: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = stringResource(Res.string.settings_write_title_error)) },
        text = { CmpText(text = stringResource(Res.string.settings_write_msg_error, changed)) },
        confirmButton = { CmpButton(text = stringResource(Res.string.settings_common_btn_retry), onClick = onRetry) },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.settings_common_btn_close), onClick = onDismiss)
        }
    )
}
