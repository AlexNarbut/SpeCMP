package ru.alarmtrade.specmp.core.localization

import com.russhwolf.settings.Settings

enum class AppLocale(
    val languageTag: String,
) {
    English("en"),
    Russian("ru");

    companion object {
        fun fromLanguageTag(languageTag: String): AppLocale {
            return if (languageTag.startsWith(Russian.languageTag)) Russian else English
        }
    }
}

private const val AppLocaleSettingsKey = "app_locale"

private val appLocaleSettings: Settings by lazy { Settings() }

fun currentAppLocale(): AppLocale {
    return savedAppLocale() ?: systemAppLocale()
}

fun savedAppLocale(): AppLocale? {
    return appLocaleSettings.getStringOrNull(AppLocaleSettingsKey)?.let(AppLocale::fromLanguageTag)
}

fun prepareAppLocale(): AppLocale {
    return currentAppLocale().also(::applyPlatformAppLocale)
}

fun applyAppLocale(locale: AppLocale) {
    appLocaleSettings.putString(AppLocaleSettingsKey, locale.languageTag)
    applyPlatformAppLocale(locale)
}

expect fun systemAppLocale(): AppLocale

internal expect fun applyPlatformAppLocale(locale: AppLocale)
