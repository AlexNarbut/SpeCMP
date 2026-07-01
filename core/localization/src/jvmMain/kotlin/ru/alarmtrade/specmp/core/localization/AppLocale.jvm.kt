package ru.alarmtrade.specmp.core.localization

import java.util.Locale

actual fun currentAppLocale(): AppLocale {
    return AppLocale.fromLanguageTag(Locale.getDefault().toLanguageTag())
}

actual fun applyAppLocale(locale: AppLocale) {
    Locale.setDefault(Locale.forLanguageTag(locale.languageTag))
}
