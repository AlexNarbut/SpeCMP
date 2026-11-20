package ru.alarmtrade.specmp.feature.mountpassport.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specialist.domain.errors.MountPassportStageNotReadException
import ru.alarmtrade.specialist.domain.errors.MountPassportUnitMismatchException
import ru.alarmtrade.specialist.interactors.mountPassport.requests.CreateMountPassportWithoutUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.FindMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.MountPassportReadCommandRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.ReadMountPassportUnitRequest
import ru.alarmtrade.specialist.interactors.mountPassport.requests.RereadMountPassportStageRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportReadingDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.mountpassport.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStageNotReadException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitMismatchException
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportReadingRepository

internal class SpecialistPassportReadingRepository(private val dataSource: PassportReadingDataSource) :
    PassportReadingRepository {

    override fun read(source: PassportReadSource, installer: PassportInstaller): Flow<Response<PassportReadState>> =
        dataSource.read(ReadMountPassportUnitRequest(source.toSdk(), installer.toSdk()))
            .map { response -> response.toResponse().map { it.toDomain() } }

    override suspend fun send(command: PassportReadCommand): Response<Unit> =
        dataSource.send(MountPassportReadCommandRequest(command.toSdk())).toResponse()

    override suspend fun reread(id: String, stage: PassportReadStage): Response<MountPassport> =
        dataSource.reread(RereadMountPassportStageRequest(id, stage.toSdk()))
            .withFiles(dataSource::files)
            .mapIfError { Response.Error(it.exception.toRereadError(), it.message) }

    override suspend fun findUnit(systemId: String): Response<PassportUnitLookup> =
        dataSource.find(FindMountPassportUnitRequest(systemId)).toResponse().map { it.toDomain() }

    override suspend fun unitModels(): Response<List<String>> = dataSource.unitModels()
        .toResponseOrUnavailable()
        .map { names -> names.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted() }

    override suspend fun createWithoutUnit(
        systemName: String,
        systemId: String,
        installer: PassportInstaller,
    ): Response<MountPassport> =
        dataSource.createWithoutUnit(CreateMountPassportWithoutUnitRequest(systemName, systemId, installer.toSdk()))
            .withFiles(dataSource::files)
}

private fun Throwable.toRereadError(): Throwable = when (this) {
    is MountPassportUnitMismatchException -> PassportUnitMismatchException(expectedSystemId, actualSystemId, this)
    is MountPassportStageNotReadException -> PassportStageNotReadException(connectionLost, this)
    else -> this
}
