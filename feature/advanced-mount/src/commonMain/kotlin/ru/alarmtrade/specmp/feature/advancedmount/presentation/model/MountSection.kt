package ru.alarmtrade.specmp.feature.advancedmount.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature

/**
 * Раздел расширенного монтажа (плитка меню). Порядок — порядок в сетке.
 * [needsCan] — раздел недоступен, пока у блока заводские CAN-настройки (сначала CAN-прошивка).
 */
@Serializable
@SerialName("advanced-mount.MountSection")
enum class MountSection(val needsCan: Boolean = false) {
    FIRMWARE_UPDATE,

    /** Живое состояние блока и команды — только по Bluetooth (SDK не отдаёт телеметрию по USB). */
    CHECK_STATE,
    SETTINGS(needsCan = true),
    CONFIGURATOR,
    BYPASS_LEARNING(needsCan = true),
    CLONE(needsCan = true),
    DYNAMIC_DEVICES(needsCan = true),

    TECH_REQUESTS,
    SUPPORT_CHAT,
    VOICE_FIRMWARE,
    COMMUNITY_MAPS,
    VEHICLES,
}

/** Разделы, доступные, когда возможности блока прочитать не удалось («устройство не поддерживается»). */
internal val UnsupportedSections =
    listOf(MountSection.FIRMWARE_UPDATE, MountSection.SUPPORT_CHAT, MountSection.VEHICLES)

/**
 * Меню по возможностям блока (правила старого приложения). `null` — возможности не прочитаны:
 * базовые разделы [UnsupportedSections]. [bluetooth] — блок подключён по BLE: «Проверка состояния» только по нему.
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

            MountSection.CHECK_STATE -> bluetooth

            MountSection.SUPPORT_CHAT,
            MountSection.VEHICLES,
            -> true
        }
    }
}

/** Экраны монтажа вне сетки разделов (кнопки шапки и верхней панели); пока заглушки — в дизайне не проработаны. */
@Serializable
@SerialName("advanced-mount.MountExtra")
enum class MountExtra {
    PRODUCT_INFO,
    REMOTE_SYNC,
}
