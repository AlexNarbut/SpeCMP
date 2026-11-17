package ru.alarmtrade.specmp.core.ui.view.effect

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpPageBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground

/**
 * Живой фон экрана: цвет страницы, синее свечение бренда сверху справа и тёплое снизу слева. Пресеты повторяют
 * фоны входа и главного; цвета — из темы, поэтому обе темы получают свой вариант.
 */
enum class CmpBackdrop(internal val brand: Glow, internal val warm: Glow) {
    /** Вход и регистрация — свечения чуть плотнее. */
    Auth(
        brand = Glow(alpha = 0.88f, x = 1.02f, y = -0.15f, radius = 0.72f),
        warm = Glow(alpha = 0.78f, x = -0.2f, y = 1.12f, radius = 0.68f),
    ),

    /** Главный и остальные экраны. */
    Home(
        brand = Glow(alpha = 0.82f, x = 1.04f, y = -0.18f, radius = 0.76f),
        warm = Glow(alpha = 0.72f, x = -0.18f, y = 1.1f, radius = 0.7f),
    ),
    ;

    /** Центр — доля ширины/высоты, радиус — доля большей стороны. */
    internal class Glow(val alpha: Float, val x: Float, val y: Float, val radius: Float)
}

@Composable
fun Modifier.cmpBackdrop(backdrop: CmpBackdrop = CmpBackdrop.Home): Modifier {
    val colors = CmpTheme.colors
    val page = colors.backgroundPagePrimary
    val brand = colors.brandPrimaryHighlight.copy(alpha = backdrop.brand.alpha)
    val warm = colors.warningHighlight.copy(alpha = backdrop.warm.alpha)
    return drawBehind {
        drawRect(page)
        listOf(brand to backdrop.brand, warm to backdrop.warm).forEach { (color, glow) ->
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(size.width * glow.x, size.height * glow.y),
                    radius = size.maxDimension * glow.radius,
                ),
            )
        }
    }
}

/** Фон страницы по текущей теме: свечение ([cmpBackdrop]), заливка или градиент — для своих рамок (предпросмотр). */
@Composable
fun Modifier.cmpPageBackground(): Modifier {
    val background = LocalCmpPageBackground.current
    val brush = background.brush
    return when {
        brush != null -> background(brush)
        background.type == ThemeBackground.Glow -> cmpBackdrop(CmpBackdrop.Home)
        else -> background(CmpTheme.colors.backgroundPagePrimary)
    }
}
