package ru.alarmtrade.specmp.feature.device.firmware.domain.model

sealed interface FirmwareUpdateProgress {
    data object Preparing : FirmwareUpdateProgress

    data class Sending(val percent: Int?) : FirmwareUpdateProgress

    data object Verifying : FirmwareUpdateProgress

    /** Блок применяет прошивку и перезагружается: связь рвётся до результата — это норма. */
    data object Applying : FirmwareUpdateProgress

    data object Finished : FirmwareUpdateProgress
}

class FirmwareUpdateException(val failure: FirmwareFailure, cause: Throwable? = null) :
    Exception("Firmware update failed: $failure", cause)

fun Throwable.toFirmwareFailure(): FirmwareFailure =
    (this as? FirmwareUpdateException)?.failure ?: FirmwareFailure.Unknown

sealed interface FirmwareFailure {
    data class Write(val reason: WriteFailureReason) : FirmwareFailure

    data object Preparation : FirmwareFailure

    data object Unknown : FirmwareFailure
}

enum class WriteFailureReason {
    Crc,
    Timeout,
    LostPacket,
    WrongPacketLength,
    DeviceNotReady,
    ApplyingError,
    Unknown,
}
