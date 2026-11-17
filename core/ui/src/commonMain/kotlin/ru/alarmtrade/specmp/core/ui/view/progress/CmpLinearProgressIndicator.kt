package ru.alarmtrade.specmp.core.ui.view.progress

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

/** [progress] = null — неопределённый индикатор. */
@Composable
fun CmpLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
) {
    val resolvedColor = if (color != Color.Unspecified) color else CmpTheme.colors.brandPrimaryDefault
    val resolvedTrack = if (trackColor != Color.Unspecified) trackColor else CmpTheme.colors.borderHighlight

    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack,
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier,
            color = resolvedColor,
            trackColor = resolvedTrack,
        )
    }
}
