package ru.alarmtrade.specmp.feature.push.testing

import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore

class FakeAppLocaleStore(var locale: AppLocale = AppLocale.Russian) : AppLocaleStore {
    val saved = mutableListOf<AppLocale>()

    override fun current(): AppLocale = locale

    override fun save(locale: AppLocale) {
        saved += locale
        this.locale = locale
    }
}
