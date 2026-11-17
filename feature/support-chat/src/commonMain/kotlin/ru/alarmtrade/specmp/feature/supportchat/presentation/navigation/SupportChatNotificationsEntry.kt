package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_notifications_btn_later
import ru.alarmtrade.specmp.core.localization.resources.supportchat_notifications_btn_settings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_notifications_msg_disabled
import ru.alarmtrade.specmp.core.localization.resources.supportchat_notifications_title_disabled
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatNotifier
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.notifications.SupportChatNotificationsViewModel

/**
 * Уведомления о сообщениях техподдержки, пока пользователь в аккаунте: вызывать в авторизованной зоне, вне записей
 * бэкстека — VM живёт с хранилищем зоны. [notifier] берётся при первом создании VM.
 */
@Composable
fun SupportChatNotificationsEntry(notifier: SupportChatNotifier) {
    val viewModel = koinViewModel<SupportChatNotificationsViewModel> { parametersOf(notifier) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.askToEnable) {
        CmpDialog(
            onDismissRequest = viewModel::onDismiss,
            title = {
                CmpText(
                    text = stringResource(Strings.string.supportchat_notifications_title_disabled),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.supportchat_notifications_msg_disabled),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            confirmButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.supportchat_notifications_btn_settings),
                    onClick = viewModel::onOpenSettings,
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.supportchat_notifications_btn_later),
                    onClick = viewModel::onDismiss,
                )
            },
        )
    }
}
