package ru.alarmtrade.specmp.core.localization

import com.russhwolf.settings.Settings

enum class AppLocale(val languageTag: String) {
    English("en"),
    Russian("ru"),
    ;

    companion object {
        fun fromLanguageTag(languageTag: String): AppLocale =
            if (languageTag.startsWith(Russian.languageTag)) Russian else English
    }
}

/*
 * Язык приложения применяется при запуске процесса, а не на лету (docs/localization.md, «Язык приложения»):
 * compose-resources берут язык из процессной локали (Android — `LocaleList.getDefault()`, iOS —
 * `NSLocale.preferredLanguages`, кэшируется на старте), публичного способа подменить их окружение нет.
 * Смена языка = сохранить выбор + перезапустить приложение (`AppRestarter`).
 *
 * Хранилище — свой `Settings`, а не Koin: на Android язык нужен в `attachBaseContext`, до старта DI.
 */
internal const val APP_LOCALE_SETTINGS_KEY = "app_locale"

private val appLocaleSettings: Settings by lazy { Settings() }

/** Язык приложения: выбранный пользователем, иначе системный. */
fun currentAppLocale(): AppLocale = savedAppLocale() ?: systemAppLocale()

private fun savedAppLocale(): AppLocale? =
    appLocaleSettings.getStringOrNull(APP_LOCALE_SETTINGS_KEY)?.let(AppLocale::fromLanguageTag)

/**
 * Применить язык на старте процесса — до первой композиции (`App()`). На Android основное применение — в
 * `attachBaseContext` Activity ([withAppLocale]), здесь — только процессная локаль на случай других входов.
 */
fun prepareAppLocale(): AppLocale = currentAppLocale().also(::applyPlatformAppLocale)

/** Сохранить выбор пользователя. Действует со следующего запуска — вызывающий перезапускает приложение. */
fun saveAppLocale(locale: AppLocale) {
    appLocaleSettings.putString(APP_LOCALE_SETTINGS_KEY, locale.languageTag)
    persistPlatformAppLocale(locale)
}

expect fun systemAppLocale(): AppLocale

/** Процессная локаль (форматы, compose-resources) — на старте. */
internal expect fun applyPlatformAppLocale(locale: AppLocale)

/**
 * Платформенная часть сохранения: iOS — `AppleLanguages` (его читает система на старте); Android — синхронная запись
 * (перезапуск сразу завершает процесс, а `Settings` пишет асинхронно); desktop — no-op.
 */
internal expect fun persistPlatformAppLocale(locale: AppLocale)

/** Выбор языка для экранов настроек — интерфейсом, чтобы подменять в тестах ([DefaultAppLocaleStore] — Settings). */
interface AppLocaleStore {
    fun current(): AppLocale

    fun save(locale: AppLocale)
}

object DefaultAppLocaleStore : AppLocaleStore {
    override fun current(): AppLocale = currentAppLocale()

    override fun save(locale: AppLocale) = saveAppLocale(locale)
}
