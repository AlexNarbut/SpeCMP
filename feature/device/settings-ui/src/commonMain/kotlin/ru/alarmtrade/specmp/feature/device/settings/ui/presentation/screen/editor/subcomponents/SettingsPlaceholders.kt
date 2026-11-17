package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_error_read_text
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_error_read_title
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_hint_read
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_loading_read
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.text

private val PlaceholderMaxWidth = 420.dp

@Composable
internal fun MenuLoading() {
    Centered {
        CmpColumn(
            modifier = Modifier.widthIn(max = PlaceholderMaxWidth).padding(CmpSpacing.screenPaddingH),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpCircularProgressIndicator()
            CmpText(
                text = stringResource(Strings.string.settings_editor_loading_read),
                style = CmpTextStyle.Headline2,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.settings_editor_hint_read),
                style = CmpTextStyle.Paragraph3,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun MenuError(error: SettingsError, onRetry: () -> Unit) {
    Centered {
        CmpStateBox(
            icon = CmpIcons.AlertCircle,
            title = error.text(Strings.string.settings_editor_error_read_title),
            text = stringResource(Strings.string.settings_editor_error_read_text),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.settings_common_btn_retry),
                onClick = onRetry,
                fullWidth = true,
                modifier = Modifier.padding(top = CmpSpacing.space12),
            )
        }
    }
}

@Composable
internal fun EmptyMessage(icon: CmpIcons, title: String, hint: String?) {
    Centered {
        CmpStateBox(icon = icon, title = title, text = hint)
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}
