package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_subtitle_bluetooth
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

@Composable
internal fun deviceSubtitle(profile: UpdateProfile, title: String): String = when (profile) {
    UpdateProfile.Main -> title
    UpdateProfile.Bluetooth -> stringResource(Strings.string.firmware_update_subtitle_bluetooth, title)
}
