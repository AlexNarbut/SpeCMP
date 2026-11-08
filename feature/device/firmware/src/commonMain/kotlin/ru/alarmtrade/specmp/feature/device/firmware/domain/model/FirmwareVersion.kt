package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import kotlinx.datetime.LocalDate

/**
 * Версия прошивки на сервере.
 *
 * @property createdOn дата создания как её отдаёт сервер: время приходит локальным, без часового пояса,
 *   поэтому в дату оно берётся без пересчёта — на другом поясе дата не «уедет».
 * @property description описание версии как есть (может быть пустым); форматирование — в UI.
 * @property url ссылка на файл прошивки: SDK скачивает файл сам при обновлении.
 * @property canCode CAN-код, под который собрана версия (единая CAN-прошивка); `null` — без привязки к CAN.
 * @property canVersion версия CAN-профиля в виде сервера; `null`, если сервер её не отдал.
 * @property canVersionCode та же версия CAN-профиля числом — для сравнения со сборкой блока ([FirmwareCan.version]).
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

/**
 * Страница архива версий. Признака «страниц больше нет» источник не отдаёт: конец списка — пустая страница
 * или страница без новых версий (эвристика, решает потребитель).
 */
data class FirmwareVersionPage(val items: List<FirmwareVersion>, val pageNumber: Int)
