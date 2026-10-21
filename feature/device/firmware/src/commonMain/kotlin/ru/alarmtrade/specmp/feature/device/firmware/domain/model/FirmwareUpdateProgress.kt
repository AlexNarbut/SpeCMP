package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Что обновляем: основную программу блока или Bluetooth-модуль (режим берётся из режима сессии). */
enum class FirmwareUpdateTarget { MainProgram, BluetoothModule }

/** Стадии обновления прошивки. Терминальные — [Finished] и [Failed]. */
sealed interface FirmwareUpdateProgress {
    /** Подготовка: SDK скачивает файл и разбирает его; процента нет. */
    data object Preparing : FirmwareUpdateProgress

    /** Передача в блок; [percent] — `null`, пока неизвестен. */
    data class Sending(val percent: Int?) : FirmwareUpdateProgress

    data object Verifying : FirmwareUpdateProgress

    /** Блок применяет прошивку и перезагружается: связь рвётся до результата — это норма. */
    data object Applying : FirmwareUpdateProgress

    data object Finished : FirmwareUpdateProgress

    data class Failed(val failure: FirmwareFailure) : FirmwareUpdateProgress
}

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
    Unknown
}
