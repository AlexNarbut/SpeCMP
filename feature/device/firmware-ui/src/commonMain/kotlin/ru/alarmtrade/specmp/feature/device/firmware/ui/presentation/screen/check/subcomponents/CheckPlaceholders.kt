package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_error_msg
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_error_title
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_loading_modules
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_btn_retry

private val CenterMaxWidth = 440.dp

@Composable
internal fun ModulesLoading() {
    CenteredBody {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Res.string.firmware_check_loading_modules),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ModulesError(onRetry: () -> Unit) {
    CenteredBody {
        FirmwareHero(icon = CmpIcons.ErrorOutline, tone = HeroTone.Neutral, size = 88.dp)
        CmpText(
            text = stringResource(Res.string.firmware_check_error_title),
            textAlign = TextAlign.Center,
            style = CmpTextStyle.Title
        )
        CmpText(
            text = stringResource(Res.string.firmware_check_error_msg),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        CmpButton(
            text = stringResource(Res.string.firmware_common_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}

@Composable
private fun CenteredBody(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(
                max = CenterMaxWidth
            ).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}
