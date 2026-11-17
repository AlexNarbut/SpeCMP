package ru.alarmtrade.specmp.feature.authstate.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_msg_error
import ru.alarmtrade.specmp.core.localization.resources.authstate_root_title_error
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone

@Composable
fun SessionCheckErrorScreen(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CmpBox(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CmpStateBox(
            icon = CmpIcons.AlertCircle,
            title = stringResource(Strings.string.authstate_root_title_error),
            text = stringResource(Strings.string.authstate_root_msg_error),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.authstate_root_btn_retry),
                onClick = onRetry,
                modifier = Modifier.padding(top = CmpSpacing.space8),
                icon = CmpIcons.Refresh,
            )
        }
    }
}
