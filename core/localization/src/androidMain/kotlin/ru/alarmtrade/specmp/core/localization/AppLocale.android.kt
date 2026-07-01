package ru.alarmtrade.specmp.core.localization

import android.os.LocaleList
import java.util.Locale

actual fun currentAppLocale(): AppLocale {
    return AppLocale.fromLanguageTag(LocaleList.getDefault()[0].toLanguageTag())
}

actual fun applyAppLocale(locale: AppLocale) {
    val platformLocale = Locale.forLanguageTag(locale.languageTag)
    Locale.setDefault(platformLocale)
    LocaleList.setDefault(LocaleList(platformLocale))
}
