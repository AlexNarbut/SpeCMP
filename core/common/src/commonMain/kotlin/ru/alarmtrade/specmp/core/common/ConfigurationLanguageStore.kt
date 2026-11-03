package ru.alarmtrade.specmp.core.common

/** Язык готовых конфигураций автомобилей: сервер отдаёт каталог и конфигурации только на этом языке. */
enum class ConfigurationLanguage { EN, RU }

/**
 * Язык конфигураций — отдельная от языка приложения настройка, применяется сразу, без перезапуска: следующий запрос
 * каталога и конфигураций уходит с новым языком. Реализацию даёт `core/specialist` (хранилище SDK).
 */
interface ConfigurationLanguageStore {
    /** Сохранённый язык; `null` — не прочитался. */
    suspend fun current(): ConfigurationLanguage?

    suspend fun save(language: ConfigurationLanguage): Response<Unit>
}
