package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.text

private val PlaceholderMaxWidth = 420.dp

@Composable
internal fun MenuLoading() {
    Centered {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Strings.string.settings_editor_loading_read),
            style = CmpTextStyle.Body,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = stringResource(Strings.string.settings_editor_hint_read),
            style = CmpTextStyle.Caption,
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun MenuError(error: SettingsError, onRetry: () -> Unit) {
    Centered {
        CmpIcon(icon = CmpIcons.AlertCircle, contentDescription = null, tint = cmpThemeColors().error)
        CmpText(
            text = error.text(Strings.string.settings_editor_error_read_title),
            style = CmpTextStyle.Title,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = stringResource(Strings.string.settings_editor_error_read_text),
            style = CmpTextStyle.Body,
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CmpButton(
            text = stringResource(Strings.string.settings_common_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        )
    }
}

@Composable
internal fun EmptyMessage(icon: CmpIcons, title: String, hint: String?) {
    Centered {
        CmpIcon(icon = icon, contentDescription = null, tint = cmpThemeColors().onSurfaceVariant)
        CmpText(text = title, style = CmpTextStyle.Title, textAlign = TextAlign.Center)
        if (hint != null) {
            CmpText(
                text = hint,
                style = CmpTextStyle.Body,
                color = cmpThemeColors().onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    CmpBox(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        CmpColumn(
            modifier = Modifier.widthIn(max = PlaceholderMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}
