package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature

/** Раздел расширенного монтажа (пункт меню), доступность — по возможностям блока. */
@Serializable
enum class MountSection {
    SETTINGS,
    FIRMWARE_UPDATE,
    VOICE_FIRMWARE,
    BYPASS_LEARNING,
    CLONE,
    DYNAMIC_DEVICES,
}

/**
 * Меню по возможностям блока (порядок — как в старом приложении). Возможности неизвестны —
 * меню по умолчанию: проверка прошивки.
 */
internal fun Set<DeviceFeature>.toSections(): List<MountSection> {
    if (isEmpty()) return listOf(MountSection.FIRMWARE_UPDATE)
    return MountSection.entries.filter { section -> section.feature in this }
}

private val MountSection.feature: DeviceFeature
    get() = when (this) {
        MountSection.SETTINGS -> DeviceFeature.SETTINGS
        MountSection.FIRMWARE_UPDATE -> DeviceFeature.FIRMWARE_UPDATE
        MountSection.VOICE_FIRMWARE -> DeviceFeature.VOICE_FIRMWARE
        MountSection.BYPASS_LEARNING -> DeviceFeature.BYPASS_LEARNING
        MountSection.CLONE -> DeviceFeature.CLONE
        MountSection.DYNAMIC_DEVICES -> DeviceFeature.DYNAMIC_DEVICES
    }
