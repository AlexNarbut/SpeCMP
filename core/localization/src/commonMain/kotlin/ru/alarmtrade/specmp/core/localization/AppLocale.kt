package ru.alarmtrade.specmp.core.localization

import com.russhwolf.settings.Settings

enum class AppLocale(val languageTag: String) {
    English("en"),
    Russian("ru");

    companion object {
        fun fromLanguageTag(languageTag: String): AppLocale =
            if (languageTag.startsWith(Russian.languageTag)) Russian else English
    }
}

private const val APP_LOCALE_SETTINGS_KEY = "app_locale"

private val appLocaleSettings: Settings by lazy { Settings() }

fun currentAppLocale(): AppLocale = savedAppLocale() ?: systemAppLocale()

fun savedAppLocale(): AppLocale? =
    appLocaleSettings.getStringOrNull(APP_LOCALE_SETTINGS_KEY)?.let(AppLocale::fromLanguageTag)

fun prepareAppLocale(): AppLocale = currentAppLocale().also(::applyPlatformAppLocale)

fun applyAppLocale(locale: AppLocale) {
    appLocaleSettings.putString(APP_LOCALE_SETTINGS_KEY, locale.languageTag)
    applyPlatformAppLocale(locale)
}

expect fun systemAppLocale(): AppLocale

internal expect fun applyPlatformAppLocale(locale: AppLocale)
