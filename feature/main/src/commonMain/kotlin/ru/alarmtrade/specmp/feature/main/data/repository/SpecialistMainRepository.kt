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
import ru.alarmtrade.specialist.interactors.appUpdates.requests.AppUpdateRequest
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.main.data.datasource.MainDataSource
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository

internal class SpecialistMainRepository(
    private val dataSource: MainDataSource,
    private val accountRepository: AccountRepository,
    private val appVersionProvider: AppVersionProvider,
) : MainRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun currentUserProfile(): Response<MainUserProfile> =
        accountRepository.currentProfile().map { MainUserProfile(name = it.name, email = it.email) }

    override suspend fun currentSettingsVersion(): Response<Long?> =
        when (val response = dataSource.currentSettingsVersion()) {
            is SpecialistResponse.Success -> Response.Success(response.value.long)

            is SpecialistResponse.Error ->
                // SDK не даёт типа ошибки: свежая установка — Exception(NO_SETTINGS_VERSION), «версии ещё нет».
                // Сменит SDK текст — это станет ошибкой синхронизации: проверять при обновлении SDK.
                if (response.exception.message == NO_SETTINGS_VERSION) {
                    logger.d { "Settings version not stored yet (fresh install)" }
                    Response.Success(null)
                } else {
                    response.toResponse()
                }
        }

    override fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>> = flow {
        // SDK-Flow может бросить мимо Response-контракта — safeFlowRun переводит это в Response.Error.
        emitAll(
            safeFlowRun {
                dataSource.synchronizeSettingsVersion()
                    .map { response -> response.toDatabaseSyncStatusResponse() }
            },
        )
    }

    override suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult> {
        val request = AppUpdateRequest(currentVersion = appVersionProvider.current().code)
        return dataSource.checkForAppUpdates(request).toResponse().mapIfSuccess { value ->
            when (value) {
                is AppUpdatesResponse.UpdateAvailable -> value.updateVersionInformation.toUpdateCheckResult()
                is AppUpdatesResponse.NoUpdates -> Response.Success(MainAppUpdateCheckResult.UpToDate)
            }
        }
    }

    override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> =
        dataSource.userAppFeatures().toResponse().map { it.toMainUserAppFeatures() }

    override suspend fun currentUserCommercial(): Response<MainUserCommercial> =
        dataSource.userCommercial().toResponse().map { it.toMainUserCommercial() }

    // SDK не отдаёт Flow по чату — наблюдение эмулируется опросом.
    override fun observeChatAvailability(): Flow<Response<Boolean>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(chatAvailability())
            delay(CHAT_AVAILABILITY_POLL_INTERVAL)
        }
    }

    private suspend fun chatAvailability(): Response<Boolean> =
        dataSource.chatAvailability().toResponse().map { it.isAvailable() }

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

        const val NO_SETTINGS_VERSION = "settingsVersion not found"
    }
}
