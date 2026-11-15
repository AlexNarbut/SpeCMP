package ru.alarmtrade.specmp.feature.push.data.repository

import co.touchlab.kermit.Logger
import ru.alarmtrade.specialist.domain.AppLanguage
import ru.alarmtrade.specialist.interactors.user.requests.UpdatePushTokenRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.push.data.datasource.PushDataSource
import ru.alarmtrade.specmp.feature.push.domain.repository.PushTokenRepository

internal class SpecialistPushTokenRepository(private val dataSource: PushDataSource) : PushTokenRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun send(token: String, locale: AppLocale): Response<Unit> =
        dataSource.updatePushToken(UpdatePushTokenRequest(token, locale.toLanguage()))
            .toResponse()
            .onError { logger.d(it) { "Push token is not sent" } }

    private fun AppLocale.toLanguage(): AppLanguage = when (this) {
        AppLocale.Russian -> AppLanguage.RU
        AppLocale.English -> AppLanguage.EN
    }
}
