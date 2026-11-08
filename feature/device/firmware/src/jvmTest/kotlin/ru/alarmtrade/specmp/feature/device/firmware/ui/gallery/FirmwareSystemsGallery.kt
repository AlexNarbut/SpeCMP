package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.FirmwareSystemsContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.FirmwareSystemsState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.SystemRowUi

/** Галерея выбора подсистемы (архив без блока): `build/state-gallery/firmware-systems`. */
class FirmwareSystemsGallery {
    private val outDir = File("build/state-gallery/firmware-systems")

    private val dx = FirmwareSystem(
        1,
        "Pandora DX-4GS",
        listOf(
            FirmwareSubsystem(10, "Основная программа", "DX4GS", FirmwareModuleKind.AlarmMain),
            FirmwareSubsystem(11, "Bluetooth-модуль", "BTM2", FirmwareModuleKind.BluetoothModule),
            FirmwareSubsystem(12, "GPS-модуль", "NAV-08", FirmwareModuleKind.GpsTracker),
        ),
    )
    private val tag =
        FirmwareSystem(2, "Pandora BT-760", listOf(FirmwareSubsystem(20, "Метка", "BT760", FirmwareModuleKind.Unknown)))
    private val watch = FirmwareSystem(
        3,
        "Pandora Watch",
        listOf(
            FirmwareSubsystem(30, "Часы", "PW2", FirmwareModuleKind.WatchMain),
            FirmwareSubsystem(31, "Bluetooth часов", "PW2BT", FirmwareModuleKind.WatchBluetooth),
        ),
    )

    private fun render(name: String, state: FirmwareSystemsState) = StateGallery.render(outDir, name) {
        FirmwareSystemsContent(state, onBack = {
        }, onQueryChange = {}, onToggle = {}, onPick = { _, _ -> }, onRetry = {})
    }

    @Test
    fun list() = render(
        "01_list",
        FirmwareSystemsState(
            LCEState.Content(
                listOf(
                    SystemRowUi(tag, tag.subsystems, expanded = false),
                    SystemRowUi(dx, dx.subsystems, expanded = true),
                    SystemRowUi(watch, watch.subsystems, expanded = false),
                ),
            ),
        ),
    )

    @Test
    fun search() = render(
        "02_search",
        FirmwareSystemsState(LCEState.Content(listOf(SystemRowUi(dx, dx.subsystems.take(1), expanded = true))), "dx4"),
    )

    @Test
    fun notFound() = render("03_not_found", FirmwareSystemsState(LCEState.Content(emptyList()), "IMMO-9"))

    @Test
    fun empty() = render("04_empty", FirmwareSystemsState(LCEState.Content(emptyList())))

    @Test
    fun loading() = render("05_loading", FirmwareSystemsState(LCEState.Loading))

    @Test
    fun error() = render("06_error", FirmwareSystemsState(LCEState.Error(FirmwareError.Common(ApiError.NoConnection))))
}
