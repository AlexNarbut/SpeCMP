package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult

/** Режим программирования блока (обучение обходу, команды проверки состояния). */
interface ProgramModeRepository {
    /**
     * Войти в режим программирования. BLE: SDK пробует сохранённый PIN или `1111`; неверный —
     * [ProgramModeResult.WRONG_PIN], не ошибка. USB: PIN не нужен.
     */
    suspend fun enter(): Response<ProgramModeResult>

    /** Выйти из режима программирования (блок перезагружается). Только BLE. */
    suspend fun exit(): Response<Unit>
}
