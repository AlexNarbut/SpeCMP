package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile
import specmp.feature.device.firmware.generated.resources.Res
import specmp.feature.device.firmware.generated.resources.firmware_update_subtitle_bluetooth

/** Подзаголовок экранов режима загрузчика: «Название  ID» блока или «Bluetooth-модуль · имя» (по [profile]). */
@Composable
internal fun deviceSubtitle(profile: UpdateProfile, title: String): String = when (profile) {
    UpdateProfile.Main -> title
    UpdateProfile.Bluetooth -> stringResource(Res.string.firmware_update_subtitle_bluetooth, title)
}
