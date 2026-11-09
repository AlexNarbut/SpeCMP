package ru.alarmtrade.specmp.feature.main.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesResponse
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.domain.chat.ChatAvailability
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

class SpecialistMainRepositoryTest {
    private val dataSource = FakeMainDataSource()
    private val accountRepository = FakeAccountRepository(
        Response.Success(AccountProfile(name = "Иван Иванов", email = "ivan@mail.ru")),
    )
    private val repository = SpecialistMainRepository(
        dataSource = dataSource,
        accountRepository = accountRepository,
        appVersionProvider = { AppVersion(name = "1.4.0", code = APP_VERSION_CODE) },
    )

    @Test
    fun `user profile comes from the account repository`() = runTest {
        assertEquals(
            Response.Success(MainUserProfile(name = "Иван Иванов", email = "ivan@mail.ru")),
            repository.currentUserProfile(),
        )

        val failure = IllegalStateException("no profile")
        accountRepository.profile = Response.Error(failure)
        assertSame(failure, assertIs<Response.Error>(repository.currentUserProfile()).exception)
    }

    @Test
    fun `settings version is unwrapped`() = runTest {
        dataSource.settingsVersion = SpecialistResponse.Success(LongWrapper(42))

        assertEquals(Response.Success(42L), repository.currentSettingsVersion())
    }

    @Test
    fun `missing settings version on a fresh install is not an error`() = runTest {
        dataSource.settingsVersion = SpecialistResponse.Error.General(Exception("settingsVersion not found"))

        assertEquals(Response.Success(null), repository.currentSettingsVersion())
    }

    @Test
    fun `other settings version failures stay errors`() = runTest {
        dataSource.settingsVersion = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.currentSettingsVersion())

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `synchronization stages map to domain statuses`() = runTest {
        dataSource.synchronization = flowOf(
            SpecialistResponse.Success(SettingsUpdatingStatus.CheckLocalVersion()),
            SpecialistResponse.Success(SettingsUpdatingStatus.DownloadingNewVersion(newVersion = 7)),
            SpecialistResponse.Success(SettingsUpdatingStatus.UpdateFromDownloadedFileStarted(newVersion = 7)),
            SpecialistResponse.Success(SettingsUpdatingStatus.FinishedSuccess(newVersion = 7)),
        )

        assertEquals(
            listOf(
                Response.Success(MainDatabaseSyncStatus.InProgress.CheckingVersion),
                Response.Success(MainDatabaseSyncStatus.InProgress.Downloading),
                Response.Success(MainDatabaseSyncStatus.InProgress.Applying),
                Response.Success(MainDatabaseSyncStatus.Finished.Success(newVersion = 7)),
            ),
            repository.synchronizeSettingsVersion().toList(),
        )
    }

    @Test
    fun `incompatible settings version reports the required app version`() = runTest {
        dataSource.synchronization = flowOf(
            SpecialistResponse.Success(
                SettingsUpdatingStatus.FinishedVersionNotCompatible(newVersion = 9, minRequiredAppVersion = 150),
            ),
        )

        assertEquals(
            listOf(Response.Success(MainDatabaseSyncStatus.Finished.NotCompatible(minRequiredAppVersion = 150))),
            repository.synchronizeSettingsVersion().toList(),
        )
    }

    @Test
    fun `synchronization that throws ends with an error instead of crashing`() = runTest {
        val failure = IllegalStateException("db locked")
        dataSource.synchronization = flow {
            emit(SpecialistResponse.Success(SettingsUpdatingStatus.CheckLocalVersion()))
            throw failure
        }

        val emitted = repository.synchronizeSettingsVersion().toList()

        assertEquals(Response.Success(MainDatabaseSyncStatus.InProgress.CheckingVersion), emitted.first())
        assertSame(failure, assertIs<Response.Error>(emitted.last()).exception)
    }

    @Test
    fun `newer app version is available and the request carries the current version`() = runTest {
        dataSource.appUpdates = updateAvailable(version = " 1.5.0 ", versionInt = APP_VERSION_CODE + 1)

        assertEquals(
            Response.Success(MainAppUpdateCheckResult.Available(versionName = "1.5.0")),
            repository.checkAppUpdate(),
        )
        assertEquals(APP_VERSION_CODE, dataSource.appUpdateRequests.single().currentVersion)
    }

    @Test
    fun `same version, blank version or no updates mean up to date`() = runTest {
        dataSource.appUpdates = updateAvailable(version = "1.4.0", versionInt = APP_VERSION_CODE)
        assertEquals(Response.Success(MainAppUpdateCheckResult.UpToDate), repository.checkAppUpdate())

        dataSource.appUpdates = updateAvailable(version = " ", versionInt = APP_VERSION_CODE + 1)
        assertEquals(Response.Success(MainAppUpdateCheckResult.UpToDate), repository.checkAppUpdate())

        dataSource.appUpdates = SpecialistResponse.Success(AppUpdatesResponse.NoUpdates())
        assertEquals(Response.Success(MainAppUpdateCheckResult.UpToDate), repository.checkAppUpdate())
    }

    @Test
    fun `user features and commercial data map to domain`() = runTest {
        dataSource.appFeatures = SpecialistResponse.Success(listOf(UserAppFeature.CAN_SCANNER))
        dataSource.commercial = SpecialistResponse.Success(UserCommercial(warehouse1CUrl = "https://1c"))

        assertEquals(
            Response.Success(MainUserAppFeatures(hasCanScanner = true)),
            repository.currentUserAppFeatures(),
        )
        assertEquals(Response.Success(MainUserCommercial("https://1c")), repository.currentUserCommercial())

        dataSource.commercial = SpecialistResponse.Success(null)
        assertEquals(Response.Success(MainUserCommercial()), repository.currentUserCommercial())
    }

    @Test
    fun `chat availability is polled and errors do not stop polling`() = runTest {
        dataSource.chatAvailabilities += SpecialistResponse.Success(ChatAvailability(statusCode = 0, reason = ""))
        dataSource.chatAvailabilities += SpecialistResponse.Error.Network(NetworkIsNotAvailableException())
        dataSource.chatAvailabilities += SpecialistResponse.Success(ChatAvailability(statusCode = 3, reason = "off"))

        val emitted = repository.observeChatAvailability().take(3).toList()

        assertEquals(Response.Success(true), emitted[0])
        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(emitted[1]).exception.appError)
        assertEquals(Response.Success(false), emitted[2])
        assertEquals(CHAT_POLL_MILLIS * 2, currentTime)
    }

    private fun updateAvailable(version: String, versionInt: Int): SpecialistResponse<AppUpdatesResponse> =
        SpecialistResponse.Success(
            AppUpdatesResponse.UpdateAvailable(
                UpdateVersionInformation(
                    id = 1,
                    version = version,
                    versionInt = versionInt,
                    description = "",
                    updateChannelId = 1,
                    downloadUrl = "",
                    publishedDate = LocalDateTime(year = 2026, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0),
                ),
            ),
        )

    private companion object {
        const val APP_VERSION_CODE = 140
        const val CHAT_POLL_MILLIS = 10_000L
    }
}
