package ru.alarmtrade.specmp.core.ui.view.divider

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CmpDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Color.Unspecified,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = if (color != Color.Unspecified) color
        else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
fun CmpVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Color.Unspecified,
) {
    VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = if (color != Color.Unspecified) color
        else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
    )
}
