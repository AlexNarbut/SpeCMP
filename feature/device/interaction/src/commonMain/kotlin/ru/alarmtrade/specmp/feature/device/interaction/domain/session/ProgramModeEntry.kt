package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlin.time.Duration
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/** Исход входа в режим программирования. */
enum class ProgramModeEntryResult {
    ENTERED,

    /** Блок ещё не принимает PIN (блокировка после неверного) — команда не отправлялась. */
    PIN_LOCKED,

    /** PIN неверный; блокировка ввода запущена ([ServicePinCooldown]). */
    WRONG_PIN,

    /** Команда не прошла: нет связи, блок не ответил. */
    FAILED,
}

/**
 * Вход в режим программирования (обучение обходу, проверка состояния) — по образцу входа в загрузчик у прошивки.
 * BLE: без [ServicePin] SDK сам пробует сохранённый или `1111`; пока блок не принимает ввод ([ServicePinCooldown]),
 * в него ничего не шлём — иначе он продлит блокировку. Состояния нет.
 */
class ProgramModeEntry(
    private val deviceSession: DeviceSession,
    private val programMode: ProgramModeRepository,
    private val pinCooldown: ServicePinCooldown,
) {
    /** PIN программирования нужен только на BLE: на USB SDK пропускает режим программирования. */
    fun isPinLocked(systemId: Long): Boolean = deviceSession.transport.value == ConnectionTransport.BLE &&
        pinCooldown.remainingNow(systemId) > Duration.ZERO

    /** Сохранить введённый PIN ([pin]) и войти в режим. Вызывать на IO. */
    suspend fun enter(systemId: Long, pin: ServicePin?): ProgramModeEntryResult {
        if (isPinLocked(systemId)) return ProgramModeEntryResult.PIN_LOCKED
        val saved = if (pin != null) deviceSession.setServicePin(pin) else null
        if (saved is Response.Error) return ProgramModeEntryResult.FAILED
        return when (val result = programMode.enter()) {
            is Response.Error -> ProgramModeEntryResult.FAILED

            is Response.Success -> when (result.value) {
                ProgramModeResult.ENTERED -> ProgramModeEntryResult.ENTERED

                ProgramModeResult.WRONG_PIN -> {
                    pinCooldown.start(systemId)
                    ProgramModeEntryResult.WRONG_PIN
                }
            }
        }
    }
}
