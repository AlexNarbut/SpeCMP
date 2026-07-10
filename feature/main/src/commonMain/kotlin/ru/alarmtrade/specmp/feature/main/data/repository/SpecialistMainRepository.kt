package ru.alarmtrade.specmp.feature.main.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.lastOrNull
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersion
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersionProvider
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppVersion
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesResponse
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.appUpdates.UpdateVersionInformation
import ru.alarmtrade.specialist.domain.chat.ChatAvailability
import ru.alarmtrade.specialist.domain.user.User
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.appUpdates.requests.AppUpdateRequest
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.user.UserInteractor

class SpecialistMainRepository(
    private val appUpdatesInteractor: AppUpdatesInteractor,
    private val userInteractor: UserInteractor,
    private val chatInteractor: ChatInteractor,
    private val appVersionProvider: SpecialistAppVersionProvider,
) : MainRepository {

    private val logger = Logger.withTag("SpecialistMainRepository")

    override val appVersion: MainAppVersion
        get() = appVersionProvider.current().toMainAppVersion()

    override suspend fun currentUserProfile(): Response<MainUserProfile> {
        return runMainRequest {
            when (val response = userInteractor.getUserUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "Current user profile fetched" }
                    Response.Success(response.value.toMainUserProfile())
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch current user profile" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun currentSettingsVersion(): Response<Long?> {
        return runMainRequest {
            when (val response = appUpdatesInteractor.getCurrentSettingsVersionUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "Current settings version fetched: ${response.value.long}" }
                    Response.Success(response.value.long)
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch current settings version" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun synchronizeSettingsVersion(): Response<Long?> {
        return runMainRequest {
            when (val response = appUpdatesInteractor.synchronizeSettingsVersionUseCase.execute().lastOrNull()) {
                null -> {
                    logger.d { "Synchronize settings version returned no value, falling back to current" }
                    currentSettingsVersion()
                }
                is SpecialistResponse.Success -> {
                    logger.d { "Settings version synchronized" }
                    response.value.toDatabaseVersionResponse()
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to synchronize settings version" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult> {
        return runMainRequest {
            val request = AppUpdateRequest(currentVersion = appVersion.code)
            when (val response = appUpdatesInteractor.checkForAppUpdatesUseCase.execute(request)) {
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
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures> {
        return runMainRequest {
            when (val response = userInteractor.getUserAppFeaturesUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "User app features fetched: ${response.value}" }
                    Response.Success(MainUserAppFeatures(response.value.toSet()))
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch user app features" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun currentUserCommercial(): Response<MainUserCommercial> {
        return runMainRequest {
            when (val response = userInteractor.getUserCommercialUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "User commercial fetched" }
                    Response.Success(response.value.toMainUserCommercial())
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch user commercial" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun chatAvailability(): Response<Boolean> {
        return runMainRequest {
            when (val response = chatInteractor.isAvailableUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "Chat availability fetched: ${response.value.isAvailable()}" }
                    Response.Success(response.value.isAvailable())
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch chat availability" }
                    response.toProjectError()
                }
            }
        }
    }

    override suspend fun unreadMessagesCount(): Response<Int> {
        return runMainRequest {
            when (val response = chatInteractor.getUnreadMessagesCountUseCase.execute()) {
                is SpecialistResponse.Success -> {
                    logger.d { "Unread messages count fetched: ${response.value.int}" }
                    Response.Success(response.value.int)
                }
                is SpecialistResponse.Error -> {
                    logger.e(response.exception) { "Failed to fetch unread messages count" }
                    response.toProjectError()
                }
            }
        }
    }

    private suspend fun SettingsUpdatingStatus.toDatabaseVersionResponse(): Response<Long?> {
        return when (this) {
            is SettingsUpdatingStatus.FinishedSuccess -> {
                logger.d { "Settings update finished successfully, new version: $newVersion" }
                Response.Success(newVersion.toLong())
            }
            else -> {
                logger.d { "Settings update status: $this, falling back to current version" }
                currentSettingsVersion()
            }
        }
    }

    private suspend fun <T> runMainRequest(
        block: suspend () -> Response<T>,
    ): Response<T> {
        return try {
            block()
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            logger.e(e) { "Unexpected error during main request" }
            Response.Error(e)
        }
    }

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message,
        )
    }

    private fun UpdateVersionInformation.toUpdateCheckResult(): Response<MainAppUpdateCheckResult> {
        val versionName = version.trim()

        return if (versionName.isBlank() || versionInt <= appVersion.code) {
            Response.Success(MainAppUpdateCheckResult.UpToDate)
        } else {
            Response.Success(
                MainAppUpdateCheckResult.Available(
                    versionName = versionName,
                    versionCode = versionInt,
                ),
            )
        }
    }

    private fun SpecialistAppVersion.toMainAppVersion(): MainAppVersion {
        return MainAppVersion(
            name = name,
            code = code,
        )
    }

    private fun User.toMainUserProfile(): MainUserProfile {
        return MainUserProfile(
            name = userContactInformation.fullName,
            email = login,
        )
    }

    private fun UserCommercial?.toMainUserCommercial(): MainUserCommercial {
        return MainUserCommercial(warehouse1CUrl = this?.warehouse1CUrl)
    }
}
