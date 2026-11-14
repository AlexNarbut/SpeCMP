package ru.alarmtrade.specmp.feature.appupdate.domain.service

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateProcess

/** Проверка, загрузка и установка обновления на всё время работы приложения: загрузка идёт и вне экрана. */
interface AppUpdateService {
    // Состояние процесса в памяти: ошибки уже свёрнуты в стадии.
    @Suppress("konsist.service-contract")
    val process: StateFlow<AppUpdateProcess>

    /** Новая проверка; во время загрузки и установки ничего не делает. */
    fun check(withChannelVersions: Boolean)

    /** Проверка, если её ещё не было. */
    fun ensureChecked(withChannelVersions: Boolean)

    fun selectChannel(id: Int)

    /** [anyFile] — без проверки, что установщик подходит этой ОС: файл только сохраняют, не ставят. */
    fun download(anyFile: Boolean)

    fun cancelDownload()

    fun install()

    /** Копия установщика в «Загрузки»; не скачан — сначала загрузка (любой файл: его только сохраняют). */
    suspend fun saveCopy(): Response<Unit>
}
