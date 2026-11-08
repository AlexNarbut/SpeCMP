package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_error_msg
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_error_title
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_loading_modules
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareCenteredBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLoadError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text

@Composable
internal fun ModulesLoading() {
    FirmwareCenteredBody {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Strings.string.firmware_check_loading_modules),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ModulesError(error: FirmwareError, onRetry: () -> Unit) {
    FirmwareLoadError(
        icon = CmpIcons.ErrorOutline,
        title = error.text(Strings.string.firmware_check_error_title),
        message = stringResource(Strings.string.firmware_check_error_msg),
        onRetry = onRetry
    )
}
