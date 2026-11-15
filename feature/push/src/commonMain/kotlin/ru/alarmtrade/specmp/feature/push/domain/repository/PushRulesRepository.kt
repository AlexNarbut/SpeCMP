package ru.alarmtrade.specmp.feature.push.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules

interface PushRulesRepository {

    suspend fun rules(): Response<PushRules>

    suspend fun save(rules: PushRules): Response<Unit>
}
