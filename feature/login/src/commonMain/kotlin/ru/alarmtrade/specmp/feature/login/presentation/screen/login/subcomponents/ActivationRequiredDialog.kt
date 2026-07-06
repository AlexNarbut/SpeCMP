package ru.alarmtrade.specmp.feature.login.presentation.screen.login.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenState
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_btn_close
import specmp.feature.login.generated.resources.login_common_error_activation_resend_failed
import specmp.feature.login.generated.resources.login_common_loading_send_email
import specmp.feature.login.generated.resources.login_login_btn_resend_activation
import specmp.feature.login.generated.resources.login_login_msg_activation_email_sent
import specmp.feature.login.generated.resources.login_login_msg_activation_required
import specmp.feature.login.generated.resources.login_login_title_confirm_email

@Composable
fun ActivationRequiredDialog(
    state: LoginScreenState,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
) {
    val resendSucceeded = state.activationResendState is LCEState.Content
    val resendFailed = state.activationResendError != null

    CmpDialog(
        onDismissRequest = onDismissActivationRequest,
        icon = CmpIcons.Email.imageVector(),
        title = {
            CmpText(
                text = stringResource(Res.string.login_login_title_confirm_email),
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Один текстовый слот: сообщение заменяется по состоянию отправки.
                CmpText(
                    text = when {
                        resendSucceeded ->
                            stringResource(Res.string.login_login_msg_activation_email_sent)
                        resendFailed ->
                            stringResource(Res.string.login_common_error_activation_resend_failed)
                        else ->
                            stringResource(Res.string.login_login_msg_activation_required)
                    },
                )
                if (state.isActivationResendLoading) {
                    CmpRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CmpCircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        CmpText(text = stringResource(Res.string.login_common_loading_send_email))
                    }
                }
            }
        },
        confirmButton = {
            if (!resendSucceeded) {
                CmpTextButton(
                    text = stringResource(Res.string.login_login_btn_resend_activation),
                    onClick = onResendActivationEmail,
                    enabled = !state.isActivationResendLoading,
                )
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.login_common_btn_close),
                onClick = onDismissActivationRequest,
                enabled = !state.isActivationResendLoading,
            )
        },
    )
}
