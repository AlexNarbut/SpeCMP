package ru.alarmtrade.specmp.feature.fastmount.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

/** Итог мастера на «Быстрый монтаж завершён»: из него оформляется паспорт монтажа. */
data class FastMountResult(
    val unit: FastMountUnit,
    val car: MountCar?,
    val configuration: FastMountResultConfiguration?,
    val works: FastMountWorks,
    val systemCheck: List<FastMountCheck>,
    val transport: ConnectionTransport?,
)

/** Блок, как его знает мастер: [firmware] — версия без `VER`, [canVersion] — версия CAN-прошивки. */
data class FastMountUnit(
    val systemName: String,
    val systemId: Long,
    val firmware: String,
    val canCode: String?,
    val canVersion: String?,
    val canFactory: Boolean,
)

data class FastMountResultConfiguration(val id: Int, val name: String, val author: String)

data class FastMountWorks(
    val settingsWritten: Boolean,
    val bypass: FastMountBypass,
    val clone: Boolean,
    val slave: Boolean,
    val durationMinutes: Int,
)

/** `CLONE_NEEDED` — обход потребовал Clone (он выполнен шагом мастера), `NONE` — обхода в конфигурации нет. */
enum class FastMountBypass { TRAINED, CLONE_NEEDED, NONE }

data class FastMountCheck(val title: String, val result: FastMountCheckResult)

enum class FastMountCheckResult { PASSED, FAILED, SKIPPED }

/** Паспорт монтажа, оформляемый с финала: [issued] — документ сформирован хотя бы раз. */
data class FastMountPassport(val id: String, val number: String, val issued: Boolean)
