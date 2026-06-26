package ru.alarmtrade.specmp.core.ui.view.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param progress null means indeterminate
 */
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
            color = if (color != Color.Unspecified) color
            else androidx.compose.material3.ProgressIndicatorDefaults.circularColor,
            trackColor = if (trackColor != Color.Unspecified) trackColor
            else androidx.compose.material3.ProgressIndicatorDefaults.circularTrackColor,
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            strokeWidth = strokeWidth,
            color = if (color != Color.Unspecified) color
            else androidx.compose.material3.ProgressIndicatorDefaults.circularColor,
            trackColor = if (trackColor != Color.Unspecified) trackColor
            else androidx.compose.material3.ProgressIndicatorDefaults.circularTrackColor,
        )
    }
}
