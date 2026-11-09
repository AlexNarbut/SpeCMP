package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.rememberFirmwareFilePicker

@Composable
internal fun rememberAppFirmwareFilePicker(): FirmwareFilePicker = rememberFirmwareFilePicker(
    appScope = koinInject(qualifier = appScopeQualifier),
    ioDispatcher = koinInject<AppDispatchers>().io,
)
