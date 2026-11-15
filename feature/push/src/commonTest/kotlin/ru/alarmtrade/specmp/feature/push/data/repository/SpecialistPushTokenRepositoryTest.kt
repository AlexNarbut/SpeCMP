package ru.alarmtrade.specmp.feature.push.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.AppLanguage
import ru.alarmtrade.specialist.interactors.user.requests.SetPushRulesRequest
import ru.alarmtrade.specialist.interactors.user.requests.UpdatePushTokenRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.feature.push.data.datasource.PushDataSource

class SpecialistPushTokenRepositoryTest {

    private val requests = mutableListOf<UpdatePushTokenRequest>()
    private var response: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)
    private val repository = SpecialistPushTokenRepository(
        object : PushDataSource {
            override suspend fun updatePushToken(request: UpdatePushTokenRequest): SpecialistResponse<Unit> {
                requests += request
                return response
            }

            override suspend fun getPushRules(): SpecialistResponse<Set<Int>> = error("Not used")

            override suspend fun setPushRules(request: SetPushRulesRequest): SpecialistResponse<Unit> =
                error("Not used")
        },
    )

    @Test
    fun `token goes to the server with the interface language`() = runTest {
        assertEquals(Response.Success(Unit), repository.send("ru-token", AppLocale.Russian))
        assertEquals(Response.Success(Unit), repository.send("en-token", AppLocale.English))

        assertEquals(
            listOf("ru-token" to AppLanguage.RU, "en-token" to AppLanguage.EN),
            requests.map { it.token to it.language },
        )
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        response = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.send("token", AppLocale.Russian))

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }
}
