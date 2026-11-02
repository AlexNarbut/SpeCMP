package ru.alarmtrade.specmp.feature.fastmount.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.alarmtrade.common.core.response.Response as SdkResponse
import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.fastMount.FastMountInteractor
import ru.alarmtrade.specialist.interactors.fastMount.requests.AddMountFeedbackRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AnswerCurrentQuestionRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.StartFastMountRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountException
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository

/**
 * [FastMountRepository] над `FastMountInteractor` SDK. Интеракторы — лениво: недоступный на платформе не должен
 * ронять создание репозитория.
 */
class SpecialistFastMountRepository(
    private val fastMount: () -> FastMountInteractor,
    private val configurations: () -> ConfigurationInteractor,
    private val deviceInformation: () -> DeviceInformationInteractor
) : FastMountRepository {

    override suspend fun deviceClass(systemName: String): Response<MountDeviceClass> {
        val interactor = deviceInformation.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getDeviceInformation.execute(GetDeviceInformationRequest(systemName)).toResponse()
            .map { information -> information.subsystems.map { it.type }.toDeviceClass() }
    }

    override suspend fun configuration(id: Int): Response<FastMountConfiguration> {
        val interactor = configurations.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getConfigurationByIdUseCase.execute(ConfigurationRequest(id)).toResponse()
            .map { it.toDomain() }
    }

    override suspend fun start(configurationId: Int): Response<Unit> {
        val configs = configurations.resolveOrNull() ?: return interactorUnavailable()
        val interactor = fastMount.resolveOrNull() ?: return interactorUnavailable()
        val configuration = when (
            val response = configs.getConfigurationByIdUseCase.execute(ConfigurationRequest(configurationId))
                .toResponse()
        ) {
            is Response.Success -> response.value
            is Response.Error -> return response
        }
        return interactor.startFastMountUseCase
            .execute(StartFastMountRequest(carConfiguration = configuration, isDebug = false))
            .toFastMountResponse()
    }

    override fun steps(): Flow<Response<FastMountStep>> = flow {
        val interactor = fastMount.resolveOrNull()
        if (interactor == null) {
            emit(interactorUnavailable())
            return@flow
        }
        safeFlowRun { interactor.getCurrentMountStateFlowUseCase.execute().map { it.toFastMountResponse() } }
            .mapNotNull { response ->
                when (response) {
                    is Response.Success -> response.value.toStep()?.let { Response.Success(it) }
                    is Response.Error -> response
                }
            }
            .collect { emit(it) }
    }

    override suspend fun next(): Response<Unit> = call { goToNextMountStateUseCase.execute().toFastMountResponse() }

    override suspend fun previous(): Response<Unit> =
        call { goToPreviousMountStateUseCase.execute().toFastMountResponse() }

    override fun questions(): Flow<Response<QuestionsState>> = flow {
        val interactor = fastMount.resolveOrNull()
        if (interactor == null) {
            emit(interactorUnavailable())
            return@flow
        }
        safeFlowRun {
            interactor.getFastMountQuestionsFlowUseCase.execute()
                .map { response -> response.toFastMountResponse().map { it.toDomain() } }
        }.collect { emit(it) }
    }

    override suspend fun answer(answer: QuestionAnswer): Response<Unit> = call {
        answerCurrentQuestionUseCase.execute(AnswerCurrentQuestionRequest(answer.toSdk())).toFastMountResponse()
    }

    override suspend fun skip(): Response<Unit> = call { skipCurrentQuestionUseCase.execute().toFastMountResponse() }

    override suspend fun feedback(text: String, rating: Int): Response<Unit> = call {
        addMountFeedbackUseCase.execute(AddMountFeedbackRequest(feedback = text, rating = rating))
            .toFastMountResponse()
    }

    private suspend fun call(block: suspend FastMountInteractor.() -> Response<Unit>): Response<Unit> {
        val interactor = fastMount.resolveOrNull() ?: return interactorUnavailable()
        return interactor.block()
    }
}

/** Ответ SDK → [Response]; предметные ошибки быстрого монтажа — [FastMountException]. */
private fun <T> SdkResponse<T>.toFastMountResponse(): Response<T> = when (val response = toResponse()) {
    is Response.Success -> response

    is Response.Error -> response.exception.toFastMountFailure()
        ?.let { Response.Error(FastMountException(it, response.exception), response.message) }
        ?: response
}
