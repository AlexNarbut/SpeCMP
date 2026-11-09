package ru.alarmtrade.specmp.feature.fastmount.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.fastMount.StateTitle
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestState
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AddMountFeedbackRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AnswerCurrentQuestionRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.StartFastMountRequest
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specmp.feature.fastmount.data.datasource.FastMountDataSource

internal class FakeFastMountDataSource : FastMountDataSource {
    var verification: SpecialistResponse<VerifyDeviceState>? = null
    var information: SpecialistResponse<DeviceInformation>? = null
    var firmware: SpecialistResponse<SubsystemFirmware>? = null
    var file: SpecialistResponse<ByteArray>? = null
    var configuration: SpecialistResponse<CarConfiguration>? = null
    var command: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)
    var states: Flow<SpecialistResponse<StateTitle>>? = emptyFlow()
    var questionStates: Flow<SpecialistResponse<FastMountQuestionTestState>>? = emptyFlow()

    val calls = mutableListOf<Any>()

    override suspend fun verify(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState>? =
        verification.also { calls += request }

    override suspend fun deviceInformation(
        request: GetDeviceInformationRequest,
    ): SpecialistResponse<DeviceInformation>? = information.also { calls += "information ${request.name}" }

    override suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>? =
        firmware.also { calls += request }

    override suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>? =
        file.also { calls += request }

    override suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>? =
        configuration.also { calls += request }

    override suspend fun start(request: StartFastMountRequest): SpecialistResponse<Unit>? =
        command.also { calls += "start" }

    override fun mountStates(): Flow<SpecialistResponse<StateTitle>>? = states

    override suspend fun next(): SpecialistResponse<Unit>? = command.also { calls += "next" }

    override suspend fun previous(): SpecialistResponse<Unit>? = command.also { calls += "previous" }

    override fun questions(): Flow<SpecialistResponse<FastMountQuestionTestState>>? = questionStates

    override suspend fun answer(request: AnswerCurrentQuestionRequest): SpecialistResponse<Unit>? =
        command.also { calls += request }

    override suspend fun skip(): SpecialistResponse<Unit>? = command.also { calls += "skip" }

    override suspend fun feedback(request: AddMountFeedbackRequest): SpecialistResponse<Unit>? =
        command.also { calls += request }
}
