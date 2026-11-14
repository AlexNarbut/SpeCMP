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
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onSuccess
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.main.data.cache.MainUserCache
import ru.alarmtrade.specmp.feature.main.data.datasource.MainDataSource
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository

internal class SpecialistMainRepository(
    private val dataSource: MainDataSource,
    private val accountRepository: AccountRepository,
    private val userCache: MainUserCache,
    private val networkMonitor: NetworkMonitor?,
) : MainRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun currentUserProfile(): Response<MainUserProfile> = accountRepository.currentProfile()
        .map { MainUserProfile(name = it.name, email = it.email) }
        .onSuccess { userCache.saveProfile(it) }

    override suspend fun cachedUserProfile(): Response<MainUserProfile?> = safeRun { userCache.profile() }

    override suspend fun cachedUserAppFeatures(): Response<MainUserAppFeatures?> = safeRun { userCache.appFeatures() }

    override suspend fun cachedUserCommercial(): Response<MainUserCommercial?> = safeRun { userCache.commercial() }

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
        // SDK при ошибке сети отдаёт FinishedNoUpdates («база обновлена»), поэтому офлайн отсекаем до запуска.
        // На desktop монитора сети нет — там решает SDK.
        if (networkMonitor?.isAvailable?.value == false) {
            emit(SpecialistResponse.Error.Network(NetworkIsNotAvailableException()).toDatabaseSyncStatusResponse())
            return@flow
        }
        // SDK-Flow может бросить мимо Response-контракта — safeFlowRun переводит это в Response.Error.
        emitAll(
            safeFlowRun {
                dataSource.synchronizeSettingsVersion()
                    .map { response -> response.toDatabaseSyncStatusResponse() }
            },
        )
    }

    override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> =
        dataSource.userAppFeatures().toResponse()
            .map { it.toMainUserAppFeatures() }
            .onSuccess { userCache.saveAppFeatures(it) }

    override suspend fun currentUserCommercial(): Response<MainUserCommercial> =
        dataSource.userCommercial().toResponse()
            .map { it.toMainUserCommercial() }
            .onSuccess { userCache.saveCommercial(it) }

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

    private fun Collection<UserAppFeature>.toMainUserAppFeatures(): MainUserAppFeatures = MainUserAppFeatures(
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
