package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object MainHomeTokens {
    val Success = Color(0xFF79BD5B)
    val CardRadius = 12.dp
    val PrimaryCardRadius = 16.dp
}

internal fun mainHomePrimaryCardHeight(
    mode: MainHomeMode,
    itemWidth: Dp,
): Dp {
    return when (mode) {
        MainHomeMode.Compact -> (itemWidth * 0.68f).coerceInDp(224.dp, 260.dp)
        MainHomeMode.Medium -> (itemWidth * 0.82f).coerceInDp(220.dp, 252.dp)
        MainHomeMode.Expanded -> (itemWidth * 0.44f).coerceInDp(212.dp, 244.dp)
        MainHomeMode.Large -> (itemWidth * 0.40f).coerceInDp(204.dp, 236.dp)
    }
}

internal fun mainHomeToolCardHeight(
    mode: MainHomeMode,
    itemWidth: Dp,
): Dp {
    return when (mode) {
        MainHomeMode.Compact -> (itemWidth * 0.36f).coerceInDp(112.dp, 136.dp)
        MainHomeMode.Medium -> (itemWidth * 0.74f).coerceInDp(184.dp, 220.dp)
        MainHomeMode.Expanded -> (itemWidth * 0.60f).coerceInDp(184.dp, 216.dp)
        MainHomeMode.Large -> (itemWidth * 0.78f).coerceInDp(184.dp, 212.dp)
    }
}

private fun Dp.coerceInDp(
    minimumValue: Dp,
    maximumValue: Dp,
): Dp {
    return when {
        this < minimumValue -> minimumValue
        this > maximumValue -> maximumValue
        else -> this
    }
}
