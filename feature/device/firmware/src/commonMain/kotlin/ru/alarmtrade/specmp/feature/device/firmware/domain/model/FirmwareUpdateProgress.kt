package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Стадии обновления прошивки. Терминальная — [Finished]; неудача — `Response.Error` с [FirmwareUpdateException]. */
sealed interface FirmwareUpdateProgress {
    /** Подготовка: SDK скачивает файл и разбирает его; процента нет. */
    data object Preparing : FirmwareUpdateProgress

    /** Передача в блок; [percent] — `null`, пока неизвестен. */
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

/** Причина неудачи обновления. */
sealed interface FirmwareFailure {
    /** Блок сообщил об ошибке записи. */
    data class Write(val reason: WriteFailureReason) : FirmwareFailure

    /** Не удалось подготовить прошивку: скачать или разобрать файл. */
    data object Preparation : FirmwareFailure

    data object Unknown : FirmwareFailure
}

/** Ошибка записи данных в блок. */
enum class WriteFailureReason {
    Crc,
    Timeout,
    LostPacket,
    WrongPacketLength,
    DeviceNotReady,
    ApplyingError,
    Unknown,
}
