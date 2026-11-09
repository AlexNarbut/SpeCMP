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
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository

internal class SpecialistMainRepository(
    private val appUpdatesInteractor: AppUpdatesInteractor,
    private val userInteractor: UserInteractor,
    private val accountRepository: AccountRepository,
    private val chatInteractor: ChatInteractor,
    private val appVersionProvider: AppVersionProvider,
) : MainRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    // Ожидаемые ошибки SDK не логируются здесь: это Response.Error, их показывает VM (docs/patterns.md, «Логирование»).
    override suspend fun currentUserProfile(): Response<MainUserProfile> =
        accountRepository.currentProfile().map { MainUserProfile(name = it.name, email = it.email) }

    override suspend fun currentSettingsVersion(): Response<Long?> =
        when (val response = appUpdatesInteractor.getCurrentSettingsVersionUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(response.value.long)

            is SpecialistResponse.Error ->
                // SDK не даёт типизированной ошибки: пустая БД (свежая установка) приходит как
                // Exception(NO_SETTINGS_VERSION) — валидное «версии ещё нет», а не сбой. Сменит SDK текст —
                // свежая установка станет ошибкой синхронизации: проверять при обновлении SDK.
                if (response.exception.message == NO_SETTINGS_VERSION) {
                    logger.d { "Settings version not stored yet (fresh install)" }
                    Response.Success(null)
                } else {
                    response.toResponse()
                }
        }

    override fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>> = flow {
        // Единственное место с безопасной обёрткой: SDK-Flow может бросить
        // мимо Response-контракта, safeFlowRun конвертирует это в Response.Error.
        emitAll(
            safeFlowRun {
                appUpdatesInteractor.synchronizeSettingsVersionUseCase.execute()
                    .map { response -> response.toDatabaseSyncStatusResponse() }
            },
        )
    }

    override suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult> {
        val request = AppUpdateRequest(currentVersion = appVersionProvider.current().code)
        return appUpdatesInteractor.checkForAppUpdatesUseCase.execute(request).toResponse().mapIfSuccess { value ->
            when (value) {
                is AppUpdatesResponse.UpdateAvailable -> value.updateVersionInformation.toUpdateCheckResult()
                is AppUpdatesResponse.NoUpdates -> Response.Success(MainAppUpdateCheckResult.UpToDate)
            }
        }
    }

    override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> =
        userInteractor.getUserAppFeaturesUseCase.execute().toResponse().map { it.toMainUserAppFeatures() }

    override suspend fun currentUserCommercial(): Response<MainUserCommercial> =
        userInteractor.getUserCommercialUseCase.execute().toResponse().map { it.toMainUserCommercial() }

    // Specialist SDK не отдаёт Flow по чату, поэтому наблюдение эмулируется
    // периодическим опросом one-shot use case'ов (как observeIsLoggedIn в feature/auth-state).
    override fun observeChatAvailability(): Flow<Response<Boolean>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(chatAvailability())
            delay(CHAT_AVAILABILITY_POLL_INTERVAL)
        }
    }

    private suspend fun chatAvailability(): Response<Boolean> =
        chatInteractor.isAvailableUseCase.execute().toResponse().map { it.isAvailable() }

    private fun SpecialistResponse<SettingsUpdatingStatus>.toDatabaseSyncStatusResponse():
        Response<MainDatabaseSyncStatus> =
        toResponse().map { it.toMainDatabaseSyncStatus() }

    private fun SettingsUpdatingStatus.toMainDatabaseSyncStatus(): MainDatabaseSyncStatus = when (this) {
        is SettingsUpdatingStatus.CheckLocalVersion,
        is SettingsUpdatingStatus.CheckNetworkVersion,
        is SettingsUpdatingStatus.CheckVersionCompatibility,
        -> MainDatabaseSyncStatus.InProgress.CheckingVersion

        is SettingsUpdatingStatus.DownloadingNewVersion -> MainDatabaseSyncStatus.InProgress.Downloading

        is SettingsUpdatingStatus.UpdateFromDownloadedFileStarted,
        is SettingsUpdatingStatus.UpdateFromLocalFileStarted,
        -> MainDatabaseSyncStatus.InProgress.Applying

        is SettingsUpdatingStatus.FinishedSuccess ->
            MainDatabaseSyncStatus.Finished.Success(newVersion.toLong())

        is SettingsUpdatingStatus.FinishedNoUpdates -> MainDatabaseSyncStatus.Finished.NoUpdates

        is SettingsUpdatingStatus.FinishedVersionNotCompatible ->
            MainDatabaseSyncStatus.Finished.NotCompatible(minRequiredAppVersion = minRequiredAppVersion)
    }

    private fun UpdateVersionInformation.toUpdateCheckResult(): Response<MainAppUpdateCheckResult> {
        val versionName = version.trim()

        return if (versionName.isBlank() || versionInt <= appVersionProvider.current().code) {
            Response.Success(MainAppUpdateCheckResult.UpToDate)
        } else {
            Response.Success(MainAppUpdateCheckResult.Available(versionName = versionName))
        }
    }

    private fun Collection<UserAppFeature>.toMainUserAppFeatures(): MainUserAppFeatures = MainUserAppFeatures(
        hasCanScanner = UserAppFeature.CAN_SCANNER in this,
        hasOrdersRequest = UserAppFeature.ORDERS_REQUEST in this,
        hasProductSearch = UserAppFeature.PRODUCT_SEARCH in this,
    )

    private fun UserCommercial?.toMainUserCommercial(): MainUserCommercial =
        MainUserCommercial(warehouse1CUrl = this?.warehouse1CUrl)

    private companion object {
        val CHAT_AVAILABILITY_POLL_INTERVAL = 10.seconds

        /** Текст ошибки SDK «версии базы ещё нет» (свежая установка) — типа у неё нет. */
        const val NO_SETTINGS_VERSION = "settingsVersion not found"
    }
}
