// Сгенерировано scripts/ds-tokens/generate.py из design/design-system/tokens/spacing.css — не править вручную.

package ru.alarmtrade.specmp.core.ui.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Отступы (сетка 4). `screenPaddingH` — компактное окно, `screenPaddingHMedium` — от 600. */
object CmpSpacing {
    val space4: Dp = 4.dp
    val space8: Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp
    val space32: Dp = 32.dp
    val space56: Dp = 56.dp
    val screenPaddingH: Dp = 24.dp
    val screenPaddingTop: Dp = 16.dp
    val screenPaddingBottom: Dp = 24.dp
    val screenPaddingHMedium: Dp = 32.dp
}

/**
 * Радиусы по типу компонента (`--r-<тип>-radius`); 9999 — пилюля.
 * Значения — из темы (`LocalCmpRadii`), CSS — по умолчанию.
 */
object CmpRadius {
    val buttons: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.buttons
    val inputs: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.inputs
    val controls: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.controls
    val chips: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.chips
    val search: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.search
    val tags: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.tags
    val checkbox: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.checkbox
    val icon: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.icon
    val round: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.round
    val cardSmall: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.cardSmall
    val cardMedium: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.cardMedium
    val cardLarge: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.cardLarge
    val listItem: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.listItem
    val snackbar: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.snackbar
    val sheet: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.sheet
    val dialog: Dp
        @Composable @ReadOnlyComposable
        get() = LocalCmpRadii.current.dialog
}

/** Значения `CmpRadius` в теме; по умолчанию — CSS-токены. */
@Immutable
data class CmpRadii(
    val buttons: Dp = 12.dp,
    val inputs: Dp = 12.dp,
    val controls: Dp = 12.dp,
    val chips: Dp = 9999.dp,
    val search: Dp = 9999.dp,
    val tags: Dp = 8.dp,
    val checkbox: Dp = 6.dp,
    val icon: Dp = 9999.dp,
    val round: Dp = 9999.dp,
    val cardSmall: Dp = 12.dp,
    val cardMedium: Dp = 16.dp,
    val cardLarge: Dp = 16.dp,
    val listItem: Dp = 12.dp,
    val snackbar: Dp = 12.dp,
    val sheet: Dp = 16.dp,
    val dialog: Dp = 16.dp,
)

internal val LocalCmpRadii = staticCompositionLocalOf { CmpRadii() }

/** Толщины рамок (`--w-<тип>-width`); 0 — рамку не рисовать. */
object CmpBorderWidth {
    val inputs: Dp = 1.dp
    val buttons: Dp = 0.dp
    val chips: Dp = 1.dp
    val search: Dp = 1.dp
    val icon: Dp = 1.dp
    val tags: Dp = 1.dp
    val listItem: Dp = 1.dp
    val focus: Dp = 3.dp
}

/** Иконки и высоты контролов (зона касания ≥ `touchMin`). */
object CmpSize {
    val iconS: Dp = 16.dp
    val iconM: Dp = 24.dp
    val iconL: Dp = 32.dp
    val iconXl: Dp = 44.dp
    val iconXxl: Dp = 56.dp
    val buttonS: Dp = 48.dp
    val buttonM: Dp = 52.dp
    val buttonL: Dp = 52.dp
    val buttonXl: Dp = 56.dp
    val chip: Dp = 48.dp
    val tab: Dp = 48.dp
    val hold: Dp = 64.dp
    val topbar: Dp = 64.dp
    val input: Dp = 56.dp
    val codeCell: Dp = 72.dp
    val tag: Dp = 32.dp
    val listItem: Dp = 80.dp
    val infoRow: Dp = 88.dp
    val rowSection: Dp = 96.dp
    val touchMin: Dp = 48.dp
}

/** Радиусы блюра поверхностей. */
object CmpBlur {
    val glass: Dp = 24.dp
    val sheet: Dp = 32.dp
}

/** Движение: нажатие и стандартные переходы. */
object CmpMotion {
    const val PRESS_SCALE: Float = 0.96f
    const val STANDARD_MS: Int = 300
    val standardEasing: CubicBezierEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    const val FAST_MS: Int = 150
    val fastEasing: CubicBezierEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    const val PRESS_MS: Int = 220
    val pressEasing: CubicBezierEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)
}
