package ru.alarmtrade.specmp.core.specialist

data class SpecialistAppVersion(val name: String, val code: Int)

fun interface SpecialistAppVersionProvider {
    fun current(): SpecialistAppVersion
}
