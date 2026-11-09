package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import kotlinx.datetime.LocalDate

/**
 * @property createdOn время сервер отдаёт локальным, без пояса, — дата берётся без пересчёта.
 * @property canVersionCode версия CAN-профиля числом — для сравнения со сборкой блока ([FirmwareCan.version]).
 */
data class FirmwareVersion(
    val id: Long,
    val version: String,
    val createdOn: LocalDate,
    val description: String,
    val url: String,
    val canCode: String? = null,
    val canVersion: String? = null,
    val canVersionCode: Int? = null,
)

/** Признака «страниц больше нет» источник не отдаёт: конец — пустая страница или страница без новых версий. */
data class FirmwareVersionPage(val items: List<FirmwareVersion>, val pageNumber: Int)
