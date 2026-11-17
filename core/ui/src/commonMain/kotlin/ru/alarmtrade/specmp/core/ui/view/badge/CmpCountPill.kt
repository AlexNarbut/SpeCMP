package ru.alarmtrade.specmp.core.ui.view.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
fun CmpDot(modifier: Modifier = Modifier, size: Dp = 10.dp, color: Color = Color.Unspecified) {
    val fill = if (color != Color.Unspecified) color else CmpTheme.colors.brandPrimaryDefault
    Box(modifier.size(size).background(fill, CircleShape))
}

@Composable
fun CmpCountPill(count: Int, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Row(
        modifier = modifier
            .height(CmpSize.tag)
            .background(colors.brandPrimaryHighlight, CircleShape)
            .padding(horizontal = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpDot(size = CmpSpacing.space8)
        CmpText(text = count.toString(), style = CmpTextStyle.Headline3, color = colors.brandPrimaryDefault)
    }
}
