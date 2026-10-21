package ru.alarmtrade.specmp.feature.authstate.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response

interface AuthStateRepository {
    fun observeIsLoggedIn(): Flow<Response<Boolean>>
    suspend fun logout(): Response<Unit>
}
