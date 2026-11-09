package ru.alarmtrade.specmp.flasher.di

import com.russhwolf.settings.Settings
import kotlin.test.Test
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveViewModel
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanDeviceViewModel

class FlasherKoinModulesTest {
    @Test
    fun `every constructor dependency of the flasher graph is defined`() {
        module { includes(flasherKoinModules(appVersionName = "1.0", appVersionCode = 1)) }.verify(
            // Settings, Set транспортов, Pair сервисов — значения, собранные в лямбдах модулей.
            extraTypes = listOf(Settings::class, Set::class, Pair::class),
            // Параметры ViewModel из parametersOf экрана.
            injections = listOf(
                definition<ScanDeviceViewModel>(Boolean::class),
                definition<FirmwareArchiveViewModel>(FirmwareArchiveRequest::class),
            ),
        )
    }
}
