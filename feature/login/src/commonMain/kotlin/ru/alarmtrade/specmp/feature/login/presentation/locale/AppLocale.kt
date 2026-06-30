package ru.alarmtrade.specmp.feature.login.presentation.locale

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

expect fun currentAppLocale(): AppLocale

expect fun applyAppLocale(locale: AppLocale)
