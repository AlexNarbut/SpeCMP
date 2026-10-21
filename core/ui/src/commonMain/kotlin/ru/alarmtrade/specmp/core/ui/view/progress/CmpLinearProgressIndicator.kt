package ru.alarmtrade.specmp.core.ui.view.progress

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * @param progress null means indeterminate
 */
@Composable
fun CmpLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified
) {
    val resolvedColor = if (color != Color.Unspecified) color else ProgressIndicatorDefaults.linearColor
    val resolvedTrack = if (trackColor != Color.Unspecified) trackColor else ProgressIndicatorDefaults.linearTrackColor

    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack
        )
    }
}
