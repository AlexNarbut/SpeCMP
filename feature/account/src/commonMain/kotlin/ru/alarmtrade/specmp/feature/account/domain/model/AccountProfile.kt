package ru.alarmtrade.specmp.feature.account.domain.model

/** [phone] — как в профиле сервера (формат свободный); `null` — не указан. [avatarUrl] — абсолютный URL. */
data class AccountProfile(
    val name: String,
    val email: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
)
