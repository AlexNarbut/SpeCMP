package ru.alarmtrade.specmp.feature.device.selection.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.bluetoothControl.domain.bluetooth.scan.ScannedDeviceState
import ru.alarmtrade.common.core.PlatformType
import ru.alarmtrade.common.core.platformType
import ru.alarmtrade.specialist.domain.bluetooth.scan.BleDeviceScanResult
import ru.alarmtrade.specialist.domain.serial.ConnectedSerialPortInformation
import ru.alarmtrade.specialist.domain.usb.AttachedUsbDeviceInformation
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.SerialConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.DeviceConnectionType
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.core.specialist.supportedConnectionTypes
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository

/** Транспорты резолвятся лениво: недоступный на платформе (BLE на desktop, USB и COM на iOS) даёт пустой список. */
internal class SpecialistDeviceScanRepository(
    private val bluetoothConnectorInteractor: () -> BluetoothConnectorInteractor,
    private val usbConnectorInteractor: () -> UsbConnectorInteractor,
    private val serialConnectorInteractor: () -> SerialConnectorInteractor,
) : DeviceScanRepository {

    private val connectionTypes: Set<DeviceConnectionType> = supportedConnectionTypes()

    override fun scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>> = flow {
        if (DeviceConnectionType.BLUETOOTH !in connectionTypes) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        val interactor = bluetoothConnectorInteractor.resolveOrNull()
        if (interactor == null) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        // Сканер SDK один на приложение с единственным ScanCallback: если поиск уже идёт
        // (например, внутренний поиск connect), новый startScan Android молча игнорирует.
        // Поэтому сначала сбрасываем текущий поиск.
        interactor.stopBluetoothScanUseCase.execute()
        emitAll(
            safeFlowRun {
                interactor.scanBluetoothDevicesUseCase.execute()
                    .map { response -> response.toResponse().map { devices -> devices.map { it.toDomain() } } }
            },
        )
    }

    override fun scanUsb(): Flow<Response<List<ScannedDevice.Usb>>> = flow {
        if (DeviceConnectionType.USB !in connectionTypes) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        val interactor = usbConnectorInteractor.resolveOrNull()
        if (interactor == null) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        emitAll(
            safeFlowRun {
                interactor.scanUsbDevicesUseCase.execute()
                    .map { response -> response.toResponse().map { devices -> devices.map { it.toDomain() } } }
            },
        )
    }

    override fun scanSerial(): Flow<Response<List<ScannedDevice.Serial>>> = flow {
        if (DeviceConnectionType.SERIAL !in connectionTypes) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        val interactor = serialConnectorInteractor.resolveOrNull()
        if (interactor == null) {
            emit(Response.Success(emptyList()))
            return@flow
        }
        emitAll(
            safeFlowRun {
                interactor.scanSerialPortDevicesUseCase.execute()
                    .map { response -> response.toResponse().map { ports -> ports.map { it.toDomain() } } }
            },
        )
    }

    private fun BleDeviceScanResult.toDomain(): ScannedDevice.Ble = ScannedDevice.Ble(
        id = uuid,
        name = name,
        kind = deviceType.name.toDeviceKind(),
        mode = state.toMode(),
        rssi = rssi,
        hasDeviceBond = hasDeviceBond,
        // На iOS SDK не знает о сопряжении с телефоном (isBonded всегда false).
        hasPhoneBond = hasPhoneBond.takeIf { platformType() == PlatformType.ANDROID },
    )

    private fun AttachedUsbDeviceInformation.toDomain(): ScannedDevice.Usb = ScannedDevice.Usb(
        id = id,
        name = productName,
        manufacturerName = manufacturerName,
        vendorId = vendorId,
        productId = productId,
        // После опроса блока SDK кладёт в id его systemId (до опроса — путь устройства).
        systemId = id.toLongOrNull(),
        firmwareVersion = firmwareVersion?.takeIf { it.isNotBlank() },
        isBootMode = isBootMode,
    )

    private fun ConnectedSerialPortInformation.toDomain(): ScannedDevice.Serial = ScannedDevice.Serial(
        id = id,
        name = name.ifBlank { id },
        vendorId = vid,
        productId = pid,
    )

    // По имени, а не по enum SDK: набор типов растёт, неизвестные уходят в OTHER.
    @Suppress("CyclomaticComplexMethod") // таблица-маппер
    private fun String.toDeviceKind(): DeviceKind = when {
        this == "ALARM" -> DeviceKind.ALARM

        this == "MICROBOT" -> DeviceKind.MICROBOT

        startsWith("TAG_") || this == "BAND" || this == "WATCH" -> DeviceKind.TAG

        startsWith("RELAY_") -> DeviceKind.RELAY

        this == "IMMOBILIZER" -> DeviceKind.IMMOBILIZER

        startsWith("REMOTE_CONTROL") -> DeviceKind.REMOTE_CONTROL

        startsWith("NAV_") || startsWith("GPS_NAV") -> DeviceKind.NAVIGATION

        endsWith("_SENSOR") || this == "TPMS" -> DeviceKind.SENSOR

        startsWith("DOOR_MODULE") || startsWith("RHM") || this == "DI_04" || this == "RF_MODULE" ||
            this == "VS_MODULE" || this == "HOOD_LOCK" || this == "SIREN" -> DeviceKind.MODULE

        else -> DeviceKind.OTHER
    }

    private fun ScannedDeviceState.toMode(): ScannedDeviceMode = when (this) {
        ScannedDeviceState.BOOT -> ScannedDeviceMode.BOOT
        ScannedDeviceState.PAIRING -> ScannedDeviceMode.PAIRING
        else -> ScannedDeviceMode.BASE
    }
}
