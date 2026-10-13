package ru.alarmtrade.specmp.feature.device.interaction.domain.model

/** Подключённый блок сигнализации (информация + возможности), без SDK-типов. */
data class AlarmDevice(
    val systemId: Long,
    val systemName: String,
    val firmwareVersion: String,
    val mode: DeviceOperationMode,
    val features: Set<DeviceFeature>,
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
