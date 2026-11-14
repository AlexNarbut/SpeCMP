package ru.alarmtrade.specmp.feature.appupdate.domain.service

/** Идёт ли работа с блоком: установка закроет приложение и оборвёт связь. Реализация — в composition root. */
fun interface AppUpdateDeviceActivity {
    fun isBusy(): Boolean
}
