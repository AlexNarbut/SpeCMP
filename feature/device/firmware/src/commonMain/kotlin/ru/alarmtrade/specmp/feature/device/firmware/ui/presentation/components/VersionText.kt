package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun VersionText(
    version: String,
    modifier: Modifier = Modifier,
    style: CmpTextStyle = CmpTextStyle.Headline3,
    color: Color = Color.Unspecified,
) {
    CmpText(
        text = version,
        modifier = modifier,
        style = style,
        color = if (color == Color.Unspecified) cmpThemeColors().primary else color,
    )
}
