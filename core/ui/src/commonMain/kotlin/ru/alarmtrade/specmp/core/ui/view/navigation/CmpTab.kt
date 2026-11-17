@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

data class CmpTabItem(val title: String, val icon: (@Composable () -> Unit)? = null)

private const val TAB_COLOR_MS = 600

/**
 * Табы-сегменты (kit SegmentedTabs): трек — ступень чернил 5% + рамка, выбранный сегмент залит чернилами, высота 48,
 * подпись Headline3 капсом. [scrollable] — сегменты по ширине текста с прокруткой; [inset] — отступ трека по бокам.
 */
@Composable
fun CmpTabRow(
    selectedTabIndex: Int,
    tabs: List<CmpTabItem>,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    inset: Dp = CmpSpacing.space16,
) {
    val colors = CmpTheme.colors
    val trackShape = RoundedCornerShape(CmpRadius.controls + CmpSpacing.space4)
    Row(
        modifier = modifier
            .padding(horizontal = inset, vertical = CmpSpacing.space8)
            .background(colors.backgroundSurfaceUltraHighlight, trackShape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, trackShape)
            .padding(CmpSpacing.space4)
            .then(if (scrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        tabs.forEachIndexed { index, tab ->
            TabSegment(
                tab = tab,
                selected = index == selectedTabIndex,
                onClick = { onTabSelect(index) },
                fill = !scrollable,
            )
        }
    }
}

@Composable
private fun RowScope.TabSegment(tab: CmpTabItem, selected: Boolean, onClick: () -> Unit, fill: Boolean) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.controls)
    val container by animateColorAsState(
        targetValue = if (selected) {
            colors.backgroundSurfacePrimary
        } else {
            colors.backgroundSurfacePrimary.copy(
                alpha = 0f,
            )
        },
        animationSpec = tween(TAB_COLOR_MS),
        label = "tab-bg",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.foregroundOnLightPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
        animationSpec = tween(TAB_COLOR_MS),
        label = "tab-fg",
    )
    Row(
        modifier = Modifier
            .then(if (fill) Modifier.weight(1f) else Modifier)
            .height(CmpSize.tab)
            .semantics { this.selected = selected }
            .cmpPressable(onClick = onClick, shape = shape, role = Role.Tab)
            .background(container, shape)
            .padding(horizontal = CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            tab.icon?.invoke()
            CmpText(
                text = tab.title,
                style = CmpTextStyle.Headline3,
                uppercase = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
