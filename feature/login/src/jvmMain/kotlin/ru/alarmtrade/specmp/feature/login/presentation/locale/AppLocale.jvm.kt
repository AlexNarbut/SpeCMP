package ru.alarmtrade.specmp.feature.login.presentation.locale

import java.util.Locale

actual fun currentAppLocale(): AppLocale {
    return AppLocale.fromLanguageTag(Locale.getDefault().toLanguageTag())
}

actual fun applyAppLocale(locale: AppLocale) {
    Locale.setDefault(Locale.forLanguageTag(locale.languageTag))
}
