package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun SupportChatDateDivider(label: String, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpText(
        text = label,
        style = CmpTextStyle.Additional0,
        color = colors.foregroundOnDarkSecondaryDefault,
        modifier = modifier
            .padding(vertical = CmpSpacing.space4)
            .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.round))
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
    )
}
