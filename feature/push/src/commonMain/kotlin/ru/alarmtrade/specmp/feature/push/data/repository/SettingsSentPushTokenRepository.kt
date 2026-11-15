package ru.alarmtrade.specmp.feature.push.data.repository

import com.russhwolf.settings.Settings
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken
import ru.alarmtrade.specmp.feature.push.domain.repository.SentPushTokenRepository

/** Неполная запись (обрыв записи, старая версия) — «не отправляли»: токен уйдёт заново. */
internal class SettingsSentPushTokenRepository(private val settings: Settings) : SentPushTokenRepository {

    override suspend fun lastSent(): Response<SentPushToken?> = safeRun {
        val token = settings.getStringOrNull(KEY_TOKEN)
        val language = settings.getStringOrNull(KEY_LANGUAGE)
        val sentAt = settings.getLongOrNull(KEY_SENT_AT)
        if (token == null || language == null || sentAt == null) {
            null
        } else {
            SentPushToken(token, AppLocale.fromLanguageTag(language), Instant.fromEpochMilliseconds(sentAt))
        }
    }

    override suspend fun save(sent: SentPushToken): Response<Unit> = safeRun {
        settings.putString(KEY_TOKEN, sent.token)
        settings.putString(KEY_LANGUAGE, sent.locale.languageTag)
        settings.putLong(KEY_SENT_AT, sent.sentAt.toEpochMilliseconds())
    }

    override suspend fun release() {
        settings.clear()
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_LANGUAGE = "language"
        const val KEY_SENT_AT = "sent_at"
    }
}
