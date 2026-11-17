package ru.alarmtrade.specmp.core.ui.view.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBlur
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth

/** Источник блюра: контент, который размывают стекло и полосы у краёв. Типы haze наружу не отдаются. */
@Stable
class CmpBlurState internal constructor(internal val haze: HazeState)

@Composable
fun rememberCmpBlurState(): CmpBlurState = remember { CmpBlurState(HazeState()) }

/** Помечает контент как источник для [cmpGlassSurface] и [cmpEdgeBlur] того же [state]. */
fun Modifier.cmpBlurSource(state: CmpBlurState): Modifier = hazeSource(state.haze)

/** Край экрана, к которому прижата полоса блюра: полоса плотная у края и сходит на нет к другому. */
enum class CmpBlurEdge { Top, Bottom }

// Тинт полосы: плотный у края, держится до середины и растворяется — текст под полосой не спорит с топбаром
private const val EDGE_TINT_ALPHA = 0.82f
private const val EDGE_TINT_MID_ALPHA = 0.6f
private const val EDGE_TINT_MID = 0.55f

/**
 * Лёгкая полоса блюра у края (топбар, нижняя панель): прогрессивный блюр контента под полосой и тонкий тинт цвета
 * страницы. [alpha] (0…1) читается в стиле haze — скролл не вызывает рекомпозицию.
 */
@Composable
fun Modifier.cmpEdgeBlur(state: CmpBlurState, edge: CmpBlurEdge, alpha: () -> Float): Modifier {
    val page = CmpTheme.colors.backgroundPagePrimary
    val currentAlpha by rememberUpdatedState(alpha)
    val style = remember(page, edge) {
        val edgeTint = page.copy(alpha = EDGE_TINT_ALPHA)
        val midTint = page.copy(alpha = EDGE_TINT_MID_ALPHA)
        val clear = page.copy(alpha = 0f)
        val tint = when (edge) {
            CmpBlurEdge.Top -> Brush.verticalGradient(0f to edgeTint, EDGE_TINT_MID to midTint, 1f to clear)
            CmpBlurEdge.Bottom -> Brush.verticalGradient(0f to clear, 1f - EDGE_TINT_MID to midTint, 1f to edgeTint)
        }
        HazeBlurStyle {
            blurRadius(CmpBlur.glass)
            noiseFactor(0f)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
            progressive(
                when (edge) {
                    CmpBlurEdge.Top -> HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
                    CmpBlurEdge.Bottom -> HazeProgressive.verticalGradient(startIntensity = 0f, endIntensity = 1f)
                },
            )
        }.then { alpha(currentAlpha().coerceIn(0f, 1f)) }
    }
    return hazeBlur(input = HazeInput.Sources(state.haze), style = style)
}

/**
 * Стекло: блюр контента под элементом, вертикальный градиент 0 → 5% чернил и градиентная рамка 1dp (15% → 10%).
 * Без [state] — только градиент и рамка поверх полупрозрачной заливки `backgroundGlass`.
 */
@Composable
fun Modifier.cmpGlassSurface(state: CmpBlurState?, shape: Shape = CircleShape): Modifier {
    val colors = CmpTheme.colors
    val style = remember {
        HazeBlurStyle {
            blurRadius(CmpBlur.glass)
            noiseFactor(0f)
        }
    }
    val blurred = if (state != null) {
        Modifier.hazeBlur(input = HazeInput.Sources(state.haze), style = style)
    } else {
        Modifier.background(colors.backgroundGlass, shape)
    }
    return clip(shape)
        .then(blurred)
        .background(Brush.verticalGradient(listOf(Color.Transparent, colors.backgroundSurfaceUltraHighlight)))
        .border(
            width = CmpBorderWidth.icon,
            brush = Brush.verticalGradient(listOf(colors.borderMediumHighlight, colors.borderHighlight)),
            shape = shape,
        )
}
