package ru.alarmtrade.specmp.feature.device.selection.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.selection.data.repository.SpecialistDeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository

/**
 * Koin-модуль логической фичи `feature/device/selection`.
 * Отдаёт репозиторий-фасад скана; SDK-интеракторы берёт из `core/specialist`.
 */
val deviceSelectionModule = module {
    single<DeviceScanRepository> {
        // Провайдеры: интеракторы резолвятся лениво — недоступный транспорт
        // (BLE на desktop, USB на iOS) не должен ронять создание репозитория.
        SpecialistDeviceScanRepository(
            bluetoothConnectorInteractor = { get() },
            usbConnectorInteractor = { get() }
        )
    }
}
