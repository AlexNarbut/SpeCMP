package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

/** Номер версии: акцентный цвет и насыщенное начертание, чтобы отличался от подписей. */
@Composable
internal fun VersionText(
    version: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    color: Color = Color.Unspecified
) {
    CmpText(
        text = version,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        color = if (color == Color.Unspecified) cmpThemeColors().primary else color
    )
}
