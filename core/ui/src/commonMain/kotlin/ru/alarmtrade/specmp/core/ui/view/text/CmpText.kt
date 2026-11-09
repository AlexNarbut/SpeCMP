@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

enum class CmpTextStyle {
    Title,

    BodyLarge,
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
            CmpTextStyle.BodyLarge -> MaterialTheme.typography.bodyLarge
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
            maxLines = maxLines,
        )
    }
}

@Composable
fun CmpText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: CmpTextStyle = CmpTextStyle.Body,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = style.toTextStyle(),
        fontWeight = fontWeight,
    )
}

@Composable
private fun CmpTextStyle.toTextStyle(): TextStyle = when (this) {
    CmpTextStyle.Title -> MaterialTheme.typography.titleLarge
    CmpTextStyle.BodyLarge -> MaterialTheme.typography.bodyLarge
    CmpTextStyle.Body -> MaterialTheme.typography.bodyMedium
    CmpTextStyle.Caption -> MaterialTheme.typography.bodySmall
}
