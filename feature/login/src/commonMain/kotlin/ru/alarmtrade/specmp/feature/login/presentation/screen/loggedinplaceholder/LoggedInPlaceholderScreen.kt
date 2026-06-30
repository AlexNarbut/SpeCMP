package ru.alarmtrade.specmp.feature.login.presentation.screen.loggedinplaceholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorMessage
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_btn_logout
import specmp.feature.login.generated.resources.login_common_error_logout_failed
import specmp.feature.login.generated.resources.login_common_loading_logout
import specmp.feature.login.generated.resources.login_loggedin_msg_placeholder
import specmp.feature.login.generated.resources.login_loggedin_title_screen

@Composable
fun LoggedInPlaceholderScreen(
    logoutState: LCEState<Unit>,
    isLogoutLoading: Boolean,
    colors: CmpThemeColors,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpColumn(
        modifier = modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpText(
            text = stringResource(Res.string.login_loggedin_title_screen),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        CmpText(
            text = stringResource(Res.string.login_loggedin_msg_placeholder),
            fontSize = 16.sp,
            color = colors.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )

        if (logoutState is LCEState.Error) {
            AuthErrorMessage(
                text = stringResource(Res.string.login_common_error_logout_failed),
                colors = colors,
            )
        }

        CmpButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLogoutLoading,
        ) {
            if (isLogoutLoading) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CmpCircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary,
                    )
                    CmpText(text = stringResource(Res.string.login_common_loading_logout))
                }
            } else {
                CmpText(text = stringResource(Res.string.login_common_btn_logout))
            }
        }
    }
}
