package ru.alarmtrade.specmp.feature.device.selection.data.repository

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.SerialConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import ru.alarmtrade.specmp.core.common.Response

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
            serialConnectorInteractor = { error("COM not supported on this platform") },
        )

        val ble = repository.scanBluetooth().first()
        val usb = repository.scanUsb().first()
        val serial = repository.scanSerial().first()

        assertTrue(ble is Response.Success && ble.value.isEmpty())
        assertTrue(usb is Response.Success && usb.value.isEmpty())
        assertTrue(serial is Response.Success && serial.value.isEmpty())
    }

    /** Провайдеры-заглушки типов SDK не нужны — конструктор их не вызывает при создании. */
    @Suppress("unused")
    private fun compileGuard(
        bt: () -> BluetoothConnectorInteractor,
        usb: () -> UsbConnectorInteractor,
        serial: () -> SerialConnectorInteractor,
    ) = SpecialistDeviceScanRepository(bt, usb, serial)
}
