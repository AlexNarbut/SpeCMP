package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@Composable
fun AuthErrorMessage(
    text: String,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.error,
        contentColor = colors.onError,
    ) {
        CmpText(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
        )
    }
}

@Composable
fun AuthSuccessMessage(
    text: String,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.primary,
        contentColor = colors.onPrimary,
    ) {
        CmpText(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
        )
    }
}
