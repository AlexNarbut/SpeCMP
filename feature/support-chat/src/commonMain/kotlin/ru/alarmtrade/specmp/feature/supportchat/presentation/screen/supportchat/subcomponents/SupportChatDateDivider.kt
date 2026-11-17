package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun SupportChatDateDivider(label: String, colors: CmpThemeColors, modifier: Modifier = Modifier) {
    CmpSurface(
        modifier = modifier.padding(vertical = 6.dp),
        shape = RoundedCornerShape(percent = 50),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurfaceVariant,
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph2,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}
