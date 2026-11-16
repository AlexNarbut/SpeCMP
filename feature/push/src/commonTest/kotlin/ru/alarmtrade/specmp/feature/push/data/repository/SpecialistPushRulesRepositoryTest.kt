package ru.alarmtrade.specmp.feature.push.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.user.requests.SetPushRulesRequest
import ru.alarmtrade.specialist.interactors.user.requests.UpdatePushTokenRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.push.PushType
import ru.alarmtrade.specmp.feature.push.data.datasource.PushDataSource
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules

class SpecialistPushRulesRepositoryTest {

    private val saved = mutableListOf<Set<Int>>()
    private var rules: SpecialistResponse<Set<Int>> = SpecialistResponse.Success(setOf(1, 2, 5))
    private var saveResponse: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)
    private val repository = SpecialistPushRulesRepository(
        object : PushDataSource {
            override suspend fun updatePushToken(request: UpdatePushTokenRequest): SpecialistResponse<Unit> =
                error("Not used")

            override suspend fun getPushRules(): SpecialistResponse<Set<Int>> = rules

            override suspend fun setPushRules(request: SetPushRulesRequest): SpecialistResponse<Unit> {
                saved += request.types
                return saveResponse
            }
        },
    )

    @Test
    fun `rules keep every server code`() = runTest {
        assertEquals(Response.Success(PushRules(setOf(1, 2, 5))), repository.rules())
    }

    @Test
    fun `toggling a type keeps unsupported codes on save`() = runTest {
        val loaded = assertIs<Response.Success<PushRules>>(repository.rules()).value

        val edited = loaded.with(PushType.CHAT, enabled = false).with(PushType.WARNING, enabled = true)
        assertEquals(Response.Success(Unit), repository.save(edited))

        assertEquals(listOf(setOf(1, 2, 4)), saved)
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        rules = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())
        saveResponse = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(repository.rules()).exception.appError)
        assertEquals(
            ApiError.NoConnection,
            assertIs<Response.Error>(repository.save(PushRules(emptySet()))).exception.appError,
        )
    }
}
