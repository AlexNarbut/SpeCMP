package ru.alarmtrade.specmp.core.localization

import java.util.Locale

actual fun systemAppLocale(): AppLocale = AppLocale.fromLanguageTag(Locale.getDefault().toLanguageTag())

internal actual fun applyPlatformAppLocale(locale: AppLocale) {
    Locale.setDefault(Locale.forLanguageTag(locale.languageTag))
}

// Выбор читается на старте из Settings — отдельное платформенное хранилище не нужно.
internal actual fun persistPlatformAppLocale(locale: AppLocale) = Unit
