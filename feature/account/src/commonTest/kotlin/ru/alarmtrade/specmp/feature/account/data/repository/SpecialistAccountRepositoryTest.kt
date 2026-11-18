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
    private var avatarResponse: SpecialistResponse<String> = SpecialistResponse.Success(AVATAR_URL)
    private val uploadedAvatars = mutableListOf<Triple<Int, String, String>>()
    private val repository = SpecialistAccountRepository(
        object : AccountDataSource {
            override suspend fun currentUser(): SpecialistResponse<User> = userResponse

            override suspend fun uploadAvatar(
                bytes: ByteArray,
                fileName: String,
                mimeType: String,
            ): SpecialistResponse<String> {
                uploadedAvatars += Triple(bytes.size, fileName, mimeType)
                return avatarResponse
            }
        },
    )

    @Test
    fun `avatar upload passes the image and returns the new url`() = runTest {
        val result = repository.uploadAvatar(ByteArray(3), "avatar.jpg", "image/jpeg")

        assertEquals(Response.Success(AVATAR_URL), result)
        assertEquals(listOf(Triple(3, "avatar.jpg", "image/jpeg")), uploadedAvatars)
    }

    @Test
    fun `avatar upload without network becomes a common no-connection error`() = runTest {
        avatarResponse = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.uploadAvatar(ByteArray(1), "a.jpg", "image/jpeg"))

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `avatar upload server failure keeps the SDK exception and message`() = runTest {
        val failure = TokenIsExpired("file is too large")
        avatarResponse = SpecialistResponse.Error.General(failure)

        val result = assertIs<Response.Error>(repository.uploadAvatar(ByteArray(1), "a.jpg", "image/jpeg"))

        assertSame(failure, result.exception)
        assertEquals("file is too large", result.message)
    }

    @Test
    fun `profile takes the full name, the login as email and the phone`() = runTest {
        assertEquals(
            Response.Success(AccountProfile(name = "Иван Иванов", email = "ivan@mail.ru", phone = "+79000000000")),
            repository.currentProfile(),
        )
    }

    @Test
    fun `profile takes the photo url as avatar`() = runTest {
        userResponse = SpecialistResponse.Success(user(photoUrl = "https://example.com/avatar.jpg"))

        val profile = assertIs<Response.Success<AccountProfile>>(repository.currentProfile()).value

        assertEquals("https://example.com/avatar.jpg", profile.avatarUrl)
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

    private fun user(photoUrl: String = ""): User {
        val date = LocalDateTime(year = 2026, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0)
        return User(
            id = 1,
            login = "ivan@mail.ru",
            userContactInformation = UserContactInformation(
                fullName = "Иван Иванов",
                phoneNumber = "+79000000000",
                company = "",
                address = "",
                photoUrl = photoUrl,
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

    private companion object {
        const val AVATAR_URL = "https://example.com/avatars/new.jpg"
    }
}
