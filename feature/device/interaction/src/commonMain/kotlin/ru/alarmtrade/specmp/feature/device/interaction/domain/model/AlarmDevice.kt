package ru.alarmtrade.specmp.feature.device.interaction.domain.model

/** Подключённый блок сигнализации (информация + возможности), без SDK-типов. */
data class AlarmDevice(
    val systemId: Long,
    val systemName: String,
    /** Версия ПО как её отдаёт блок (`VER3.57`) — так же уходит на сервер; для показа — [displayFirmwareVersion]. */
    val firmwareVersion: String,
    val mode: DeviceOperationMode,
    /** Возможности блока; `null` — прочитать не удалось (блок не поддерживается приложением). */
    val features: Set<DeviceFeature>?,
    /** CAN-прошивка — только у блоков с отдельной CAN-прошивкой (`SINGLE_CAN_FIRMWARE`). */
    val can: CanInfo?,
    /**
     * Модель блока есть в базе устройств на телефоне. `false` — возможности не прочитаны, потому что базы нет или
     * она устарела (а не потому, что блок не поддерживается): нужна синхронизация базы.
     */
    val modelInDatabase: Boolean = true,
)

/** Подпись блока «DX-9X  1234567» (модель и номер) — шапки экранов и маршрутов. */
fun deviceTitle(systemName: String, systemId: Long): String = "$systemName  $systemId"

val AlarmDevice.title: String get() = deviceTitle(systemName, systemId)

/** Версия ПО для показа: без префикса `VER` (`3.57`). */
val AlarmDevice.displayFirmwareVersion: String
    get() = firmwareVersion.removePrefix("VER").trim()

/**
 * Данные CAN-прошивки блока. [isFactory] — заводские CAN-настройки: сначала нужно прошить блок под CAN-код авто.
 * [firmwareVersion] — версия CAN-профиля для показа, [version] — она же числом (так её принимает сервер в запросах
 * прошивок под CAN; `null` — блок не сообщил).
 */
data class CanInfo(val firmwareVersion: String, val code: String, val isFactory: Boolean, val version: Int? = null)

/** Режим работы основной программы блока: рабочий или загрузчик (прошивка/восстановление). */
enum class DeviceOperationMode { MAIN, BOOT }

/** Режим Bluetooth-модуля блока (только BLE-подключение): рабочий или загрузчик (обновление BLE-прошивки). */
enum class BluetoothModuleMode { MAIN, BOOT }

/** Bluetooth-модуль блока на текущем BLE-подключении: режим + мануфактура. */
data class BluetoothModule(val mode: BluetoothModuleMode, val manufacturer: BluetoothManufacturer)

/** Мануфактура Bluetooth-модуля (нужна в загрузчике BLE: показ и обновление BLE-прошивки). */
data class BluetoothManufacturer(
    val serialNumber: String,
    val firmwareName: String,
    val deviceName: String,
    val bootFirmwareVersion: String,
    val mainFirmwareVersion: String,
)

/** Возможности блока, от которых зависит меню работы с устройством (подмножество `SubsystemFeature` SDK). */
enum class DeviceFeature {
    FIRMWARE_UPDATE,
    VOICE_FIRMWARE,
    SETTINGS,
    BYPASS_LEARNING,
    CLONE,
    DYNAMIC_DEVICES,
}

/** Связь с блоком по данным SDK (`getDeviceConnectionStateFlowUseCase`). */
enum class DeviceLinkState { CONNECTING, READY, DISCONNECTED }

/**
 * Состояние сессии для экранов: пока связи нет — Disconnected/Connecting; на связи — режим блока,
 * определённый на этом подключении (после переподключения определяется заново).
 */
sealed interface DeviceSessionState {
    data object Connecting : DeviceSessionState

    data object Disconnected : DeviceSessionState

    /** Блок в рабочем режиме — основной flow работы с устройством. */
    data class Ready(val device: AlarmDevice) : DeviceSessionState

    /** BLE: Bluetooth-модуль в загрузчике (обновление BLE-прошивки); основная программа недоступна. */
    data class BluetoothBoot(val manufacturer: BluetoothManufacturer) : DeviceSessionState

    /** Основная программа блока в загрузчике (обновление основной прошивки). */
    data class FirmwareBoot(val device: AlarmDevice) : DeviceSessionState
}

/**
 * Блок на связи — в рабочем режиме или основная программа в загрузчике; `null` — связи нет или BLE-модуль в загрузчике.
 */
val DeviceSessionState.deviceOrNull: AlarmDevice?
    get() = when (this) {
        is DeviceSessionState.Ready -> device
        is DeviceSessionState.FirmwareBoot -> device
        DeviceSessionState.Connecting, DeviceSessionState.Disconnected, is DeviceSessionState.BluetoothBoot -> null
    }

/** Блок (основная программа или Bluetooth-модуль) в загрузчике. */
val DeviceSessionState.isBoot: Boolean
    get() = this is DeviceSessionState.BluetoothBoot || this is DeviceSessionState.FirmwareBoot

/** Связи нет: обрыв или переподключение. */
val DeviceSessionState.isLinkDown: Boolean
    get() = this is DeviceSessionState.Disconnected || this is DeviceSessionState.Connecting
