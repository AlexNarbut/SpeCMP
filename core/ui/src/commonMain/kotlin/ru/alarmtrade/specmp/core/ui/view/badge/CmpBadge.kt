@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
fun CmpBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    Badge(
        modifier = modifier,
        containerColor = if (containerColor != Color.Unspecified) {
            containerColor
        } else {
            CmpTheme.colors.brandPrimaryDefault
        },
        contentColor = if (contentColor != Color.Unspecified) {
            contentColor
        } else {
            CmpTheme.colors.onBrand
        },
        content = content,
    )
}

@Composable
fun CmpBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BadgedBox(
        badge = badge,
        modifier = modifier,
        content = content,
    )
}

private val CountBadgeMin = 24.dp
private val CountBadgeRing = 2.dp

/** Угловой счётчик (kit): синий круг от 24dp с числом Additional0 и кольцом цвета страницы. */
@Composable
fun CmpCountBadge(count: Int, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = CountBadgeMin, minHeight = CountBadgeMin)
            .border(CountBadgeRing, colors.backgroundPagePrimary, CircleShape)
            .padding(CountBadgeRing)
            .background(colors.brandPrimaryDefault, CircleShape)
            .padding(horizontal = CmpSpacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(text = count.toString(), style = CmpTextStyle.Additional0, color = colors.onBrand)
    }
}

/** Плитка-подложка под иконку: квадрат с радиусом `CmpRadius.cardSmall`, фон `*-highlight` тона, иконка цветом тона. */
@Composable
fun CmpIconTile(
    icon: Painter,
    modifier: Modifier = Modifier,
    tone: CmpIconTileTone = CmpIconTileTone.Brand,
    size: Dp = CmpSize.touchMin,
) {
    val colors = CmpTheme.colors
    val (fill, tint) = when (tone) {
        CmpIconTileTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkPrimaryDefault
        CmpIconTileTone.Brand -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
        CmpIconTileTone.Warning -> colors.warningHighlight to colors.warningDefault
        CmpIconTileTone.Error -> colors.errorHighlight to colors.errorDefault
    }
    Box(
        modifier = modifier.size(size).background(fill, RoundedCornerShape(CmpRadius.cardSmall)),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(painter = icon, contentDescription = null, tint = tint, modifier = Modifier.size(CmpSize.iconM))
    }
}

enum class CmpIconTileTone { Neutral, Brand, Warning, Error }
