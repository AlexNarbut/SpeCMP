package ru.alarmtrade.specmp.feature.device.state.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CameraInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.DeviceEventKind
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvCharging
import ru.alarmtrade.specmp.feature.device.state.domain.model.EvInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.HeaterInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.TirePressure
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Window
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateChecklist
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateFastMount
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistTask
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateActions
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateContent
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateDialog
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateState
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandOutcome
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CommandStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.commandsFor
import ru.alarmtrade.specmp.feature.device.state.ui.vehicle

/**
 * Состояния экрана «Проверка состояния» (бриф S1–S14, F1–F6); PNG — в `build/state-gallery/device-state`.
 * `_pult` — вкладка «Пульт», `_details` — «Подробно» (на узком окне), широкие окна показывают обе колонки.
 */
class CheckStateGallery {

    private val outDir = File("build/state-gallery/device-state")

    private val normal = vehicle(
        zones = mapOf(Zone.HANDBRAKE to ZoneValue.ON, Zone.NEUTRAL to ZoneValue.ON),
    ).copy(event = DeviceEventKind.DISARMING)

    private fun state(vehicle: VehicleState?, fastMount: Boolean = false) = CheckStateState(
        deviceName = "Pandora DX-4GS",
        deviceId = 107131,
        fastMount = fastMount,
        vehicle = vehicle,
        tick = 5,
        commands = commandsFor(vehicle, fastMount),
    )

    private fun render(name: String, state: CheckStateState, tab: Int? = null, fastMount: CheckStateFastMount? = null) =
        StateGallery.render(outDir, "check_$name") {
            CheckStateContent(state = state, actions = CheckStateActions(), fastMount = fastMount, initialTab = tab)
        }

    private fun both(name: String, state: CheckStateState, fastMount: CheckStateFastMount? = null) {
        val offset = if (fastMount != null && fastMount.checklist.tasks.isNotEmpty()) 1 else 0
        render("${name}_pult", state, tab = offset, fastMount = fastMount)
        render("${name}_details", state, tab = offset + 1, fastMount = fastMount)
    }

    @Test
    fun s01Loading() = both("s01_loading", state(null))

    @Test
    fun s02Normal() = both("s02_normal", state(normal))

    @Test
    fun s03Changed() = both(
        "s03_changed",
        state(normal.copy(zones = normal.zones + (Zone.DOOR_FRONT_LEFT to ZoneValue.ON) + (Zone.BRAKE to ZoneValue.ON)))
            .copy(changedZones = setOf(Zone.DOOR_FRONT_LEFT, Zone.BRAKE)),
    )

    @Test
    fun s04Armed() =
        both("s04_armed", state(normal.copy(security = SecurityState.ARMED, event = DeviceEventKind.ARMING)))

    @Test
    fun s05Alarm() = both(
        "s05_alarm",
        state(
            normal.copy(
                security = SecurityState.ARMED,
                alarm = true,
                zones = normal.zones + (Zone.TRUNK to ZoneValue.ALARM) + (Zone.SHOCK to ZoneValue.MAIN),
            ),
        ),
    )

    @Test
    fun s06ServiceMode() = render("s06_service", state(normal.copy(serviceMode = true, programMode = true)), tab = 0)

    @Test
    fun s06bProgramOnly() = render("s06b_program", state(normal.copy(programMode = true)), tab = 0)

    @Test
    fun s06cServiceOnly() =
        render("s06c_service_only", state(normal.copy(serviceMode = true, security = SecurityState.ARMED)), tab = 0)

    @Test
    fun s06dProgramExit() =
        render("s06d_program_exit", state(normal.copy(programMode = true)).copy(programExiting = true), tab = 0)

    @Test
    fun s07Autostart() = both(
        "s07_autostart",
        state(normal.copy(engine = EngineInfo(EngineStatus.RUNNING, 82, 900, Autostart.Running(452)))),
    )

