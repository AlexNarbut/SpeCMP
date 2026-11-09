package ru.alarmtrade.specmp.core.specialist.mapper

import ru.alarmtrade.common.app.domain.AppLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage

internal fun AppLanguage.toDomain(): ConfigurationLanguage = when (this) {
    AppLanguage.RU -> ConfigurationLanguage.RU
    AppLanguage.EN -> ConfigurationLanguage.EN
}

internal fun ConfigurationLanguage.toSdk(): AppLanguage = when (this) {
    ConfigurationLanguage.RU -> AppLanguage.RU
    ConfigurationLanguage.EN -> AppLanguage.EN
}
