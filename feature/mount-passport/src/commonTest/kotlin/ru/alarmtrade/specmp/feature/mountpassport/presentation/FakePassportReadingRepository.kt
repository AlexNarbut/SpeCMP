package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportReadingRepository

internal class FakePassportReadingRepository : PassportReadingRepository {
    val states = MutableSharedFlow<Response<PassportReadState>>()
    val reads = mutableListOf<Pair<PassportReadSource, PassportInstaller>>()
    val commands = mutableListOf<PassportReadCommand>()

    var rereadResult: Response<MountPassport> = Response.Success(testPassport())
    var rereadDelayMs = 0L
    val rereads = mutableListOf<PassportReadStage>()

    var lookup: Response<PassportUnitLookup> = Response.Success(PassportUnitLookup.NotFound)
    val lookups = mutableListOf<String>()
    var models: Response<List<String>> = Response.Success(listOf("Pandora DX-4GS", "Pandora DX-91"))
    var modelCalls = 0
    var created: Response<MountPassport> = Response.Success(testPassport(manual = true))
    val creations = mutableListOf<Triple<String, String, PassportInstaller>>()

    override fun read(source: PassportReadSource, installer: PassportInstaller): Flow<Response<PassportReadState>> {
        reads += source to installer
        return states
    }

    override suspend fun send(command: PassportReadCommand): Response<Unit> {
        commands += command
        return Response.Success(Unit)
    }

    override suspend fun reread(id: String, stage: PassportReadStage): Response<MountPassport> {
        rereads += stage
        delay(rereadDelayMs)
        return rereadResult
    }

    override suspend fun findUnit(systemId: String): Response<PassportUnitLookup> {
        lookups += systemId
        return lookup
    }

    override suspend fun unitModels(): Response<List<String>> {
        modelCalls++
        return models
    }

    override suspend fun createWithoutUnit(
        systemName: String,
        systemId: String,
        installer: PassportInstaller,
    ): Response<MountPassport> {
        creations += Triple(systemName, systemId, installer)
        return created
    }
}