    @Test
    fun s09Max() = both(
        "s09_max",
        state(
            normal.copy(
                can = CanInfo(
                    84_210,
                    410,
                    8,
                    true,
                    setOf(Window.FRONT_LEFT),
                    TirePressure(2.3f, 2.3f, 2.2f, 2.4f, 2.5f),
                ),
                ev = EvInfo(EvCharging.SLOW, 78, 96, 24, 312),
                heater = HeaterInfo(on = false, temperature = 18, voltage = 12.5f, powerPercent = 0, hasErrors = true),
                camera = CameraInfo(recording = true, minutes = 42),
            ),
        ),
    )

    @Test
    fun s10Stale() = render("s10_stale", state(normal).copy(staleSeconds = 14), tab = 0)

    @Test
    fun s11Commands() = render(
        "s11_commands",
        state(normal).copy(
            pending = StateCommand.ARM,
            statuses = mapOf(
                StateCommand.ARM to CommandStatus.Sending,
                StateCommand.OPEN_TRUNK to CommandStatus.Finished(CommandOutcome.DONE, 1),
                StateCommand.PANIC to CommandStatus.Finished(CommandOutcome.NO_REPLY, 2),
            ),
        ),
        tab = 0,
    )

    @Test
    fun s12Confirm() =
        render("s12_confirm", state(normal).copy(dialog = CheckStateDialog.Confirm(StateCommand.START_ENGINE)), tab = 0)

    @Test
    fun s13Pin() =
        render("s13_pin", state(normal).copy(dialog = CheckStateDialog.Pin(ServicePinDialogState())), tab = 0)

    @Test
    fun s14Exit() = render(
        "s14_exit",
        state(normal.copy(serviceMode = true)).copy(dialog = CheckStateDialog.Exit(service = true, program = false)),
        tab = 0,
    )

    @Test
    fun s15CommandLabels() = render(
        "s15_command_labels",
        state(
            normal.copy(
                serviceMode = true,
                heater = HeaterInfo(on = true, temperature = 18, voltage = 12.5f, powerPercent = 40, hasErrors = false),
            ),
        ),
        tab = 1,
    )

    private val tasks = listOf(
        ChecklistTask("t1", "Откройте водительскую дверь", ChecklistStatus.PASSED, zone = Zone.DOOR_FRONT_LEFT),
        ChecklistTask("t2", "Откройте капот", zone = Zone.HOOD),
        ChecklistTask("t3", "Нажмите педаль тормоза", zone = Zone.BRAKE),
        ChecklistTask("t4", "Поставьте на охрану брелоком", guard = true),
        ChecklistTask("t5", "Сработала ли сирена при постановке на охрану?", options = listOf("Да", "Нет")),
        ChecklistTask("t6", "Затяните ручник", ChecklistStatus.SKIPPED, zone = Zone.HANDBRAKE),
    )

    private fun fast(tasks: List<ChecklistTask>) = CheckStateFastMount(CheckStateChecklist(tasks), {
    }, {}, { }, { _, _ -> })

    @Test
    fun f01Checklist() = render("f01_checklist", state(normal, fastMount = true), tab = 0, fastMount = fast(tasks))

    /** Строгий порядок (курсор SDK): текущее задание выделено, остальные — «Ожидает». */
    @Test
    fun f02Sequential() = render(
        "f02_sequential",
        state(normal, fastMount = true),
        tab = 0,
        fastMount = CheckStateFastMount(
            CheckStateChecklist(
                tasks.mapIndexed { index, task ->
                    task.copy(
                        status = if (index ==
                            0
                        ) {
                            ChecklistStatus.PASSED
                        } else {
                            ChecklistStatus.PENDING
                        },
                        active = index == 1,
                    )
                },
                sequential = true,
            ),
            {},
            {},
            {},
            { _, _ -> },
        ),
    )

    @Test
    fun f04AllDone() = render(
        "f04_all_done",
        state(normal, fastMount = true),
        tab = 0,
        fastMount = fast(tasks.map { it.copy(status = ChecklistStatus.PASSED) }),
    )

    @Test
    fun f05Finish() = render(
        "f05_finish",
        state(normal, fastMount = true).copy(dialog = CheckStateDialog.FastFinish(4, 6)),
        tab = 0,
        fastMount = fast(tasks),
    )

    @Test
    fun f06NoTasks() = render("f06_no_tasks", state(normal, fastMount = true), tab = 0, fastMount = fast(emptyList()))
}
