package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature

class MountSectionTest {

    @Test
    fun `all features give all sections in grid order`() {
        assertEquals(MountSection.entries.toList(), DeviceFeature.entries.toSet().toSections())
    }

    @Test
    fun `settings feature brings tech requests and community maps`() {
        assertEquals(
            listOf(
                MountSection.FIRMWARE_UPDATE,
                MountSection.SETTINGS,
                MountSection.TECH_REQUESTS,
                MountSection.SUPPORT_CHAT,
                MountSection.COMMUNITY_MAPS,
                MountSection.VEHICLES
            ),
            setOf(DeviceFeature.FIRMWARE_UPDATE, DeviceFeature.SETTINGS).toSections()
        )
    }

    @Test
    fun `chat and vehicles are always shown`() {
        assertEquals(listOf(MountSection.SUPPORT_CHAT, MountSection.VEHICLES), emptySet<DeviceFeature>().toSections())
    }

    @Test
    fun `unread features give the unsupported fallback`() {
        assertEquals(
            listOf(MountSection.FIRMWARE_UPDATE, MountSection.SUPPORT_CHAT, MountSection.VEHICLES),
            (null as Set<DeviceFeature>?).toSections()
        )
    }

    @Test
    fun `only device configuration sections need CAN firmware`() {
        assertEquals(
            setOf(
                MountSection.SETTINGS,
                MountSection.BYPASS_LEARNING,
                MountSection.CLONE,
                MountSection.DYNAMIC_DEVICES
            ),
            MountSection.entries.filter { it.needsCan }.toSet()
        )
    }
}
