package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState

class FirmwareUpdateMachineTest {

    private val device = AlarmDevice(1, "DX-9X", "VER3.57", DeviceOperationMode.MAIN, emptySet(), can = null)
    private val boot = DeviceSessionState.FirmwareBoot(device.copy(mode = DeviceOperationMode.BOOT))

    private fun profile(overBle: Boolean) = Profile(
        kind = UpdateProfile.Main,
        target = BootModeTarget.MainProgram,
        deviceName = "DX-9X",
        deviceTitle = "DX-9X  1",
        currentVersion = "3.57",
        modules = emptyList(),
        canExit = overBle,
        overBle = overBle
    )

    @Test
    fun `reboot into the loader shows entering until the unit is in boot`() {
        assertEquals(UpdatePhase.Entering, phaseOf(Core(), DeviceSessionState.Ready(device), DeviceReboot.ToBoot))
        assertEquals(UpdatePhase.Loading, phaseOf(Core(), boot, DeviceReboot.ToBoot))
    }

    @Test
    fun `unit back in main mode after apply means success`() {
        assertEquals(
            UpdatePhase.Success,
            phaseOf(Core(stage = UpdatePhase.Apply), DeviceSessionState.Ready(device), null)
        )
    }

    @Test
    fun `lost link during a running BLE transfer is a reconnect, not a loss`() {
        val transfer =
            Core(stage = UpdatePhase.Transfer(percent = 40), profile = profile(overBle = true), running = true)

        assertEquals(
            UpdatePhase.Transfer(percent = 40, reconnecting = true),
            phaseOf(transfer, DeviceSessionState.Disconnected, null)
        )
    }

    @Test
    fun `lost link while preparing over USB is a loss during the update`() {
        val prepare = Core(stage = UpdatePhase.Prepare, profile = profile(overBle = false), running = true)

        assertEquals(UpdatePhase.Lost(updating = true), phaseOf(prepare, DeviceSessionState.Connecting, null))
        assertEquals(
            UpdatePhase.Lost(updating = false),
            phaseOf(Core(stage = UpdatePhase.Source), DeviceSessionState.Disconnected, null)
        )
    }
}
