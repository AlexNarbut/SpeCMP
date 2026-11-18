package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadii
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColors

/** Сдвиги размеров кнопок/чипов от отступов темы; заливка — только у Primary и Accent. */
@Immutable
internal data class CmpControlStyle(
    val buttonFill: ButtonFill = ButtonFill.Filled,
    val buttonHeight: Dp = 0.dp,
    val buttonPadding: Dp = 0.dp,
    val chipHeight: Dp = 0.dp,
    val chipPadding: Dp = 0.dp,
)

@Immutable
internal data class CmpPageBackground(
    val type: ThemeBackground = ThemeBackground.Glow,
    val direction: GradientDirection = GradientDirection.Down,
    val start: Color = Color.Unspecified,
    val end: Color = Color.Unspecified,
) {
    /** `null` — заливка цветом страницы (свечение рисуют сами экраны). */
    val brush: Brush?
        get() = if (type != ThemeBackground.Gradient) {
            null
        } else {
            when (direction) {
                GradientDirection.Down -> Brush.verticalGradient(listOf(start, end))
                GradientDirection.Diagonal -> Brush.linearGradient(listOf(start, end))
                GradientDirection.Corner -> Brush.radialGradient(listOf(end, start))
            }
        }
}

@Immutable
internal class ResolvedTheme(
    val colors: CmpColors,
    val scheme: ColorScheme,
    val radii: CmpRadii,
    val controls: CmpControlStyle,
    val textScale: Float,
    val background: CmpPageBackground,
)

internal fun ThemeModeSpec.resolve(isDark: Boolean): ResolvedTheme {
    val derivation = ThemeColorDerivation(palette, isDark)
    val colors = derivation.colors()
    return ResolvedTheme(
        colors = colors,
        scheme = derivation.scheme(colors),
        radii = radii(),
        controls = controls(),
        textScale = textScale.factor,
        background = CmpPageBackground(
            type = background,
            direction = gradientDirection,
            start = colors.backgroundPagePrimary,
            end = palette.background2.toColor(),
        ),
    )
}

/** Тема по умолчанию отдаёт CSS-радиусы: `fromBase(12)` + пилюля у чипов совпадает с spacing.css. */
private fun ThemeModeSpec.radii(): CmpRadii {
    val css = CmpRadii()
    val small = max(radii.cards - CARD_SMALL_STEP, 0).dp
    return css.copy(
        buttons = when (buttonShape) {
            ButtonShape.Square -> SQUARE_BUTTON.dp
            ButtonShape.Rounded -> radii.buttons.dp
            ButtonShape.Pill -> css.round
        },
        inputs = radii.inputs.dp,
        controls = radii.inputs.dp,
        chips = if (radii.chipsArePill) css.chips else radii.chips.dp,
        tags = min(radii.buttons, MAX_TAG).dp,
        cardSmall = small,
        cardMedium = radii.cards.dp,
        cardLarge = radii.cards.dp,
        listItem = small,
        snackbar = small,
        sheet = radii.sheets.dp,
        dialog = radii.sheets.dp,
    )
}

private fun ThemeModeSpec.controls() = CmpControlStyle(
    buttonFill = buttonFill,
    buttonHeight = buttonPadding.step(BUTTON_STEP, BUTTON_STEP),
    buttonPadding = buttonPadding.step(BUTTON_STEP, BUTTON_STEP),
    chipHeight = chipPadding.step(0.dp, CHIP_HEIGHT_STEP),
    chipPadding = chipPadding.step(CHIP_PADDING_STEP, CHIP_PADDING_STEP),
)

private fun ControlPadding.step(down: Dp, up: Dp): Dp = when (this) {
    ControlPadding.Small -> -down
    ControlPadding.Medium -> 0.dp
    ControlPadding.Large -> up
}

/** Тема × системный масштаб шрифта ≤ [MAX_TOTAL_TEXT_SCALE]; системный больше — тема не увеличивает, но уменьшает. */
internal fun effectiveTextScale(theme: Float, systemFontScale: Float): Float =
    min(theme, max(1f, MAX_TOTAL_TEXT_SCALE / systemFontScale))

internal val DefaultResolvedTheme: ResolvedTheme by lazy { ThemePreset.Default.light.resolve(isDark = false) }

internal val LocalCmpColors = staticCompositionLocalOf { LightCmpColors }
internal val LocalCmpControlStyle = staticCompositionLocalOf { CmpControlStyle() }
internal val LocalCmpTextScale = staticCompositionLocalOf { 1f }
internal val LocalCmpPageBackground = staticCompositionLocalOf { CmpPageBackground() }

private const val MAX_TOTAL_TEXT_SCALE = 1.5f
private const val SQUARE_BUTTON = 4
private const val MAX_TAG = 8
private const val CARD_SMALL_STEP = 4
private val BUTTON_STEP = 8.dp
private val CHIP_HEIGHT_STEP = 8.dp
private val CHIP_PADDING_STEP = 6.dp
