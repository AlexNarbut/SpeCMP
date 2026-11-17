package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val CenterMaxWidth = 440.dp

@Composable
internal fun FirmwareCenteredBody(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(
                max = CenterMaxWidth,
            ).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}

@Composable
internal fun FirmwareLoadError(icon: CmpIcons, title: String, message: String, onRetry: () -> Unit) {
    FirmwareCenteredBody {
        FirmwareHero(icon = icon, tone = HeroTone.Neutral, size = 88.dp)
        CmpText(text = title, style = CmpTextStyle.Headline1, textAlign = TextAlign.Center)
        CmpText(
            text = message,
            style = CmpTextStyle.Paragraph2,
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CmpButton(
            text = stringResource(Strings.string.firmware_common_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}
