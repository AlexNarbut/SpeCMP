@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

/**
 * Шкала стилей текста (design/design-system/tokens/typography.css). Размер, межстрочный, вес и шрифт зашиты в стиль:
 * своих размеров и весов у текста нет — не хватает стиля, он добавляется в шкалу (docs/ui-kit.md, «Типографика»).
 */
enum class CmpTextStyle {
    /** 88 — крупные цифры, Highlight. */
    Accent1,

    /** 56. */
    Accent2,

    /** 40. */
    Accent3,

    /** 32 — заголовок экрана, Modal, ContentError, OTP. */
    Title1,

    /** 28. */
    Title2,

    /** 24 — кнопки L/XL, табы, значения инпутов, заголовок шторки. */
    Headline1,

    /** 19 — заголовок топбара, подзаголовок, значение InfoRow. */
    Headline2,

    /** 16 — кнопки S/M, чипы, хинты, жирный акцент в тексте. */
    Headline3,

    /** 19, Medium — крупный основной текст. */
    Paragraph1,

    /** 16, Medium — основной текст. */
    Paragraph2,

    /** 14, Medium — вторичный текст, описания. */
    Paragraph3,

    /** 14 — лейблы. */
    Additional0,

    /** 12 — подписи, бейджи. */
    Additional1,

    /** 10 — мелкие метки. */
    Additional2,

    /** 16/22, Roboto Mono Medium — машинный текст: строка лога. */
    Mono1,

    /** 14/18, Roboto Mono Medium — время строки лога, мелкие коды. */
    Mono2,
}

@Composable
fun CmpText(
    text: String,
    style: CmpTextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    uppercase: Boolean = false,
    autoSize: Boolean = false,
) {
    val textStyle = style.toTextStyle()
    Text(
        text = if (uppercase) text.uppercase() else text,
        modifier = modifier,
        color = color,
        autoSize = rememberCmpAutoSize(autoSize, textStyle, maxLines),
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = textStyle,
    )
}

@Composable
fun CmpText(
    text: AnnotatedString,
    style: CmpTextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    autoSize: Boolean = false,
) {
    val textStyle = style.toTextStyle()
    Text(
        text = text,
        modifier = modifier,
        color = color,
        autoSize = rememberCmpAutoSize(autoSize, textStyle, maxLines),
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = textStyle,
    )
}

@Composable
internal fun CmpTextStyle.toTextStyle(): TextStyle = CmpTheme.typography.of(this)
