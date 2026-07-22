package ru.alarmtrade.specmp.feature.device.selection.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import kotlin.test.Test
import kotlin.test.assertTrue

class SpecialistDeviceScanRepositoryTest {

    /**
     * Регрессия десктопа: транспорт, недоступный на платформе (SDK бросает при обращении
     * к интерактору), не должен ронять создание репозитория/графа Koin — сканирование по
     * нему возвращает пустой список.
     */
    @Test
    fun `unavailable transports do not crash construction and yield empty results`() = runTest {
        val repository = SpecialistDeviceScanRepository(
            bluetoothConnectorInteractor = { error("BLE not supported on this platform") },
            usbConnectorInteractor = { error("USB not supported on this platform") },
        )

        val ble = repository.scanBluetooth().first()
        val usb = repository.scanUsb().first()

        assertTrue(ble is Response.Success && ble.value.isEmpty())
        assertTrue(usb is Response.Success && usb.value.isEmpty())
    }

    /** Провайдеры-заглушки типов SDK не нужны — конструктор их не вызывает при создании. */
    @Suppress("unused")
    private fun compileGuard(
        bt: () -> BluetoothConnectorInteractor,
        usb: () -> UsbConnectorInteractor,
    ) = SpecialistDeviceScanRepository(bt, usb)
}
