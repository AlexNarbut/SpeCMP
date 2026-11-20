package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountBypass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheck
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountCheckResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResultConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountUnit
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountWorks
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.fastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.ui.question
import ru.alarmtrade.specmp.feature.fastmount.ui.testConfiguration

class FastMountResultTest {

    private val car = MountCar(id = 1, brand = "Volkswagen", model = "Polo", years = "2020", canCode = "0213")

    private fun finished(
        transport: ConnectionTransport = ConnectionTransport.BLE,
        keyBypass: Boolean = true,
        slave: Boolean = true,
        cloneNeeded: Boolean? = false,
        slaveDone: Boolean = true,
        statuses: Map<Int, MountTaskStatus> = mapOf(5 to MountTaskStatus.PASSED, 100 to MountTaskStatus.FAILED),
    ): FastMountState {
        val configuration = testConfiguration(
            keyBypass = keyBypass,
            slave = slave,
            questions = listOf(question(5, automatic = true), question(100), question(101)),
        )
        return FastMountState(
            fixed = FixedDevice(systemId = 77, systemName = "DX-9X", title = "DX-9X · 77", transport = transport),
            unit = UnitInfo(firmware = "3.57", canCode = "0213", canVersion = "1.2", canFactory = false),
            screen = WizardScreen.MASTER,
            car = car,
            configuration = configuration,
            plan = fastMountPlan(configuration, transport),
            master = MasterState(
                step = FastMountStep.FINISH,
                cloneNeeded = cloneNeeded,
                slaveDone = slaveDone,
                statuses = statuses,
                mountMinutes = 47,
            ),
        ).withChecklist()
    }

    @Test
    fun `unit, car, configuration and duration come from the wizard`() {
        val result = requireNotNull(finished().fastMountResult())

        assertEquals(
            FastMountUnit("DX-9X", 77, "3.57", canCode = "0213", canVersion = "1.2", canFactory = false),
            result.unit,
        )
        assertEquals(car, result.car)
        assertEquals(FastMountResultConfiguration(id = 42, name = "Polo VI", author = "Автор"), result.configuration)
        assertEquals(ConnectionTransport.BLE, result.transport)
        assertEquals(47, result.works.durationMinutes)
        assertEquals(true, result.works.settingsWritten)
    }

    @Test
    fun `trained bypass without clone`() {
        val works = requireNotNull(finished(cloneNeeded = false).fastMountResult()).works

        assertEquals(
            FastMountWorks(true, FastMountBypass.TRAINED, clone = false, slave = true, durationMinutes = 47),
            works,
        )
    }

    @Test
    fun `bypass that needed clone records clone done`() {
        val works = requireNotNull(finished(cloneNeeded = true).fastMountResult()).works

        assertEquals(FastMountBypass.CLONE_NEEDED, works.bypass)
        assertEquals(true, works.clone)
    }

    @Test
    fun `configuration without bypass has no bypass and no clone`() {
        val works = requireNotNull(finished(keyBypass = false, cloneNeeded = null).fastMountResult()).works

        assertEquals(FastMountBypass.NONE, works.bypass)
        assertEquals(false, works.clone)
    }

    @Test
    fun `system check keeps task results and unanswered tasks are skipped`() {
        val check = requireNotNull(finished().fastMountResult()).systemCheck

        assertEquals(
            listOf(
                FastMountCheck("Задание 5", FastMountCheckResult.PASSED),
                FastMountCheck("Задание 100", FastMountCheckResult.FAILED),
                FastMountCheck("Задание 101", FastMountCheckResult.SKIPPED),
            ),
            check,
        )
    }

    @Test
    fun `usb mount has no slave and no system check`() {
        val result = requireNotNull(
            finished(transport = ConnectionTransport.USB, slaveDone = false, statuses = emptyMap()).fastMountResult(),
        )

        assertEquals(false, result.works.slave)
        assertEquals(emptyList(), result.systemCheck)
        assertEquals(ConnectionTransport.USB, result.transport)
    }

    @Test
    fun `slave is recorded only when the configuration has it`() {
        assertEquals(false, requireNotNull(finished(slave = false).fastMountResult()).works.slave)
    }

    @Test
    fun `no result before the finish`() {
        val state = finished().let { it.copy(master = it.master.copy(step = FastMountStep.FEEDBACK)) }

        assertNull(state.fastMountResult())
    }
}
