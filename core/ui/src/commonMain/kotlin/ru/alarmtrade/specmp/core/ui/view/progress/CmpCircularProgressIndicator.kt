package ru.alarmtrade.specmp.core.ui.view.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

/** [progress] = null — неопределённый индикатор. */
@Composable
fun CmpCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    strokeWidth: Dp = 4.dp,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            strokeWidth = strokeWidth,
            color = if (color != Color.Unspecified) {
                color
            } else {
                CmpTheme.colors.brandPrimaryDefault
            },
            trackColor = if (trackColor != Color.Unspecified) {
                trackColor
            } else {
                CmpTheme.colors.borderHighlight
            },
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            strokeWidth = strokeWidth,
            color = if (color != Color.Unspecified) {
                color
            } else {
                CmpTheme.colors.brandPrimaryDefault
            },
            trackColor = if (trackColor != Color.Unspecified) {
                trackColor
            } else {
                CmpTheme.colors.borderHighlight
            },
        )
    }
}
