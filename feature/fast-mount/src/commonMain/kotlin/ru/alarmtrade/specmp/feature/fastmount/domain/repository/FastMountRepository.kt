package ru.alarmtrade.specmp.feature.fastmount.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess

/** Состояние мастера держит SDK (сессия в памяти), репозиторий без состояния. */
interface FastMountRepository {
    suspend fun deviceClass(systemName: String): Response<MountDeviceClass>

    suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<UnitAccess>

    /** `null` — модуля нет в базе; [canCode] и [canVersion] — последняя версия под CAN-код блока. */
    suspend fun latestFirmware(systemName: String, canCode: String? = null, canVersion: Int? = null): Response<String?>

    suspend fun configuration(id: Int): Response<FastMountConfiguration>

    suspend fun download(url: String): Response<ByteArray>

    /** Старт: конфигурация перечитывается с сервера и передаётся SDK; сессия монтажа на сервере. */
    suspend fun start(configurationId: Int): Response<Unit>

    /** Текущий шаг сессии; служебные `*_FINISHED` не приходят. */
    fun steps(): Flow<Response<FastMountStep>>

    suspend fun next(): Response<Unit>

    suspend fun previous(): Response<Unit>

    /**
     * Вопросы проверки системы. Холодный поток: автоответы SDK считаются **в подписчике** — держать ровно одного,
     * иначе ответ уйдёт дважды и вопрос пропустится.
     */
    fun questions(): Flow<Response<QuestionsState>>

    suspend fun answer(answer: QuestionAnswer): Response<Unit>

    suspend fun skip(): Response<Unit>

    /** Отзыв: текст не пустой, оценка 1..5 (правило SDK). */
    suspend fun feedback(text: String, rating: Int): Response<Unit>
}
