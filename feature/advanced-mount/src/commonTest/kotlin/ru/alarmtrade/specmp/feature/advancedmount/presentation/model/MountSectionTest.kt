package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature

class MountSectionTest {

    @Test
    fun `all features give all sections in grid order`() {
        assertEquals(MountSection.entries.toList(), DeviceFeature.entries.toSet().toSections(bluetooth = true))
    }

    @Test
    fun `settings feature brings configurator, tech requests and community maps`() {
        assertEquals(
            listOf(
                MountSection.FIRMWARE_UPDATE,
                MountSection.SETTINGS,
                MountSection.CONFIGURATOR,
                MountSection.TECH_REQUESTS,
                MountSection.SUPPORT_CHAT,
                MountSection.COMMUNITY_MAPS,
                MountSection.VEHICLES,
            ),
            setOf(DeviceFeature.FIRMWARE_UPDATE, DeviceFeature.SETTINGS).toSections(bluetooth = false),
        )
    }

    @Test
    fun `chat and vehicles are always shown`() {
        assertEquals(
            listOf(MountSection.SUPPORT_CHAT, MountSection.VEHICLES),
            emptySet<DeviceFeature>().toSections(bluetooth = false),
        )
    }

    @Test
    fun `state check is shown only over bluetooth`() {
        val features = setOf(DeviceFeature.SETTINGS)
        assertEquals(true, MountSection.CHECK_STATE in features.toSections(bluetooth = true))
        assertEquals(false, MountSection.CHECK_STATE in features.toSections(bluetooth = false))
    }

    @Test
    fun `system devices are shown only over bluetooth, pairing follows the feature`() {
        val features = setOf(DeviceFeature.DYNAMIC_DEVICES)
        assertEquals(true, MountSection.SYSTEM_DEVICES in emptySet<DeviceFeature>().toSections(bluetooth = true))
        assertEquals(false, MountSection.SYSTEM_DEVICES in features.toSections(bluetooth = false))
        assertEquals(true, MountSection.DYNAMIC_DEVICES in features.toSections(bluetooth = false))
        assertEquals(false, MountSection.DYNAMIC_DEVICES in emptySet<DeviceFeature>().toSections(bluetooth = true))
    }

    @Test
    fun `unread features give the unsupported fallback`() {
        assertEquals(
            listOf(MountSection.FIRMWARE_UPDATE, MountSection.SUPPORT_CHAT, MountSection.VEHICLES),
            (null as Set<DeviceFeature>?).toSections(bluetooth = true),
        )
    }

    @Test
    fun `only device configuration sections need CAN firmware`() {
        assertEquals(
            setOf(
                MountSection.SETTINGS,
                MountSection.BYPASS_LEARNING,
                MountSection.CLONE,
                MountSection.DYNAMIC_DEVICES,
            ),
            MountSection.entries.filter { it.needsCan }.toSet(),
        )
    }
}
