package ru.alarmtrade.specmp.feature.push.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale

interface PushTokenRepository {

    /** Привязать [token] к текущему аккаунту; тексты push сервер шлёт на языке [locale]. */
    suspend fun send(token: String, locale: AppLocale): Response<Unit>
}
