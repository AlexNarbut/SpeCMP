package ru.alarmtrade.specmp.mountpassport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller

class AppPassportInstallerRepositoryTest {

    private class Account(private val profile: suspend () -> Response<AccountProfile>) : AccountRepository {
        override suspend fun currentProfile(): Response<AccountProfile> = profile()

        override suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Response<String> =
            error("not used")
    }

    @Test
    fun nameAndPhoneFromProfile() = runTest {
        val repository = AppPassportInstallerRepository(
            Account { Response.Success(AccountProfile("Алексей Смирнов", "a@b.ru", phone = "+7 912 345-67-89")) },
        )

        assertEquals(
            PassportInstaller(name = "Алексей Смирнов", contact = "+7 912 345-67-89"),
            assertIs<Response.Success<PassportInstaller>>(repository.installer()).value,
        )
    }

    @Test
    fun withoutPhoneContactIsEmail() = runTest {
        val repository =
            AppPassportInstallerRepository(Account { Response.Success(AccountProfile("Алексей", "a@b.ru")) })

        assertEquals("a@b.ru", assertIs<Response.Success<PassportInstaller>>(repository.installer()).value.contact)
    }

    @Test
    fun slowProfileIsError() = runTest {
        val repository = AppPassportInstallerRepository(Account { awaitCancellation() })

        assertIs<Response.Error>(repository.installer())
    }
}
