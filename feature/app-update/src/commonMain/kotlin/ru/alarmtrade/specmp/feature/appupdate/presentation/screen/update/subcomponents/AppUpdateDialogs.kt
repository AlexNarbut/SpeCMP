package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_btn_switch
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_btn_update
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_msg_busy
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_msg_channel
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_title_busy
import ru.alarmtrade.specmp.core.localization.resources.appupdate_dialog_title_channel
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateDialog

@Composable
internal fun AppUpdateDialogs(dialog: AppUpdateDialog?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val texts = when (dialog) {
        null -> return

        AppUpdateDialog.DeviceBusy -> DialogTexts(
            CmpIcons.Warning,
            stringResource(Strings.string.appupdate_dialog_title_busy),
            stringResource(Strings.string.appupdate_dialog_msg_busy),
            stringResource(Strings.string.appupdate_dialog_btn_update),
        )

        is AppUpdateDialog.SwitchChannel -> DialogTexts(
            CmpIcons.Science,
            stringResource(Strings.string.appupdate_dialog_title_channel, dialog.channel.name),
            stringResource(Strings.string.appupdate_dialog_msg_channel, dialog.defaultName),
            stringResource(Strings.string.appupdate_dialog_btn_switch),
        )
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = texts.icon.imageVector(),
        title = { CmpText(text = texts.title, style = CmpTextStyle.Paragraph2) },
        text = { CmpText(text = texts.text, style = CmpTextStyle.Paragraph2) },
        confirmButton = {
            CmpButton(
                text = texts.confirm,
                onClick = onConfirm,
                modifier = Modifier.testTag(AppUpdateTags.DIALOG_CONFIRM),
            )
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.appupdate_dialog_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.testTag(AppUpdateTags.DIALOG_CANCEL),
            )
        },
    )
}

private data class DialogTexts(val icon: CmpIcons, val title: String, val text: String, val confirm: String)
