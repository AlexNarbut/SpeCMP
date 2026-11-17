@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

data class CmpBreadcrumb(val label: String, val marked: Boolean = false)

private const val COLLAPSED_MARK = "…"

/** Последний элемент — текущий (не нажимается); больше [maxVisible] — начало сворачивается в «…». */
@Composable
fun CmpBreadcrumbs(
    items: List<CmpBreadcrumb>,
    onItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
) {
    val collapsed = items.size > maxVisible
    val firstShown = if (collapsed) items.size - (maxVisible - 1) else 0
    val scroll = rememberScrollState()
    LaunchedEffect(items.size) { scroll.scrollTo(scroll.maxValue) }

    Row(
        modifier = modifier.horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (collapsed) {
            Crumb(text = COLLAPSED_MARK, current = false, marked = false, onClick = null)
        }
        for (index in firstShown until items.size) {
            if (index > 0 || collapsed) Chevron()
            val item = items[index]
            val current = index == items.lastIndex
            Crumb(
                text = item.label,
                current = current,
                marked = item.marked,
                onClick = if (current) null else ({ onItemClick(index) }),
            )
        }
    }
}

@Composable
private fun Chevron() {
    CmpIcon(
        icon = CmpIcons.ChevronRight,
        contentDescription = null,
        tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        modifier = Modifier.size(CmpSize.iconM),
    )
}

@Composable
private fun Crumb(text: String, current: Boolean, marked: Boolean, onClick: (() -> Unit)?) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.tags)
    Row(
        modifier = Modifier
            .heightIn(min = CmpSize.touchMin)
            .widthIn(min = CmpSize.touchMin)
            .then(if (onClick != null) Modifier.cmpPressable(onClick = onClick, shape = shape) else Modifier)
            .padding(horizontal = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = text,
            style = if (current) CmpTextStyle.Headline3 else CmpTextStyle.Paragraph2,
            color = if (current ||
                onClick == null
            ) {
                colors.foregroundOnDarkPrimaryDefault
            } else {
                colors.brandPrimaryDefault
            },
            maxLines = 1,
        )
        if (marked) CmpDot(size = CmpSpacing.space8)
    }
}
