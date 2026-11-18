package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.max
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpControlStyle
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.rememberCmpAutoSize
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

/** Primary — заливка чернилами, Accent — синяя, Secondary — ступень чернил + рамка, Tertiary — без фона,
 * TertiaryDanger — без фона красным (отмена, удаление). */
enum class CmpButtonType { Primary, Accent, Secondary, Tertiary, Danger, TertiaryDanger }

/** S 48 · M 52 · L 52 · XL 56 (высоты — CmpSize.button*); подпись S/M — Headline3, L — Headline2, XL — Headline1. */
enum class CmpButtonSize(internal val height: Dp, internal val padding: Dp, internal val label: CmpTextStyle) {
    S(CmpSize.buttonS, CmpSpacing.space20, CmpTextStyle.Headline3),
    M(CmpSize.buttonM, CmpSpacing.space24, CmpTextStyle.Headline3),
    L(CmpSize.buttonL, CmpSpacing.space24, CmpTextStyle.Headline2),
    XL(CmpSize.buttonXl, CmpSpacing.space32, CmpTextStyle.Headline1),
}

private val MinWidth = 96.dp
private val OutlinedBorder = 2.dp
private const val TONAL_ALPHA = 0.2f
private val SpinnerSize = 26.dp
private val SpinnerStroke = 3.dp
private const val LABEL_TRACKING = 0.03f

/**
 * Кнопка дизайн-системы: подпись капсом в стиле размера; `loading` прячет подпись (ширина та же) + спиннер,
 * с [loadingText] — спиннер и этот текст вместо подписи («Отправляем…»). В тесноте сначала сужаются боковые отступы
 * (до 12dp), затем уменьшается подпись, затем скрывается иконка, и лишь потом подпись обрезается многоточием.
 */
@Composable
fun CmpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: CmpButtonType = CmpButtonType.Primary,
    size: CmpButtonSize = CmpButtonSize.L,
    loading: Boolean = false,
    icon: CmpIcons? = null,
    fullWidth: Boolean = false,
    maxLines: Int = 1,
    uppercase: Boolean = true,
    loadingText: String? = null,
) {
    val showLoadingText = loading && loadingText != null
    val label = if (loading && loadingText != null) loadingText else text
    CmpButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !showLoadingText,
        type = type,
        size = size,
        loading = loading && !showLoadingText,
        fullWidth = fullWidth,
    ) {
        val shown = if (uppercase) label.uppercase() else label
        ButtonLabelLayout(
            label = shown,
            gap = CmpSpacing.space8,
            hideLeading = !showLoadingText && maxLines == 1,
            leading = when {
                showLoadingText -> {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(CmpSize.iconM),
                            color = LocalContentColor.current,
                            strokeWidth = SpinnerStroke,
                        )
                    }
                }

                icon != null -> {
                    { CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconM)) }
                }

                else -> null
            },
        ) {
            Text(
                text = shown,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                autoSize = rememberCmpAutoSize(enabled = true, style = LocalTextStyle.current, maxLines = maxLines),
            )
        }
    }
}

@Composable
fun CmpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: CmpButtonType = CmpButtonType.Primary,
    size: CmpButtonSize = CmpButtonSize.L,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.buttons)
    val style = LocalCmpControlStyle.current
    val look = buttonLook(type, colors, enabled = enabled, loading = loading)
        .filled(type, style.buttonFill, colors, active = enabled && !loading)
    val labelStyle = size.label.toTextStyle().copy(letterSpacing = LABEL_TRACKING.em)
    Box(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = max(size.height + style.buttonHeight, CmpSize.touchMin))
            .defaultMinSize(minWidth = MinWidth)
            .cmpPressable(onClick = onClick, enabled = enabled && !loading, shape = shape, role = Role.Button)
            .background(look.container, shape)
            .cmpBorder(look.borderWidth, look.border, shape)
            .adaptiveHorizontalPadding(
                full = max(size.padding + style.buttonPadding, CmpSpacing.space8),
                min = CmpSpacing.space12,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides look.content, LocalTextStyle provides labelStyle) {
            Row(
                modifier = Modifier.alpha(if (loading) 0f else 1f),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(SpinnerSize),
                color = colors.brandPrimaryDefault,
                trackColor = colors.borderLight,
                strokeWidth = SpinnerStroke,
            )
        }
    }
}

private class ButtonLook(val container: Color, val content: Color, val border: Color, val borderWidth: Dp)

/** Заливка темы: у Primary (чернила) и Accent (акцент); остальные типы и неактивные состояния — как есть. */
private fun ButtonLook.filled(type: CmpButtonType, fill: ButtonFill, c: CmpColors, active: Boolean): ButtonLook {
    if (fill == ButtonFill.Filled || !active) return this
    val base = when (type) {
        CmpButtonType.Primary -> c.foregroundOnDarkPrimaryDefault
        CmpButtonType.Accent -> c.brandPrimaryDefault
        else -> return this
    }
    return when (fill) {
        ButtonFill.Tonal -> ButtonLook(
            base.copy(alpha = TONAL_ALPHA),
            c.foregroundOnDarkPrimaryDefault,
            Color.Transparent,
            0.dp,
        )

        else -> ButtonLook(Color.Transparent, base, base, OutlinedBorder)
    }
}

private fun buttonLook(type: CmpButtonType, c: CmpColors, enabled: Boolean, loading: Boolean): ButtonLook = when {
    loading -> ButtonLook(Color.Transparent, c.foregroundOnDarkPrimaryDefault, c.borderHighlight, CmpBorderWidth.inputs)

    !enabled -> ButtonLook(
        container = if (type == CmpButtonType.Tertiary ||
            type == CmpButtonType.TertiaryDanger
        ) {
            Color.Transparent
        } else {
            c.backgroundSurfaceUltraHighlight
        },
        content = c.foregroundOnDarkPrimaryDisabled,
        border = Color.Transparent,
        borderWidth = 0.dp,
    )

    else -> when (type) {
        CmpButtonType.Primary ->
            ButtonLook(c.backgroundSurfacePrimary, c.foregroundOnLightPrimaryDefault, Color.Transparent, 0.dp)

        CmpButtonType.Accent -> ButtonLook(c.brandPrimaryDefault, c.onBrand, Color.Transparent, 0.dp)

        CmpButtonType.Secondary ->
            ButtonLook(
                c.backgroundSurfaceHighlight,
                c.foregroundOnDarkPrimaryDefault,
                c.borderLight,
                CmpBorderWidth.inputs,
            )

        CmpButtonType.Tertiary -> ButtonLook(
            Color.Transparent,
            c.foregroundOnDarkPrimaryDefault,
            Color.Transparent,
            0.dp,
        )

        CmpButtonType.Danger -> ButtonLook(c.errorDefault, c.staticWhite, Color.Transparent, 0.dp)

        CmpButtonType.TertiaryDanger -> ButtonLook(Color.Transparent, c.errorDefault, Color.Transparent, 0.dp)
    }
}
