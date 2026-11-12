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
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

sealed interface BootEntryResult {
    /** Блок принял команду и перезагружается в загрузчик — дальше экран загрузчика показывает хост. */
    data object Entered : BootEntryResult

    /** Блок ещё не принимает PIN (блокировка после неверного) — команда не отправлялась. */
    data object PinLocked : BootEntryResult

    data object WrongPin : BootEntryResult

    data object Failed : BootEntryResult
}

/**
 * Пока блок не принимает ввод ([ServicePinCooldown]), в него ничего не шлём — иначе он продлит блокировку. Ожидаемая
 * перезагрузка ставится до команды (блок может перезагрузиться раньше ответа) и снимается при любом исходе, кроме
 * успеха.
 */
class FirmwareBootEntry(
    private val deviceSession: DeviceSession,
    private val bootRequester: FirmwareBootRequester,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
) {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

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
