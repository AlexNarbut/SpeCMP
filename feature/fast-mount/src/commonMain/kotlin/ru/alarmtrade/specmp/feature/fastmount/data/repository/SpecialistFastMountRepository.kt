package ru.alarmtrade.specmp.feature.fastmount.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.alarmtrade.common.core.response.Response as SdkResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmwareType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AddMountFeedbackRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AnswerCurrentQuestionRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.StartFastMountRequest
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.fastmount.data.datasource.FastMountDataSource
import ru.alarmtrade.specmp.feature.fastmount.data.mapper.toDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.fastmount.data.mapper.toFastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.fastmount.data.mapper.toStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountException
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository

internal class SpecialistFastMountRepository(private val dataSource: FastMountDataSource) : FastMountRepository {

    override suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<UnitAccess> {
        val request =
            VerifyDeviceRequest(systemId = systemId, systemName = systemName, firmwareVersion = firmwareVersion)
        return dataSource.verify(request).toResponseOrUnavailable().map { UnitAccess(it.hasAccess, it.message) }
    }

    override suspend fun latestFirmware(systemName: String, canCode: String?, canVersion: Int?): Response<String?> {
        val moduleId = when (
            val response = dataSource.deviceInformation(GetDeviceInformationRequest(systemName))
                .toResponseOrUnavailable()
        ) {
            is Response.Success -> response.value.subsystems.firstOrNull { it.type == SubsystemType.ALARM_MAIN }?.id
            is Response.Error -> return response
        } ?: return Response.Success(null)
        return dataSource
            .latestFirmware(
                SubsystemFirmwareRequest(
                    subsystemId = moduleId,
                    type = SubsystemFirmwareType.RELEASE,
                    canCode = canCode,
                    canCodeVersion = canVersion,
                ),
            )
            .toResponseOrUnavailable()
            .map { it.version }
    }

    override suspend fun download(url: String): Response<ByteArray> =
        dataSource.download(DownloadFileRequest(url)).toResponseOrUnavailable()

    override suspend fun deviceClass(systemName: String): Response<MountDeviceClass> =
        dataSource.deviceInformation(GetDeviceInformationRequest(systemName)).toResponseOrUnavailable()
            .map { information -> information.subsystems.map { it.type }.toDeviceClass() }

    override suspend fun configuration(id: Int): Response<FastMountConfiguration> =
        dataSource.configuration(ConfigurationRequest(id)).toResponseOrUnavailable().map { it.toDomain() }

    override suspend fun start(configurationId: Int): Response<Unit> {
        val configuration = when (
            val response = dataSource.configuration(ConfigurationRequest(configurationId)).toResponseOrUnavailable()
        ) {
            is Response.Success -> response.value
            is Response.Error -> return response
        }
        return dataSource.start(StartFastMountRequest(carConfiguration = configuration, isDebug = false))
            .toFastMountResponse()
    }

    override fun steps(): Flow<Response<FastMountStep>> = flow {
        safeFlowRun {
            dataSource.mountStates()?.map { it.toFastMountResponse() } ?: flowOf(interactorUnavailable())
        }
            .mapNotNull { response ->
                when (response) {
                    is Response.Success -> response.value.toStep()?.let { Response.Success(it) }
                    is Response.Error -> response
                }
            }
            .collect { emit(it) }
    }

    override suspend fun next(): Response<Unit> = dataSource.next().toFastMountResponse()

    override suspend fun previous(): Response<Unit> = dataSource.previous().toFastMountResponse()

    override fun questions(): Flow<Response<QuestionsState>> = flow {
        safeFlowRun {
            dataSource.questions()?.map { response -> response.toFastMountResponse().map { it.toDomain() } }
                ?: flowOf(interactorUnavailable())
        }.collect { emit(it) }
    }

    override suspend fun answer(answer: QuestionAnswer): Response<Unit> =
        dataSource.answer(AnswerCurrentQuestionRequest(answer.toSdk())).toFastMountResponse()

    override suspend fun skip(): Response<Unit> = dataSource.skip().toFastMountResponse()

    override suspend fun feedback(text: String, rating: Int): Response<Unit> =
        dataSource.feedback(AddMountFeedbackRequest(feedback = text, rating = rating)).toFastMountResponse()
}

/** Ответ SDK → [Response]; предметные ошибки — [FastMountException], `null` — интерактор недоступен. */
private fun <T> SdkResponse<T>?.toFastMountResponse(): Response<T> = when (val response = toResponseOrUnavailable()) {
    is Response.Success -> response

    is Response.Error -> response.exception.toFastMountFailure()
        ?.let { Response.Error(FastMountException(it, response.exception), response.message) }
        ?: response
}
