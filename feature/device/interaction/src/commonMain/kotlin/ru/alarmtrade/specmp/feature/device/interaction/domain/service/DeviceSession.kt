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
 * Единственный держатель состояния подключённого устройства (app-scoped). На каждом `READY` определяет режим
 * блока (с повтором): рабочий — [DeviceSessionState.Ready], загрузчик BLE-модуля — [DeviceSessionState.BluetoothBoot],
 * загрузчик основной программы — [DeviceSessionState.FirmwareBoot]; обрыв — [DeviceSessionState.Disconnected].
 * Вход в загрузчик и выход из него проходят через переподключение блока — режим определяется заново.
 * Ожидаемая перезагрузка блока ([expectReboot]) — отдельное состояние [reboot]: по нему экраны отличают
 * намеренный обрыв связи (вход в загрузчик, возврат после прошивки) от потери связи.
 * COM-порт ([ConnectionTransport.SERIAL]) обслуживается как USB; перезагрузка блока связь по нему не рвёт,
 * поэтому после прошивки режим перечитывают явно ([refreshMode]).
 * Фоновую работу (переподключение, закрытие) ведёт в собственном скоупе — она переживает экраны.
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

    /** Транспорт текущей сессии (для текстов «связь потеряна»); `null` — сессии нет. */
    // Снимок транспорта текущей сессии — не результат операции.
    @Suppress("konsist.service-contract")
    val transport: StateFlow<ConnectionTransport?>

    /** Запуск фоновой части сессии (публикация [reboot]) — один раз из стартера приложения; повтор ничего не делает. */
    fun start()

    /** Сессия создана поиском устройства — запомнить, по какому транспорту. */
    fun attach(transport: ConnectionTransport)

    /**
     * Закрыть сессию с устройством. Не ждёт: транспорт снимается сразу (переподключений больше не будет),
     * само закрытие идёт в скоупе сессии и переживает экран. Кто открывает новую сессию — зовёт [closeAndAwait],
     * иначе отложенное закрытие оборвёт новую сессию.
     */
    fun close()

    /** То же, что [close], и дождаться конца закрытия (в том числе уже идущего). */
    suspend fun closeAndAwait(): Response<Unit>

    /**
     * Перечитать режим блока на текущей связи, не показывая «Подключение…». Нужен COM-порту: после прошивки блок
     * перезагрузился (загрузчик → рабочий режим), а порт не отключался — сам режим не перечитается.
     */
    fun refreshMode()

    /** IMEI GSM-модема подключённого блока. */
    suspend fun readImei(): Response<String>

    /**
     * Переподключиться сейчас (кнопка «Переподключиться» при обрыве): одна попытка, сериализованная с циклом
     * переподключения BLE и закрытием, с тем же тайм-аутом. `null` — попытка не уложилась в тайм-аут или сессия
     * закрыта.
     */
    suspend fun reconnectNow(): Response<Unit>?

    /** Bluetooth-модуль блока (мануфактура и режим) — BLE; в рабочем режиме нужен для версии BLE-прошивки. */
    suspend fun readBluetoothModule(): Response<BluetoothModule>

    /**
     * Выйти из загрузчика командой (перезагрузка [target] в рабочий режим). Одна команда, без повтора; кто вызывает —
     * сам ставит [expectReboot] ([DeviceReboot.ToMain]) до команды: блок может перезагрузиться раньше ответа.
     */
    suspend fun exitBootMode(target: BootModeTarget): Response<Unit>

    /** Сохранить PIN программирования для текущего блока (после [BootModeResult.WrongPin]). */
    suspend fun setServicePin(pin: ServicePin): Response<Unit>

    /**
     * Перевести [target] в загрузчик. Ответ возвращается сразу после первой команды. Если блок её принял, через паузу
     * в скоупе сессии уходит повтор: при потерянном обрыве связи он форсирует переподключение (как в старом
     * приложении); после успешного перехода повтор не дойдёт до блока. Неверный PIN и ошибки не повторяем: каждая
     * лишняя попытка с неверным PIN продлевает блокировку ввода на блоке.
     */
    suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult>

    /**
     * Ждём перезагрузку блока: до команды ([DeviceReboot.ToBoot]) или на стадии применения прошивки
     * ([DeviceReboot.ToMain]).
     * - [DeviceReboot.ToBoot] снимается, когда блок вернулся в загрузчике, по тайм-ауту, при [cancelReboot], [attach] и [close].
     * - [DeviceReboot.Restart] снимается, когда блок снова в рабочем режиме, по тайм-ауту, при [cancelReboot],
     *   [attach] и [close].
     * - [DeviceReboot.ToMain] держится, пока блок не вернулся в рабочий режим **и** пользователь не подтвердил
     *   ([acknowledgeReboot]); тайм-аут действует только до возврата блока.
     */
    fun expectReboot(kind: DeviceReboot)

    /**
     * Началась передача прошивки в текущем режиме загрузчика: на переподключениях BLE до [endFirmwareTransfer] режим
     * блока не перечитывается (SDK продолжает передачу сам). Блок не в загрузчике — ничего не меняет.
     */
    fun beginFirmwareTransfer()

    fun endFirmwareTransfer()

    /** Команда не удалась, ожидаемой перезагрузки не будет. */
    fun cancelReboot()

    /** Пользователь увидел результат прошивки: [DeviceReboot.ToMain] снимается, как только блок в рабочем режиме. */
    fun acknowledgeReboot()
}
