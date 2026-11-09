package ru.alarmtrade.specmp.feature.account.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.TokenIsExpired
import ru.alarmtrade.specialist.domain.user.User
import ru.alarmtrade.specialist.domain.user.UserAccountRoleInformation
import ru.alarmtrade.specialist.domain.user.UserCompetencyLevel
import ru.alarmtrade.specialist.domain.user.UserContactInformation
import ru.alarmtrade.specialist.domain.user.UserRole
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.account.data.datasource.AccountDataSource
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile

class SpecialistAccountRepositoryTest {
    private var userResponse: SpecialistResponse<User> = SpecialistResponse.Success(user())
    private val repository = SpecialistAccountRepository(
        object : AccountDataSource {
            override suspend fun currentUser(): SpecialistResponse<User> = userResponse
        },
    )

    @Test
    fun `profile takes the full name and the login as email`() = runTest {
        assertEquals(
            Response.Success(AccountProfile(name = "Иван Иванов", email = "ivan@mail.ru")),
            repository.currentProfile(),
        )
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        userResponse = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.currentProfile())

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `api failure keeps the SDK exception`() = runTest {
        val failure = TokenIsExpired("expired")
        userResponse = SpecialistResponse.Error.General(failure)

        val result = assertIs<Response.Error>(repository.currentProfile())

        assertSame(failure, result.exception)
        assertEquals("expired", result.message)
    }

    private fun user(): User {
        val date = LocalDateTime(year = 2026, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0)
        return User(
            id = 1,
            login = "ivan@mail.ru",
            userContactInformation = UserContactInformation(
                fullName = "Иван Иванов",
                phoneNumber = "+79000000000",
                company = "",
                address = "",
                photoUrl = "",
            ),
            userAccountRoleInformation = UserAccountRoleInformation(
                role = UserRole.INSTALLER,
                isActivated = true,
                competencyLevel = UserCompetencyLevel.SENIOR,
            ),
            registrationDate = date,
            lastLoginDate = date,
            commercial = null,
        )
    }
}
