package ru.alarmtrade.specmp.feature.device.selection.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.selection.data.repository.SpecialistDeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanDeviceViewModel

/**
 * Koin-модуль фичи `feature/device/selection`: репозиторий-фасад скана и ViewModel экрана поиска.
 * SDK-интеракторы берёт из `core/specialist`, сессию устройства — из `device:interaction`.
 */
val deviceSelectionModule = module {
    single<DeviceScanRepository> {
        // Провайдеры: интеракторы резолвятся лениво — недоступный транспорт
        // (BLE на desktop, USB на iOS) не должен ронять создание репозитория.
        SpecialistDeviceScanRepository(
            bluetoothConnectorInteractor = { get() },
            usbConnectorInteractor = { get() },
            serialConnectorInteractor = { get() },
        )
    }
    // serialEnabled — поиск COM-портов (только приложение прошивки), alarmsOnly — только сигнализации (быстрый
    // монтаж); параметры графа поиска.
    viewModel { (serialEnabled: Boolean, alarmsOnly: Boolean) ->
        ScanDeviceViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            serialEnabled = serialEnabled,
            alarmsOnly = alarmsOnly,
        )
    }
}
