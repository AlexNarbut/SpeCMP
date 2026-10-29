package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * Стадии обновления основной программы блока по COM-порту (`.frm`). Порядок SDK: GOBOOT ([WaitingForBoot]: блок
 * перезагружают по питанию, пока SDK шлёт команду) → проверка загрузчика → передача → CRC → применение.
 * Терминальные — [Finished] и [Failed].
 */
sealed interface SerialFirmwareProgress {
    /** SDK шлёт GOBOOT: блок нужно перезагрузить по питанию (или он уже в загрузчике). */
    data object WaitingForBoot : SerialFirmwareProgress

    data object CheckingBoot : SerialFirmwareProgress

    /** Передача; [percent] — `null`, пока неизвестен. */
    data class Sending(val percent: Int?) : SerialFirmwareProgress

    data object Verifying : SerialFirmwareProgress

    data object Applying : SerialFirmwareProgress

    data object Finished : SerialFirmwareProgress

    data class Failed(val failure: SerialFirmwareFailure) : SerialFirmwareProgress
}

/** Причина неудачи обновления по COM-порту. */
enum class SerialFirmwareFailure {
    /** Файл не разобран как `.frm` (или не прочитан). */
    File,

    /** Блок не ответил на GOBOOT за отведённое время — не перезагрузили по питанию или не тот порт. */
    BootTimeout,

    /** Блок ответил, но он не в загрузчике `.frm`. */
    NotInBoot,

    Transfer,
    Crc,
    Applying,

    /** COM-порт пропал во время шага; обновление нужно начать заново. */
    ConnectionLost,
    Unknown
}
