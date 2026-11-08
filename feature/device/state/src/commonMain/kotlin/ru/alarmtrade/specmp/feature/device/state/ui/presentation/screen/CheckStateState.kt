package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone

/**
 * Экран «Проверка состояния».
 *
 * @property vehicle последний снимок; `null` — ждём первый ответ блока (скелетон, без нулей).
 * @property tick номер снимка: на каждом новом мигает точка «на связи», гаснут метки «Изменилось».
 * @property changedZones зоны, изменившиеся с прошлого снимка (метка «Изменилось» — до следующего снимка).
 * @property guardChanged охрана изменилась с прошлого снимка — статус вспыхивает.
 * @property staleSeconds новых снимков нет столько секунд (≥ порога) при живой связи; `null` — данные свежие.
 * @property statuses состояние кнопок команд (отправка, итог); нет записи — кнопка в покое.
 * @property pending команда в работе: остальные кнопки недоступны (одна команда за раз).
 * @property programExiting быстрый монтаж: приложение само выводит блок из режима программирования.
 */
@Immutable
data class CheckStateState(
    val deviceName: String? = null,
    val deviceId: Long? = null,
    val fastMount: Boolean = false,
    val vehicle: VehicleState? = null,
    val tick: Int = 0,
    val changedZones: Set<Zone> = emptySet(),
    val guardChanged: Boolean = false,
    val staleSeconds: Int? = null,
    val commands: List<CommandUi> = emptyList(),
    val statuses: Map<StateCommand, CommandStatus> = emptyMap(),
    val pending: StateCommand? = null,
    val programExiting: Boolean = false,
    val dialog: CheckStateDialog? = null,
) {
    val loading: Boolean get() = vehicle == null

    /** Тревога видна только под охраной (как в макете): иначе «Тревога» в зоне — просто её значение. */
    val alarm: Boolean get() = vehicle?.alarm == true

    fun status(command: StateCommand): CommandStatus = statuses[command] ?: CommandStatus.Idle
}

/** Кнопка команды. [primary] — в колонке пульта; [danger] — через лист подтверждения с удержанием 2 с. */
@Immutable
data class CommandUi(val command: StateCommand, val primary: Boolean = false, val danger: Boolean = false)

@Immutable
sealed interface CommandStatus {
    data object Idle : CommandStatus

    data object Sending : CommandStatus

    /** Итог; [seq] отличает повторный итог той же команды (его сброс по таймеру не трогает новый). */
    data class Finished(val outcome: CommandOutcome, val seq: Int) : CommandStatus
}

enum class CommandOutcome { DONE, REJECTED, NO_REPLY, NO_LINK }

@Immutable
sealed interface CheckStateDialog {
    /** Лист подтверждения опасной команды (запуск двигателя, паника). */
    data class Confirm(val command: StateCommand) : CheckStateDialog

    data class Pin(val pin: ServicePinDialogState = ServicePinDialogState()) : CheckStateDialog

    /** «Выйти из проверки?»: включён режим ТО и/или программирования. */
    data class Exit(val service: Boolean, val program: Boolean) : CheckStateDialog

    /** Быстрый монтаж: «Вернуться на предыдущий шаг?». */
    data object FastBack : CheckStateDialog

    /** Быстрый монтаж: «Не все проверки пройдены. Завершить?». */
    data class FastFinish(val notDone: Int, val total: Int) : CheckStateDialog
}

/** Набор команд по снимку и режиму (порядок — порядок в «Все команды»). */
internal fun commandsFor(vehicle: VehicleState?, fastMount: Boolean): List<CommandUi> {
    val engineRunning = vehicle?.engine?.status == EngineStatus.RUNNING
    val autostartRunning = vehicle?.engine?.autostart is Autostart.Running
    val heater = vehicle?.heater
    val service = if (vehicle?.serviceMode == true) StateCommand.SERVICE_MODE_OFF else StateCommand.SERVICE_MODE_ON
    val program = if (vehicle?.programMode == true) StateCommand.PROGRAM_MODE_OFF else StateCommand.PROGRAM_MODE_ON
    return buildList {
        add(CommandUi(StateCommand.ARM, primary = true))
        add(CommandUi(StateCommand.DISARM, primary = true))
        if (!engineRunning) add(CommandUi(StateCommand.START_ENGINE, primary = true, danger = true))
        if (autostartRunning) add(CommandUi(StateCommand.EXTEND_AUTOSTART))
        if (engineRunning) add(CommandUi(StateCommand.STOP_ENGINE, primary = true))
        add(CommandUi(StateCommand.OPEN_TRUNK, primary = true))
        if (heater != null) add(CommandUi(if (heater.on) StateCommand.HEATER_OFF else StateCommand.HEATER_ON))
        add(CommandUi(service))
        if (!fastMount) add(CommandUi(program))
        add(CommandUi(StateCommand.PANIC, primary = true, danger = true))
        add(CommandUi(StateCommand.LIGHT_WAY))
    }
}
