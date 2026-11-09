package ru.alarmtrade.specmp.feature.login.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.AuthFailure
import ru.alarmtrade.specialist.domain.errors.UserIsNotConfirmed
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials

class SpecialistLoginRepositoryTest {
    private val dataSource = FakeLoginDataSource()
    private val repository = SpecialistLoginRepository(dataSource)
    private val credentials = LoginCredentials(login = "  user@mail.ru ", password = "secret")

    @Test
    fun `login syncs the client first and sends the trimmed login`() = runTest {
        val result = repository.login(credentials)

        assertEquals(Response.Success(LoginResult.Authorized), result)
        assertEquals(1, dataSource.syncCalls)
        assertEquals(listOf<Any>(LoginRequest(username = "user@mail.ru", password = "secret")), dataSource.requests)
    }

    @Test
    fun `unconfirmed user is a successful activation-required result`() = runTest {
        dataSource.result = SpecialistResponse.Error.General(UserIsNotConfirmed("not confirmed"))

        assertEquals(Response.Success(LoginResult.ActivationRequired), repository.login(credentials))
    }

    @Test
    fun `rejected credentials keep the SDK exception without a common app error`() = runTest {
        val failure = AuthFailure("wrong password")
        dataSource.result = SpecialistResponse.Error.General(failure, message = "Неверный пароль")

        val result = assertIs<Response.Error>(repository.login(credentials))

        assertSame(failure, result.exception)
        assertEquals(null, result.exception.appError)
        assertEquals("Неверный пароль", result.message)
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        dataSource.result = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.login(credentials))

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `failed client sync stops login before the SDK call`() = runTest {
        val failure = IllegalStateException("sync failed")
        dataSource.syncResult = Response.Error(failure)

        val result = assertIs<Response.Error>(repository.login(credentials))

        assertSame(failure, result.exception)
        assertTrue(dataSource.requests.isEmpty())
    }

    @Test
    fun `sign up sends normalized fields`() = runTest {
        val signUp = SignUpCredentials(
            fullName = " Иван Иванов ",
            email = " ivan@mail.ru",
            phoneNumber = "+7 900 000-00-00 ",
            password = "secret1",
            isInstallerConfirmed = true,
        )

        assertEquals(Response.Success(Unit), repository.signUp(signUp))
        assertEquals(
            listOf<Any>(
                SignUpRequest(
                    email = "ivan@mail.ru",
                    password = "secret1",
                    fullName = "Иван Иванов",
                    phoneNumber = "+7 900 000-00-00",
                ),
            ),
            dataSource.requests,
        )
        assertEquals(0, dataSource.syncCalls)
    }

    @Test
    fun `reset password sends the trimmed email and maps errors`() = runTest {
        dataSource.result = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.resetPassword(ResetPasswordCredentials(" ivan@mail.ru ")))

        assertEquals(ApiError.NoConnection, result.exception.appError)
        assertEquals("ivan@mail.ru", assertIs<ResetPasswordRequest>(dataSource.requests.single()).login)
    }

    @Test
    fun `resend activation email syncs the client and sends credentials`() = runTest {
        assertEquals(Response.Success(Unit), repository.resendActivationEmail(credentials))

        assertEquals(1, dataSource.syncCalls)
        assertEquals(
            listOf<Any>(ResendActivationEmailRequest(email = "user@mail.ru", password = "secret")),
            dataSource.requests,
        )
    }
}
