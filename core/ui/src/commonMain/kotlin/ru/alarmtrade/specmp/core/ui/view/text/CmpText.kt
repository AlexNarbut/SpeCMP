package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Типографические роли (U-6): маппинг на `MaterialTheme.typography` внутри
 * обёртки, чтобы фичи не задавали размеры числами и шкала шрифтов не
 * расходилась между экранами.
 */
enum class CmpTextStyle {
    Title,
    Body,
    Caption,
}

@Composable
fun CmpText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: CmpTextStyle? = null,
) {
    if (style != null) {
        val textStyle: TextStyle = when (style) {
            CmpTextStyle.Title -> MaterialTheme.typography.titleLarge
            CmpTextStyle.Body -> MaterialTheme.typography.bodyMedium
            CmpTextStyle.Caption -> MaterialTheme.typography.bodySmall
        }
        Text(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
            style = textStyle,
            fontWeight = fontWeight,
        )
    } else {
        Text(
            text = text,
            modifier = modifier,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}