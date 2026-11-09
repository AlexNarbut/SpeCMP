package ru.alarmtrade.specmp.feature.fastmount.domain.model

data class UnitAccess(val granted: Boolean, val message: String)

data class MountCar(val id: Int, val brand: String, val model: String, val years: String, val canCode: String) {
    val title: String get() = listOf(brand, model, years).filter { it.isNotBlank() }.joinToString(" ")
}

/** Сравнение версий прошивки: [this] новее [installed] (строки как их отдают сервер и блок, `VER3.57` / `3.57`). */
fun String.isNewerFirmwareThan(installed: String): Boolean {
    val server = firmwareParts()
    val unit = installed.firmwareParts()
    if (server.isEmpty() || unit.isEmpty()) return false
    for (index in 0 until maxOf(server.size, unit.size)) {
        val a = server.getOrElse(index) { 0 }
        val b = unit.getOrElse(index) { 0 }
        if (a != b) return a > b
    }
    return false
}

private val FirmwareNumbers = Regex("""\d+(?:\.\d+)*""")

private fun String.firmwareParts(): List<Int> =
    FirmwareNumbers.find(trim().removePrefix("VER"))?.value?.split('.')?.map { it.toIntOrNull() ?: 0 }.orEmpty()
