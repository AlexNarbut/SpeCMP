package ru.alarmtrade.specmp.feature.login.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveAppDeviceInformationRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.domain.errors.UserIsNotConfirmed
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest

class SpecialistLoginRepository(
    private val specialistApp: SpecialistApp,
    private val dispatchers: AppDispatchers,
) : LoginRepository {

    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> {
        return runAuthRequest(defaultError = "Не удалось войти") {
            when (val response = saveDeviceInformation()) {
                is Response.Success -> Unit
                is Response.Error -> return@runAuthRequest response
            }
            when (val response = specialistApp.authInteractor.loginUseCase.execute(credentials.toLoginRequest())) {
                is SpecialistResponse.Success -> Response.Success(LoginResult.Authorized)
                is SpecialistResponse.Error -> {
                    if (response.exception is UserIsNotConfirmed) {
                        Response.Success(LoginResult.ActivationRequired)
                    } else {
                        response.toProjectError(defaultMessage = "Не удалось войти")
                    }
                }
            }
        }
    }

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> {
        return runAuthRequest(defaultError = "Не удалось отправить письмо подтверждения") {
            when (val response = saveDeviceInformation()) {
                is Response.Success -> Unit
                is Response.Error -> return@runAuthRequest response
            }
            when (
                val response = specialistApp.authInteractor.resendActivationEmailUseCase.execute(
                    credentials.toResendActivationEmailRequest()
                )
            ) {
                is SpecialistResponse.Success -> Response.Success(Unit)
                is SpecialistResponse.Error -> response.toProjectError(
                    defaultMessage = "Не удалось отправить письмо подтверждения"
                )
            }
        }
    }

    override suspend fun isLoggedIn(): Response<Boolean> {
        return runAuthRequest(defaultError = "Не удалось проверить статус авторизации") {
            when (val response = specialistApp.authInteractor.checkIfUserLoggedInUseCase.execute()) {
                is SpecialistResponse.Success -> Response.Success(response.value.boolean)
                is SpecialistResponse.Error -> response.toProjectError(
                    defaultMessage = "Не удалось проверить статус авторизации"
                )
            }
        }
    }

    private suspend fun <T> runAuthRequest(
        defaultError: String,
        block: suspend () -> Response<T>,
    ): Response<T> {
        return try {
            withContext(dispatchers.io) { block() }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            Response.Error(e, e.message ?: defaultError)
        }
    }

    private fun LoginCredentials.toLoginRequest(): LoginRequest {
        return LoginRequest(
            username = normalizedLogin,
            password = password,
        )
    }

    private fun LoginCredentials.toResendActivationEmailRequest(): ResendActivationEmailRequest {
        return ResendActivationEmailRequest(
            email = normalizedLogin,
            password = password,
        )
    }

    private suspend fun saveDeviceInformation(): Response<Unit> {
        return when (
            val response = specialistApp.appDeviceInformationInteractor.saveAppDeviceInformationUseCase.execute(
                SaveAppDeviceInformationRequest(createAppDeviceInformation())
            )
        ) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError(defaultMessage = "Не удалось сохранить информацию об устройстве")
        }
    }

    private fun <T> SpecialistResponse.Error.toProjectError(defaultMessage: String): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message ?: defaultMessage,
        )
    }

    private fun createAppDeviceInformation(): AppDeviceInformation {
        val osName = System.getProperty("os.name").orEmpty()
        val osVersion = System.getProperty("os.version").orEmpty()
        val userName = System.getProperty("user.name").orEmpty()
        val javaVendor = System.getProperty("java.vendor").orEmpty()
        val javaVersion = System.getProperty("java.version").orEmpty()

        val deviceType = when {
            osName.contains("mac", ignoreCase = true) -> DeviceType.MACOS
            osName.contains("win", ignoreCase = true) -> DeviceType.WINDOWS
            else -> DeviceType.LINUX
        }

        val deviceId = listOf(osName, osVersion, userName)
            .joinToString(separator = ":")
            .ifBlank { "SpeCMP JVM" }

        return AppDeviceInformation(
            deviceId = deviceId,
            deviceType = deviceType,
            deviceDescription = "$osName $osVersion, JVM $javaVersion $javaVendor, SpeCMP",
        )
    }
}
