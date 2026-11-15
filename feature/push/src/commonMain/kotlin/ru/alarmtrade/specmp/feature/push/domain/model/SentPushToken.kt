package ru.alarmtrade.specmp.feature.push.domain.model

import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.localization.AppLocale

/** Последний push-токен, принятый сервером: [locale] — язык текстов push, который сервер запомнил для токена. */
data class SentPushToken(val token: String, val locale: AppLocale, val sentAt: Instant)
