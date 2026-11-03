package ru.alarmtrade.specmp.feature.fastmount.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

class FastMountPlanTest {

    private fun configuration(keyBypass: Boolean, slave: Boolean) = FastMountConfiguration(
        id = 1,
        name = "",
        shortDescription = "",
        fullDescription = "",
        deviceName = "DX-9X",
        author = "",
        rating = 0,
        mountingMap = null,
        images = emptyList(),
        keyBypass = keyBypass,
        slave = slave,
        questions = emptyList()
    )

    @Test
    fun bluetoothFullPath() {
        val plan = requireNotNull(fastMountPlan(configuration(keyBypass = true, slave = true), ConnectionTransport.BLE))
        assertEquals(
            listOf(
                PlannedStep(FastMountStep.WRITING_SETTINGS),
                PlannedStep(FastMountStep.MOUNT),
                PlannedStep(FastMountStep.KEY_BYPASS),
                PlannedStep(FastMountStep.CLONE, conditional = true),
                PlannedStep(FastMountStep.SLAVE),
                PlannedStep(FastMountStep.SYSTEM_CHECK),
                PlannedStep(FastMountStep.FEEDBACK)
            ),
            plan.steps
        )
        assertNull(plan.indexOf(FastMountStep.REBOOTING))
    }

    @Test
    fun bluetoothWithoutBypassAndSlave() {
        val plan =
            requireNotNull(fastMountPlan(configuration(keyBypass = false, slave = false), ConnectionTransport.BLE))
        assertEquals(
            listOf(
                FastMountStep.WRITING_SETTINGS,
                FastMountStep.MOUNT,
                FastMountStep.SYSTEM_CHECK,
                FastMountStep.FEEDBACK
            ),
            plan.steps.map { it.step }
        )
    }

    @Test
    fun usbHasNoSlaveAndSystemCheck() {
        val plan = requireNotNull(fastMountPlan(configuration(keyBypass = true, slave = true), ConnectionTransport.USB))
        assertEquals(
            listOf(
                FastMountStep.WRITING_SETTINGS,
                FastMountStep.MOUNT,
                FastMountStep.KEY_BYPASS,
                FastMountStep.CLONE,
                FastMountStep.FEEDBACK
            ),
            plan.steps.map { it.step }
        )
    }

    @Test
    fun serialIsNotSupported() {
        assertNull(fastMountPlan(configuration(keyBypass = false, slave = false), ConnectionTransport.SERIAL))
    }
}
