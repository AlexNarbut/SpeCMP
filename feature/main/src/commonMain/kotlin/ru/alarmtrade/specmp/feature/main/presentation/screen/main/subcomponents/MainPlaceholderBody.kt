package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenState
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.main_main_btn_logout
import specmp.feature.main.generated.resources.main_main_error_logout_failed
import specmp.feature.main.generated.resources.main_main_loading_logout
import specmp.feature.main.generated.resources.main_main_msg_placeholder
import specmp.feature.main.generated.resources.main_main_title_screen

@Composable
fun MainPlaceholderBody(
    state: MainScreenState,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors(),
) {
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CmpText(
                text = stringResource(Res.string.main_main_title_screen),
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
            )

            CmpText(
                text = stringResource(Res.string.main_main_msg_placeholder),
                fontSize = 16.sp,
                color = colors.onBackground.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )

            if (state.isLogoutFailed) {
                MainLogoutErrorMessage(colors = colors)
            }

            MainLogoutButton(
                isLogoutInProgress = state.isLogoutInProgress,
                colors = colors,
                onLogout = onLogout,
            )
        }
    }
}

@Composable
private fun MainLogoutErrorMessage(
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.error,
        contentColor = colors.onError,
    ) {
        CmpText(
            text = stringResource(Res.string.main_main_error_logout_failed),
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun MainLogoutButton(
    isLogoutInProgress: Boolean,
    colors: CmpThemeColors,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpButton(
        onClick = onLogout,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = !isLogoutInProgress,
    ) {
        if (isLogoutInProgress) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmpCircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary,
                )
                CmpText(text = stringResource(Res.string.main_main_loading_logout))
            }
        } else {
            CmpText(text = stringResource(Res.string.main_main_btn_logout))
        }
    }
}
