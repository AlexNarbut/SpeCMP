package ru.alarmtrade.specmp.feature.fastmount.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess

/**
 * Мастер быстрого монтажа над `FastMountInteractor` SDK. Состояние мастера держит SDK (сессия в памяти), репозиторий
 * без состояния. Предметные ошибки — `FastMountException` в `Response.Error`.
 */
interface FastMountRepository {
    /** Класс блока по основной подсистеме модели из базы устройств. */
    suspend fun deviceClass(systemName: String): Response<MountDeviceClass>

    /** Доступ к блоку на сервере (как при входе в расширенный монтаж). */
    suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<UnitAccess>

    /** Версия последней релизной прошивки основной программы модели; `null` — модуля нет в базе. */
    suspend fun latestFirmware(systemName: String): Response<String?>

    suspend fun configuration(id: Int): Response<FastMountConfiguration>

    /** Файл конфигурации (карта монтажа) целиком в память. */
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
