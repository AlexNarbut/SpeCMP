package ru.alarmtrade.specmp.core.common.di

import com.russhwolf.settings.Settings

// Единственный платформенный Settings процесса: язык и тема читают его до старта Koin (ключи без префикса),
// остальные получают именованное хранилище из AppSettingsFactory поверх него.
val platformSettings: Settings by lazy { Settings() }
