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

/**
 * Тот же файл, что у `Settings()` (default SharedPreferences), — запоминается в [withAppLocale] (раньше любой VM).
 * Нужен для синхронной записи: `Settings` пишет через `apply()`, а перезапуск ([saveAppLocale] → `AppRestarter`) сразу
 * завершает процесс — асинхронная запись не успела бы на диск.
 */
@Volatile
private var appPreferences: SharedPreferences? = null

internal actual fun persistPlatformAppLocale(locale: AppLocale) {
    appPreferences?.edit()?.putString(APP_LOCALE_SETTINGS_KEY, locale.languageTag)?.commit()
}

/**
 * Контекст Activity с языком приложения — вызывать из `attachBaseContext`, до первой композиции.
 *
 * Два механизма, потому что язык резолвят по-разному: android-ресурсы — по `Configuration` контекста,
 * compose-resources — по процессной локали (`LocaleList.getDefault()`). Язык в конфигурации Activity нужен ещё и
 * затем, чтобы смена конфигурации (поворот, размер окна при `configChanges`) не возвращала процессную локаль
 * к системной. Пользователь язык не выбирал — контекст как есть (системный язык).
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

/**
 * Снова применить язык приложения к процессной локали — из `Activity.onConfigurationChanged`. При смене конфигурации,
 * которую Activity обрабатывает сама (`configChanges`: поворот, размер окна), система возвращает `Locale.getDefault()`
 * к системному языку, и перерисованный UI (compose-resources) съехал бы на него. Перерисовка — на следующем кадре,
 * поэтому достаточно вызвать до `super`/сразу после.
 */
fun reapplyAppLocale() {
    Locale.setDefault(Locale.forLanguageTag(currentAppLocale().languageTag))
}
