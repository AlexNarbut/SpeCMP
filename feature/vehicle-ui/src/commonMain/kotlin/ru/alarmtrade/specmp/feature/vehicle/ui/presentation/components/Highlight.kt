package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

@Composable
internal fun highlighted(text: String, query: String): AnnotatedString {
    val colors = CmpTheme.colors
    val color = colors.brandPrimaryDefault
    val background = colors.brandPrimaryHighlight
    return remember(text, query, color, background) { highlight(text, query, color, background) }
}

internal fun highlight(
    text: String,
    query: String,
    color: Color,
    background: Color = Color.Unspecified,
): AnnotatedString {
    val q = query.trim()
    val start = if (q.isEmpty()) -1 else text.indexOf(q, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color, background = background)) {
            append(
                text.substring(
                    start,
                    start + q.length,
                ),
            )
        }
        append(text.substring(start + q.length))
    }
}
