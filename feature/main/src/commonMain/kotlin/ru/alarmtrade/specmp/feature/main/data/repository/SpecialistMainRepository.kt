package ru.alarmtrade.specmp.feature.main.data.repository

import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesResponse
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.appUpdates.requests.AppUpdateRequest
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.user.UserInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersion
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersionProvider
import ru.alarmtrade.specmp.core.specialist.account.AccountRepository
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppVersion
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository

class SpecialistMainRepository(
    private val appUpdatesInteractor: AppUpdatesInteractor,
    private val userInteractor: UserInteractor,
    private val accountRepository: AccountRepository,
    private val chatInteractor: ChatInteractor,
    private val appVersionProvider: SpecialistAppVersionProvider
) : MainRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override val appVersion: MainAppVersion
        get() = appVersionProvider.current().toMainAppVersion()

    override suspend fun currentUserProfile(): Response<MainUserProfile> =
        when (val response = accountRepository.currentProfile()) {
            is Response.Success -> {
                logger.d { "Current user profile fetched" }
                Response.Success(
                    MainUserProfile(
                        name = response.value.name,
                        email = response.value.email
                    )
                )
            }

            is Response.Error -> {
                logger.e(response.exception) { "Failed to fetch current user profile" }
                response
            }
        }

    override suspend fun currentSettingsVersion(): Response<Long?> =
        when (val response = appUpdatesInteractor.getCurrentSettingsVersionUseCase.execute()) {
            is SpecialistResponse.Success -> {
                logger.d { "Current settings version fetched: ${response.value.long}" }
                Response.Success(response.value.long)
            }

            is SpecialistResponse.Error -> {
                // SDK не даёт типизированной ошибки: пустая БД (свежая установка)
                // приходит как Exception("settingsVersion not found"). Это валидное
                // состояние «версии ещё нет», а не сбой — отдаём Success(null).
                if (response.exception.message == "settingsVersion not found") {
                    logger.d { "Settings version not stored yet (fresh install)" }
                    Response.Success(null)
                } else {
                    logger.e(response.exception) { "Failed to fetch current settings version" }
                    response.toResponse()
                }
            }
        }

    override fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>> = flow {
        // Единственное место с безопасной обёрткой: SDK-Flow может бросить
        // мимо Response-контракта, safeFlowRun конвертирует это в Response.Error.
        emitAll(
            safeFlowRun {
                appUpdatesInteractor.synchronizeSettingsVersionUseCase.execute()
                    .map { response -> response.toDatabaseSyncStatusResponse() }
            }
        )
    }

    override suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult> {
        val request = AppUpdateRequest(currentVersion = appVersion.code)
        return when (val response = appUpdatesInteractor.checkForAppUpdatesUseCase.execute(request)) {
            is SpecialistResponse.Success -> when (val value = response.value) {
                is AppUpdatesResponse.UpdateAvailable -> {
                    logger.d { "App update available: ${value.updateVersionInformation.version}" }
                    value.updateVersionInformation.toUpdateCheckResult()
                }

                is AppUpdatesResponse.NoUpdates -> {
                    logger.d { "No app updates available" }
                    Response.Success(MainAppUpdateCheckResult.UpToDate)
                }
            }

            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to check app update" }
                response.toResponse()
            }
        }
    }

    override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> =
        when (val response = userInteractor.getUserAppFeaturesUseCase.execute()) {
            is SpecialistResponse.Success -> {
                logger.d { "User app features fetched: ${response.value}" }
                Response.Success(response.value.toMainUserAppFeatures())
            }

            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to fetch user app features" }
                response.toResponse()
            }
        }

    override suspend fun currentUserCommercial(): Response<MainUserCommercial> =
        when (val response = userInteractor.getUserCommercialUseCase.execute()) {
            is SpecialistResponse.Success -> {
                logger.d { "User commercial fetched" }
                Response.Success(response.value.toMainUserCommercial())
            }

            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to fetch user commercial" }
                response.toResponse()
            }
        }

    // Specialist SDK не отдаёт Flow по чату, поэтому наблюдение эмулируется
    // периодическим опросом one-shot use case'ов (как observeIsLoggedIn в feature/auth-state).
    override fun observeChatAvailability(): Flow<Response<Boolean>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(chatAvailability())
            delay(CHAT_AVAILABILITY_POLL_INTERVAL)
        }
    }

    override fun observeUnreadMessagesCount(): Flow<Response<Int>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(unreadMessagesCount())
            delay(CHAT_UNREAD_POLL_INTERVAL)
        }
    }

    private suspend fun chatAvailability(): Response<Boolean> =
        when (val response = chatInteractor.isAvailableUseCase.execute()) {
            is SpecialistResponse.Success -> {
                logger.d { "Chat availability fetched: ${response.value.isAvailable()}" }
                Response.Success(response.value.isAvailable())
            }

            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to fetch chat availability" }
                response.toResponse()
            }
        }

    private suspend fun unreadMessagesCount(): Response<Int> =
        when (val response = chatInteractor.getUnreadMessagesCountUseCase.execute()) {
            is SpecialistResponse.Success -> {
                logger.d { "Unread messages count fetched: ${response.value.int}" }
                Response.Success(response.value.int)
            }

            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to fetch unread messages count" }
                response.toResponse()
            }
        }

    private fun SpecialistResponse<SettingsUpdatingStatus>.toDatabaseSyncStatusResponse():
        Response<MainDatabaseSyncStatus> =
        when (this) {
            is SpecialistResponse.Success -> {
                logger.d { "Settings sync status: $value" }
                Response.Success(value.toMainDatabaseSyncStatus())
            }

            is SpecialistResponse.Error -> {
                logger.e(exception) { "Failed to synchronize settings version" }
                toResponse()
            }
        }

    private fun SettingsUpdatingStatus.toMainDatabaseSyncStatus(): MainDatabaseSyncStatus = when (this) {
        is SettingsUpdatingStatus.CheckLocalVersion,
        is SettingsUpdatingStatus.CheckNetworkVersion,
        is SettingsUpdatingStatus.CheckVersionCompatibility
        -> MainDatabaseSyncStatus.InProgress.CheckingVersion

        is SettingsUpdatingStatus.DownloadingNewVersion -> MainDatabaseSyncStatus.InProgress.Downloading

        is SettingsUpdatingStatus.UpdateFromDownloadedFileStarted,
        is SettingsUpdatingStatus.UpdateFromLocalFileStarted
        -> MainDatabaseSyncStatus.InProgress.Applying

        is SettingsUpdatingStatus.FinishedSuccess ->
            MainDatabaseSyncStatus.Finished.Success(newVersion.toLong())

        is SettingsUpdatingStatus.FinishedNoUpdates -> MainDatabaseSyncStatus.Finished.NoUpdates

        is SettingsUpdatingStatus.FinishedVersionNotCompatible ->
            MainDatabaseSyncStatus.Finished.NotCompatible(minRequiredAppVersion = minRequiredAppVersion)
    }

    private fun UpdateVersionInformation.toUpdateCheckResult(): Response<MainAppUpdateCheckResult> {
        val versionName = version.trim()

        return if (versionName.isBlank() || versionInt <= appVersion.code) {
            Response.Success(MainAppUpdateCheckResult.UpToDate)
        } else {
            Response.Success(MainAppUpdateCheckResult.Available(versionName = versionName))
        }
    }

    private fun Collection<UserAppFeature>.toMainUserAppFeatures(): MainUserAppFeatures = MainUserAppFeatures(
        hasCanScanner = UserAppFeature.CAN_SCANNER in this,
        hasOrdersRequest = UserAppFeature.ORDERS_REQUEST in this,
        hasProductSearch = UserAppFeature.PRODUCT_SEARCH in this
    )

    private fun SpecialistAppVersion.toMainAppVersion(): MainAppVersion = MainAppVersion(
        name = name,
        code = code
    )

    private fun UserCommercial?.toMainUserCommercial(): MainUserCommercial =
        MainUserCommercial(warehouse1CUrl = this?.warehouse1CUrl)

    private companion object {
        val CHAT_AVAILABILITY_POLL_INTERVAL = 10.seconds
        val CHAT_UNREAD_POLL_INTERVAL = 60.seconds
    }
}
