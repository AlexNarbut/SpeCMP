package ru.alarmtrade.specmp.core.localization

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual fun systemAppLocale(): AppLocale {
    val preferredLanguage = NSLocale.preferredLanguages.firstOrNull() as? String
    return preferredLanguage?.let(AppLocale::fromLanguageTag) ?: AppLocale.English
}

internal actual fun applyPlatformAppLocale(locale: AppLocale) {
    NSUserDefaults.standardUserDefaults.setObject(
        value = listOf(locale.languageTag),
        forKey = "AppleLanguages"
    )
    NSUserDefaults.standardUserDefaults.synchronize()
}
