package ru.alarmtrade.specmp.feature.device.selection.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.bluetoothControl.domain.bluetooth.scan.ScannedDeviceState
import ru.alarmtrade.common.core.PlatformType
import ru.alarmtrade.common.core.platformType
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.DeviceConnectionType
import ru.alarmtrade.specmp.core.specialist.supportedConnectionTypes
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository
import ru.alarmtrade.specialist.domain.bluetooth.scan.BleDeviceScanResult
import ru.alarmtrade.specialist.domain.usb.AttachedUsbDeviceInformation
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor

/**
 * Реализация [DeviceScanRepository] над Specialist SDK.
 *
 * Интеракторы транспортов резолвятся **лениво** (провайдеры) и через [resolveOrNull]:
 * недоступный на платформе транспорт (BLE на desktop, USB на iOS) даёт пустой список,
 * а не роняет создание графа/репозитория.
 */
class SpecialistDeviceScanRepository(
    private val bluetoothConnectorInteractor: () -> BluetoothConnectorInteractor,
    private val usbConnectorInteractor: () -> UsbConnectorInteractor,
) : DeviceScanRepository {

    private val connectionTypes: Set<DeviceConnectionType> = supportedConnectionTypes()

    override fun supportedTransports(): Set<DeviceTransport> = buildSet {
        if (DeviceConnectionType.BLUETOOTH in connectionTypes) add(DeviceTransport.BLE)
        if (DeviceConnectionType.USB in connectionTypes) add(DeviceTransport.USB)
    }

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
                    .map { response -> response.toBleResponse() }
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
                    .map { response -> response.toUsbResponse() }
            },
        )
    }

    override suspend fun stopScan() {
        bluetoothConnectorInteractor.resolveOrNull()?.stopBluetoothScanUseCase?.execute()
    }

    private fun SpecialistResponse<List<BleDeviceScanResult>>.toBleResponse(): Response<List<ScannedDevice.Ble>> =
        when (this) {
            is SpecialistResponse.Success -> Response.Success(value.map { it.toDomain() })
            is SpecialistResponse.Error -> Response.Error(exception, message)
        }

    private fun SpecialistResponse<List<AttachedUsbDeviceInformation>>.toUsbResponse(): Response<List<ScannedDevice.Usb>> =
        when (this) {
            is SpecialistResponse.Success -> Response.Success(value.map { it.toDomain() })
            is SpecialistResponse.Error -> Response.Error(exception, message)
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

    // По имени, а не по enum SDK: набор типов растёт, неизвестные уходят в OTHER.
    private fun String.toDeviceKind(): DeviceKind = when {
        this == "ALARM" || this == "MICROBOT" -> DeviceKind.ALARM
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

/**
 * Резолвит зависимость провайдера, возвращая null, если она недоступна на платформе
 * (SDK бросает при обращении к неподдерживаемому транспорту). Отмена пробрасывается.
 */
private fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
