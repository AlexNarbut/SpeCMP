package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature

/** [needsCan] — раздел недоступен, пока у блока заводские CAN-настройки (сначала CAN-прошивка). */
@Serializable
@SerialName("advanced-mount.MountSection")
enum class MountSection(val needsCan: Boolean = false) {
    /** Открывается при любом CAN: сам показывает, что закрыто заводским CAN. */
    ASSISTANT,
    FIRMWARE_UPDATE,

    /** Живое состояние блока и команды — только по Bluetooth (SDK не отдаёт телеметрию по USB). */
    CHECK_STATE,
    SETTINGS(needsCan = true),
    CONFIGURATOR,
    BYPASS_LEARNING(needsCan = true),
    CLONE(needsCan = true),

    /** Периферия по типам — только по Bluetooth (SDK читает её только по BLE). */
    SYSTEM_DEVICES,

    /** Служебный лог блока — только по Bluetooth (блок шлёт его только по BLE). */
    SIGNAL_LOG,
    DYNAMIC_DEVICES(needsCan = true),

    TECH_REQUESTS,
    SUPPORT_CHAT,
    VOICE_FIRMWARE,
    COMMUNITY_MAPS,
    VEHICLES,
}

internal val UnsupportedSections =
    listOf(MountSection.ASSISTANT, MountSection.FIRMWARE_UPDATE, MountSection.SUPPORT_CHAT, MountSection.VEHICLES)

/**
 * `null` — возможности не прочитаны: только [UnsupportedSections]; «Проверка состояния», «Устройства системы» и
 * «Лог состояния» — только по Bluetooth.
 */
internal fun Set<DeviceFeature>?.toSections(bluetooth: Boolean): List<MountSection> {
    if (this == null) return UnsupportedSections
    return MountSection.entries.filter { section ->
        when (section) {
            MountSection.FIRMWARE_UPDATE -> DeviceFeature.FIRMWARE_UPDATE in this

            MountSection.SETTINGS,
            MountSection.CONFIGURATOR,
            MountSection.TECH_REQUESTS,
            MountSection.COMMUNITY_MAPS,
            -> DeviceFeature.SETTINGS in this

            MountSection.BYPASS_LEARNING -> DeviceFeature.BYPASS_LEARNING in this

            MountSection.CLONE -> DeviceFeature.CLONE in this

            MountSection.DYNAMIC_DEVICES -> DeviceFeature.DYNAMIC_DEVICES in this

            MountSection.VOICE_FIRMWARE -> DeviceFeature.VOICE_FIRMWARE in this

            MountSection.CHECK_STATE, MountSection.SYSTEM_DEVICES, MountSection.SIGNAL_LOG -> bluetooth

            MountSection.ASSISTANT,
            MountSection.SUPPORT_CHAT,
            MountSection.VEHICLES,
            -> true
        }
    }
}

@Serializable
@SerialName("advanced-mount.MountExtra")
enum class MountExtra {
    REMOTE_SYNC,
}
