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
                MountSection.ASSISTANT,
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
    fun `assistant, chat and vehicles are always shown`() {
        assertEquals(
            listOf(MountSection.ASSISTANT, MountSection.SUPPORT_CHAT, MountSection.VEHICLES),
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
    fun `unread features give the unsupported fallback`() {
        assertEquals(
            listOf(
                MountSection.ASSISTANT,
                MountSection.FIRMWARE_UPDATE,
                MountSection.SUPPORT_CHAT,
                MountSection.VEHICLES,
            ),
            (null as Set<DeviceFeature>?).toSections(bluetooth = true),
        )
    }

    @Test
    fun `assistant is the first tile`() {
        assertEquals(MountSection.ASSISTANT, MountSection.entries.first())
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
