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
)

/** Версия ПО для показа: без префикса `VER` (`3.57`). */
val AlarmDevice.displayFirmwareVersion: String
    get() = firmwareVersion.removePrefix("VER").trim()

/** Данные CAN-прошивки блока. [isFactory] — заводские CAN-настройки: сначала нужно прошить блок под CAN-код авто. */
data class CanInfo(
    val firmwareVersion: String,
    val code: String,
    val isFactory: Boolean,
)

/** Режим работы блока: рабочий или загрузчик (прошивка/восстановление). */
enum class DeviceOperationMode { MAIN, BOOT }

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

/** Состояние сессии для экранов: пока связи нет — Disconnected/Connecting, на связи — Ready с блоком. */
sealed interface DeviceSessionState {
    data object Connecting : DeviceSessionState

    data object Disconnected : DeviceSessionState

    data class Ready(val device: AlarmDevice) : DeviceSessionState
}
