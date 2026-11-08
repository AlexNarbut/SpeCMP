package ru.alarmtrade.specmp.feature.device.firmware.domain.session

import co.touchlab.kermit.Logger
import kotlin.time.Duration
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

/** Исход команды входа в загрузчик. */
sealed interface BootEntryResult {
    /** Блок принял команду и перезагружается в загрузчик — дальше экран загрузчика показывает хост. */
    data object Entered : BootEntryResult

    /** Блок ещё не принимает PIN (блокировка после неверного) — команда не отправлялась. */
    data object PinLocked : BootEntryResult

    /** PIN неверный; блокировка ввода запущена ([ServicePinCooldown]). */
    data object WrongPin : BootEntryResult

    /** Команда не прошла: нет связи, блок не ответил. */
    data object Failed : BootEntryResult
}

/**
 * Вход в загрузчик для обновления модуля — общий для экрана проверки прошивок и экрана «Обновить из файла».
 *
 * На BLE команда идёт через PIN программирования: без [ServicePin] SDK сам пробует сохранённый или `1111`; пока блок
 * не принимает ввод ([ServicePinCooldown]), в него ничего не шлём — иначе он продлит блокировку. Ожидаемая перезагрузка
 * ставится до команды (блок может перезагрузиться раньше ответа) и снимается при любом исходе, кроме принятой команды;
 * [FirmwareBootRequest] переживает перезагрузку для экрана обновления. Состояния нет — экземпляр создаёт ViewModel.
 */
class FirmwareBootEntry(
    private val deviceSession: DeviceSession,
    private val bootRequester: FirmwareBootRequester,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) {
    private val logger = Logger.withTag("FirmwareBootEntry")

    /** PIN программирования нужен только на BLE: на USB SDK пропускает режим программирования. */
    fun isPinLocked(systemId: Long): Boolean = deviceSession.transport.value == ConnectionTransport.BLE &&
        pinCooldown.remainingNow(systemId) > Duration.ZERO

    suspend fun enter(
        request: FirmwareBootRequest,
        target: BootModeTarget,
        systemId: Long,
        pin: ServicePin?,
    ): BootEntryResult {
        if (isPinLocked(systemId)) return BootEntryResult.PinLocked
        bootRequester.request(request)
        deviceSession.expectReboot(DeviceReboot.ToBoot)

        val saved = if (pin != null) withContext(dispatchers.io) { deviceSession.setServicePin(pin) } else null
        val result = when (saved) {
            is Response.Error -> saved
            else -> withContext(dispatchers.io) { deviceSession.setBootMode(target) }
        }
        return when (result) {
            is Response.Error -> {
                logger.d { "Enter boot mode failed: ${result.exception::class.simpleName}" }
                abandon()
                BootEntryResult.Failed
            }

            is Response.Success -> when (result.value) {
                BootModeResult.Entered -> BootEntryResult.Entered

                BootModeResult.WrongPin -> {
                    abandon()
                    pinCooldown.start(systemId)
                    BootEntryResult.WrongPin
                }
            }
        }
    }

    // Запрос и ожидание перезагрузки больше не нужны; ожидание снимаем, только если блок остался на связи.
    private fun abandon() {
        bootRequester.take()
        if (deviceSession.state.value is DeviceSessionState.Ready) deviceSession.cancelReboot()
    }
}
