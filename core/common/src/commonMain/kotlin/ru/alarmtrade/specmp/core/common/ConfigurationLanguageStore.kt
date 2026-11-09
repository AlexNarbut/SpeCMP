package ru.alarmtrade.specmp.core.common

/** Язык готовых конфигураций автомобилей: сервер отдаёт каталог и конфигурации только на этом языке. */
enum class ConfigurationLanguage { EN, RU }

/** Применяется сразу, без перезапуска; реализацию даёт `core/specialist` (хранилище SDK). */
interface ConfigurationLanguageStore {
    /** Сохранённый язык; `null` — не прочитался. */
    suspend fun current(): ConfigurationLanguage?

    suspend fun save(language: ConfigurationLanguage): Response<Unit>
}
