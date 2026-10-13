package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import kotlin.test.Test
import kotlin.test.assertEquals

class MountSectionTest {

    @Test
    fun `sections follow device features in menu order`() {
        val sections = setOf(DeviceFeature.CLONE, DeviceFeature.SETTINGS, DeviceFeature.FIRMWARE_UPDATE).toSections()

        assertEquals(listOf(MountSection.SETTINGS, MountSection.FIRMWARE_UPDATE, MountSection.CLONE), sections)
    }

    @Test
    fun `unknown features give default menu`() {
        assertEquals(listOf(MountSection.FIRMWARE_UPDATE), emptySet<DeviceFeature>().toSections())
    }
}
