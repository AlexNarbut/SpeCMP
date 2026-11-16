package ru.alarmtrade.specmp.feature.push.data.repository

import co.touchlab.kermit.Logger
import ru.alarmtrade.specialist.interactors.user.requests.SetPushRulesRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.push.data.datasource.PushDataSource
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules
import ru.alarmtrade.specmp.feature.push.domain.repository.PushRulesRepository

internal class SpecialistPushRulesRepository(private val dataSource: PushDataSource) : PushRulesRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun rules(): Response<PushRules> = dataSource.getPushRules()
        .toResponse()
        .map(::PushRules)
        .onError { logger.d(it) { "Push rules are not loaded" } }

    override suspend fun save(rules: PushRules): Response<Unit> =
        dataSource.setPushRules(SetPushRulesRequest(rules.codes))
            .toResponse()
            .onError { logger.d(it) { "Push rules are not saved" } }
}
