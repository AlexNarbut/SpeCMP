package ru.alarmtrade.specmp.core.localization

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual fun systemAppLocale(): AppLocale {
    val preferredLanguage = NSLocale.preferredLanguages.firstOrNull() as? String
    return preferredLanguage?.let(AppLocale::fromLanguageTag) ?: AppLocale.English
}

/**
 * compose-resources резолвят строки по `NSLocale.preferredLanguages`; повлиять на него можно только `AppleLanguages`,
 * и список кэшируется на старте процесса. Здесь — синхронизация оверрайда с выбором (лечит расхождения после смены
 * per-app языка в настройках iOS); действует со следующего запуска.
 */
internal actual fun applyPlatformAppLocale(locale: AppLocale) = persistPlatformAppLocale(locale)

internal actual fun persistPlatformAppLocale(locale: AppLocale) {
    NSUserDefaults.standardUserDefaults.setObject(value = listOf(locale.languageTag), forKey = APPLE_LANGUAGES_KEY)
}

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"
