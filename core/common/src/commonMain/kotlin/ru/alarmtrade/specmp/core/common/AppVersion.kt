package ru.alarmtrade.specmp.core.common

data class AppVersion(val name: String, val code: Int)

fun interface AppVersionProvider {
    fun current(): AppVersion
}
