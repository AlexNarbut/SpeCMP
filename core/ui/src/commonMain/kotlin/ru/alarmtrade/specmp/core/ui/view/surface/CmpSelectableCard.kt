package ru.alarmtrade.specmp.core.ui.view.surface

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius

@Composable
fun CmpSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            CmpTheme.colors.brandPrimaryDefault
        } else {
            CmpTheme.colors.borderLight
        },
        animationSpec = tween(300),
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            CmpTheme.colors.brandPrimaryHighlight
        } else {
            CmpTheme.colors.backgroundSurfaceCard
        },
        animationSpec = tween(300),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CmpRadius.cardMedium),
        color = containerColor,
        border = BorderStroke(CmpBorderWidth.listItem, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
            }
            content()
        }
    }
}
