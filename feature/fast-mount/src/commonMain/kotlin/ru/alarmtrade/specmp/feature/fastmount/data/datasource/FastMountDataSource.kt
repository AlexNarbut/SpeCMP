package ru.alarmtrade.specmp.feature.fastmount.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.fastMount.StateTitle
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestState
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.VerificationInteractor
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.fastMount.FastMountInteractor
import ru.alarmtrade.specialist.interactors.fastMount.requests.AddMountFeedbackRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AnswerCurrentQuestionRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.StartFastMountRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.FirmwareFilesInteractor
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface FastMountDataSource {
    suspend fun verify(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState>?
    suspend fun deviceInformation(request: GetDeviceInformationRequest): SpecialistResponse<DeviceInformation>?
    suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>?
    suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>?
    suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>?
    suspend fun start(request: StartFastMountRequest): SpecialistResponse<Unit>?
    fun mountStates(): Flow<SpecialistResponse<StateTitle>>?
    suspend fun next(): SpecialistResponse<Unit>?
    suspend fun previous(): SpecialistResponse<Unit>?
    fun questions(): Flow<SpecialistResponse<FastMountQuestionTestState>>?
    suspend fun answer(request: AnswerCurrentQuestionRequest): SpecialistResponse<Unit>?
    suspend fun skip(): SpecialistResponse<Unit>?
    suspend fun feedback(request: AddMountFeedbackRequest): SpecialistResponse<Unit>?
}

internal class SpecialistFastMountDataSource(
    private val fastMount: () -> FastMountInteractor,
    private val configurations: () -> ConfigurationInteractor,
    private val deviceInformation: () -> DeviceInformationInteractor,
    private val verification: () -> VerificationInteractor,
    private val firmwareFiles: () -> FirmwareFilesInteractor,
    private val files: () -> RemoteFilesInteractor,
) : FastMountDataSource {
    override suspend fun verify(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState>? =
        verification.resolveOrNull()?.verifyDeviceUseCase?.execute(request)

    override suspend fun deviceInformation(
        request: GetDeviceInformationRequest,
    ): SpecialistResponse<DeviceInformation>? =
        deviceInformation.resolveOrNull()?.getDeviceInformation?.execute(request)

    override suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>? =
        firmwareFiles.resolveOrNull()?.getLatestSubsystemFirmwareUseCase?.execute(request)

    override suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>? =
        files.resolveOrNull()?.downloadFileUseCase?.execute(request)

    override suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>? =
        configurations.resolveOrNull()?.getConfigurationByIdUseCase?.execute(request)

    override suspend fun start(request: StartFastMountRequest): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.startFastMountUseCase?.execute(request)

    override fun mountStates(): Flow<SpecialistResponse<StateTitle>>? = fastMount.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.getCurrentMountStateFlowUseCase.execute()) }
    }

    override suspend fun next(): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.goToNextMountStateUseCase?.execute()

    override suspend fun previous(): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.goToPreviousMountStateUseCase?.execute()

    override fun questions(): Flow<SpecialistResponse<FastMountQuestionTestState>>? =
        fastMount.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.getFastMountQuestionsFlowUseCase.execute()) }
        }

    override suspend fun answer(request: AnswerCurrentQuestionRequest): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.answerCurrentQuestionUseCase?.execute(request)

    override suspend fun skip(): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.skipCurrentQuestionUseCase?.execute()

    override suspend fun feedback(request: AddMountFeedbackRequest): SpecialistResponse<Unit>? =
        fastMount.resolveOrNull()?.addMountFeedbackUseCase?.execute(request)
}
