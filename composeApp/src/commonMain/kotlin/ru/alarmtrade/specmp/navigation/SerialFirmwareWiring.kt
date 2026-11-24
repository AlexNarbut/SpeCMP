package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.filepicker.rememberAppFirmwareFilePicker

@Composable
internal fun SerialFirmwareHome(onExit: () -> Unit) {
    val filePicker = rememberAppFirmwareFilePicker()
    DeviceInteractionHost(
        onExit = onExit,
        bootContent = { exit -> SerialFirmwareFlow(exit, filePicker) },
    ) { _, exit ->
        SerialFirmwareFlow(exit, filePicker)
    }
}

@Composable
private fun SerialFirmwareFlow(onExit: () -> Unit, filePicker: FirmwareFilePicker) {
    SerialFirmwareScreen(
        onExit = onExit,
        filePicker = filePicker,
        sources = FirmwareSources.All,
        vehiclePicker = { deviceName, onPick, onPickerBack -> ArchiveVehiclePicker(deviceName, onPick, onPickerBack) },
    )
}
