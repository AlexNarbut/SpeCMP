package ru.alarmtrade.specmp.feature.device.interaction.domain.model

data class AlarmDevice(
    val systemId: Long,
    val systemName: String,
    /** Версия ПО как её отдаёт блок (`VER3.57`) — так же уходит на сервер; для показа — [displayFirmwareVersion]. */
    val firmwareVersion: String,
    val mode: DeviceOperationMode,
    /** Возможности блока; `null` — прочитать не удалось (блок не поддерживается приложением). */
    val features: Set<DeviceFeature>?,
    val can: CanInfo?,
    /** `false` — возможности не прочитаны: базы нет или она устарела — нужна синхронизация. */
    val modelInDatabase: Boolean = true,
)

fun deviceTitle(systemName: String, systemId: Long): String = "$systemName  $systemId"

val AlarmDevice.title: String get() = deviceTitle(systemName, systemId)

val AlarmDevice.displayFirmwareVersion: String
    get() = firmwareVersion.removePrefix("VER").trim()

/** [version] — версия CAN-профиля числом, как её принимает сервер; `null` — блок не сообщил. */
data class CanInfo(val firmwareVersion: String, val code: String, val isFactory: Boolean, val version: Int? = null)

enum class DeviceOperationMode { MAIN, BOOT }

enum class BluetoothModuleMode { MAIN, BOOT }

data class BluetoothModule(val mode: BluetoothModuleMode, val manufacturer: BluetoothManufacturer)

data class BluetoothManufacturer(
    val serialNumber: String,
    val firmwareName: String,
    val deviceName: String,
    val bootFirmwareVersion: String,
    val mainFirmwareVersion: String,
)

enum class DeviceFeature {
    FIRMWARE_UPDATE,
    VOICE_FIRMWARE,
    SETTINGS,
    BYPASS_LEARNING,
    CLONE,
    DYNAMIC_DEVICES,
}

enum class DeviceLinkState { CONNECTING, READY, DISCONNECTED }

/** На связи — режим блока, определённый на этом подключении (после переподключения определяется заново). */
sealed interface DeviceSessionState {
    data object Connecting : DeviceSessionState

    data object Disconnected : DeviceSessionState

    data class Ready(val device: AlarmDevice) : DeviceSessionState

    data class BluetoothBoot(val manufacturer: BluetoothManufacturer) : DeviceSessionState

    data class FirmwareBoot(val device: AlarmDevice) : DeviceSessionState
}

val DeviceSessionState.deviceOrNull: AlarmDevice?
    get() = when (this) {
        is DeviceSessionState.Ready -> device
        is DeviceSessionState.FirmwareBoot -> device
        DeviceSessionState.Connecting, DeviceSessionState.Disconnected, is DeviceSessionState.BluetoothBoot -> null
    }

val DeviceSessionState.isBoot: Boolean
    get() = this is DeviceSessionState.BluetoothBoot || this is DeviceSessionState.FirmwareBoot

val DeviceSessionState.isLinkDown: Boolean
    get() = this is DeviceSessionState.Disconnected || this is DeviceSessionState.Connecting
