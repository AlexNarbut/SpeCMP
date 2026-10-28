package ru.alarmtrade.specmp.feature.authstate.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_msg_error
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_title_error
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@Composable
fun SessionCheckErrorScreen(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()

    CmpBox(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CmpColumn(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CmpIcon(
                icon = CmpIcons.AlertCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = colors.error
            )
            CmpText(
                text = stringResource(Strings.string.authstate_root_title_error),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )
            CmpText(
                text = stringResource(Strings.string.authstate_root_msg_error),
                fontSize = 14.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            CmpButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                CmpIcon(
                    icon = CmpIcons.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                CmpText(
                    text = stringResource(Strings.string.authstate_root_btn_retry),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
