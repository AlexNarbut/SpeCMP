package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.FirmwareSystemsEntry
import ru.alarmtrade.specmp.filepicker.rememberFirmwareFileExporter

@Composable
internal fun FirmwareArchiveHome(onBack: () -> Unit) {
    var archive by remember { mutableStateOf<FirmwareArchiveRequest?>(null) }
    val request = archive
    if (request == null) {
        FirmwareSystemsEntry(onPick = { archive = it }, onBack = onBack)
        return
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = { archive = null },
    )
    FirmwareArchiveEntry(
        request = request,
        onPick = {},
        onBack = { archive = null },
        vehiclePicker = { deviceName, onPick, onPickerBack -> ArchiveVehiclePicker(deviceName, onPick, onPickerBack) },
        exporter = rememberFirmwareFileExporter(),
        onChangeModel = { archive = null },
    )
}
