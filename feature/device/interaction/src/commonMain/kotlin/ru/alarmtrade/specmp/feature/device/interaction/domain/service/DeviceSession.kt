package ru.alarmtrade.specmp.feature.device.interaction.domain.service

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin

/**
 * Ожидаемая перезагрузка ([expectReboot]) — отдельное состояние [reboot]: по нему экраны отличают намеренный обрыв
 * от потери связи. COM-порт перезагрузка не рвёт — после прошивки режим перечитывают явно ([refreshMode]).
 */
interface DeviceSession {
    /** Горячий, пока есть подписчики; после ухода всех подписчиков значение сбрасывается. */
    // Состояние сессии в памяти: ошибка связи уже свёрнута в Disconnected, ошибкой поток не бывает.
    @Suppress("konsist.service-contract")
    val state: StateFlow<DeviceSessionState>

    /** Перезагрузка блока, которую приложение вызвало и ждёт; `null` — не ждём. Обновляется после [start]. */
    // Снимок того, чего ждёт приложение, — не результат операции.
    @Suppress("konsist.service-contract")
    val reboot: StateFlow<DeviceReboot?>

    // Снимок транспорта текущей сессии — не результат операции.
    @Suppress("konsist.service-contract")
    val transport: StateFlow<ConnectionTransport?>

    /** Запуск фоновой части сессии (публикация [reboot]) — один раз из стартера приложения; повтор ничего не делает. */
    fun start()

    fun attach(transport: ConnectionTransport)

    /**
     * Не ждёт: транспорт снимается сразу, закрытие идёт в скоупе сессии. Новую сессию открывают после [closeAndAwait],
     * иначе отложенное закрытие оборвёт её.
     */
    fun close()

    /** То же, что [close], и дождаться конца закрытия (в том числе уже идущего). */
    suspend fun closeAndAwait(): Response<Unit>

    /** Нужен COM-порту: после прошивки блок перезагрузился, а порт не отключался — сам режим не перечитается. */
    fun refreshMode()

    suspend fun readImei(): Response<String>

    /** Одна попытка, сериализованная с циклом переподключения BLE и закрытием; `null` — тайм-аут или сессия закрыта. */
    suspend fun reconnectNow(): Response<Unit>?

    suspend fun readBluetoothModule(): Response<BluetoothModule>

    /**
     * Без повтора; вызывающий ставит [expectReboot] ([DeviceReboot.ToMain]) до команды — блок может перезагрузиться
     * раньше ответа.
     */
    suspend fun exitBootMode(target: BootModeTarget): Response<Unit>

    suspend fun setServicePin(pin: ServicePin): Response<Unit>

    /**
     * Если блок принял команду, через паузу уходит повтор: при потерянном обрыве связи он форсирует переподключение.
     * Неверный PIN и ошибки не повторяем — лишняя попытка с неверным PIN продлевает блокировку ввода.
     */
    suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult>

    /**
     * [DeviceReboot.ToBoot] и [DeviceReboot.Restart] снимаются по достижении режима, тайм-ауту, [cancelReboot],
     * [attach], [close]; [DeviceReboot.ToMain] — только когда блок в рабочем режиме и пользователь подтвердил
     * ([acknowledgeReboot]).
     */
    fun expectReboot(kind: DeviceReboot)

    /** До [endFirmwareTransfer] на переподключениях BLE режим блока не перечитывается: SDK продолжает передачу сам. */
    fun beginFirmwareTransfer()

    fun endFirmwareTransfer()

    fun cancelReboot()

    fun acknowledgeReboot()
}
