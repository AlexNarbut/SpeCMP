package ru.alarmtrade.specmp.core.localization

import platform.Foundation.NSUserDefaults

actual fun currentAppLocale(): AppLocale {
    return AppLocale.English
}

actual fun applyAppLocale(locale: AppLocale) {
    NSUserDefaults.standardUserDefaults.setObject(
        value = listOf(locale.languageTag),
        forKey = "AppleLanguages",
    )
    NSUserDefaults.standardUserDefaults.synchronize()
}
