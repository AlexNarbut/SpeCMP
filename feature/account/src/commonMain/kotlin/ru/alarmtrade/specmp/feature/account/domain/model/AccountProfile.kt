package ru.alarmtrade.specmp.feature.account.domain.model

/** [phone] — как в профиле сервера (формат свободный); `null` — не указан. */
data class AccountProfile(val name: String, val email: String, val phone: String? = null)
