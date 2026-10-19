package ru.alarmtrade.specmp.core.specialist

data class SpecialistAppVersion(val name: String, val code: Int)

fun interface SpecialistAppVersionProvider {
    fun current(): SpecialistAppVersion
}

val CurrentSpecialistAppVersion = SpecialistAppVersion(
    name = "2.0.40",
    code = 240
)
