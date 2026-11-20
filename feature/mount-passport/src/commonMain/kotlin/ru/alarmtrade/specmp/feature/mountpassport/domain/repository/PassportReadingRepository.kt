package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup

/** Считывание блока в паспорт и паспорт без блока; этапы, повторы и сохранение — в SDK. */
interface PassportReadingRepository {
    /** Отмена сбора — отмена считывания без записи. */
    fun read(source: PassportReadSource, installer: PassportInstaller): Flow<Response<PassportReadState>>

    suspend fun send(command: PassportReadCommand): Response<Unit>

    /** Другой блок — `PassportUnitMismatchException`, не прочитано — `PassportStageNotReadException`. */
    suspend fun reread(id: String, stage: PassportReadStage): Response<MountPassport>

    suspend fun findUnit(systemId: String): Response<PassportUnitLookup>

    /** Модели блоков для ручного ввода. */
    suspend fun unitModels(): Response<List<String>>

    suspend fun createWithoutUnit(
        systemName: String,
        systemId: String,
        installer: PassportInstaller,
    ): Response<MountPassport>
}
