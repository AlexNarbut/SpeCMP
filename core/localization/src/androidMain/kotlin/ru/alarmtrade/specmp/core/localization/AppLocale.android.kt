package ru.alarmtrade.specmp.core.localization

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

// Язык системы — из системной конфигурации: процессную локаль приложение подменяет своей (withAppLocale).
actual fun systemAppLocale(): AppLocale =
    AppLocale.fromLanguageTag(Resources.getSystem().configuration.locales[0].toLanguageTag())

internal actual fun applyPlatformAppLocale(locale: AppLocale) {
    Locale.setDefault(Locale.forLanguageTag(locale.languageTag))
}

/** Файл `Settings()` для синхронной записи: `apply()` не успел бы на диск до завершения процесса перезапуском. */
@Volatile
private var appPreferences: SharedPreferences? = null

internal actual fun persistPlatformAppLocale(locale: AppLocale) {
    appPreferences?.edit()?.putString(APP_LOCALE_SETTINGS_KEY, locale.languageTag)?.commit()
}

/**
 * Вызывать из `attachBaseContext`. Android-ресурсы резолвят язык по `Configuration`, compose-resources — по процессной
 * локали; язык в конфигурации ещё и не даёт смене конфигурации вернуть процессную локаль к системной.
 */
fun Context.withAppLocale(): Context {
    appPreferences = applicationContext.getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    val locale = Locale.forLanguageTag(currentAppLocale().languageTag)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    return createConfigurationContext(config)
}

/** Из `onConfigurationChanged`: при `configChanges` система возвращает `Locale.getDefault()` к системному языку. */
fun reapplyAppLocale() {
    Locale.setDefault(Locale.forLanguageTag(currentAppLocale().languageTag))
}
