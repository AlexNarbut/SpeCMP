package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * Порядок SDK: GOBOOT ([WaitingForBoot]: блок перезагружают по питанию) → проверка загрузчика → передача → CRC →
 * применение.
 */
sealed interface SerialFirmwareProgress {
    data object WaitingForBoot : SerialFirmwareProgress

    data object CheckingBoot : SerialFirmwareProgress

    data class Sending(val percent: Int?) : SerialFirmwareProgress

    data object Verifying : SerialFirmwareProgress

    data object Applying : SerialFirmwareProgress

    data object Finished : SerialFirmwareProgress
}

class SerialFirmwareException(val failure: SerialFirmwareFailure, cause: Throwable? = null) :
    Exception("Serial firmware update failed: $failure", cause)

fun Throwable.toSerialFirmwareFailure(): SerialFirmwareFailure =
    (this as? SerialFirmwareException)?.failure ?: SerialFirmwareFailure.Unknown

enum class SerialFirmwareFailure {
    File,

    /** Блок не ответил на GOBOOT за отведённое время — не перезагрузили по питанию или не тот порт. */
    BootTimeout,

    NotInBoot,

    Transfer,
    Crc,
    Applying,

    ConnectionLost,
    Unknown,
}
