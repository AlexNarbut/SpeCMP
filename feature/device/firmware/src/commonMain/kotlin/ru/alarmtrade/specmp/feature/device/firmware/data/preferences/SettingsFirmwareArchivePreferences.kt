package ru.alarmtrade.specmp.feature.device.firmware.data.preferences

import com.russhwolf.settings.Settings
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder

private const val KEY_SYSTEMS_ORDER = "systems_order"

/** [FirmwareArchivePreferences] в именованном хранилище приложения (`AppSettingsFactory`). */
class SettingsFirmwareArchivePreferences(private val settings: Settings) : FirmwareArchivePreferences {

    // Неизвестное (из будущей версии) значение — по умолчанию, а не падение.
    override fun systemsOrder(): SystemsOrder = settings.getStringOrNull(KEY_SYSTEMS_ORDER)?.let { saved ->
        SystemsOrder.entries.firstOrNull { it.name == saved }
    }
        ?: SystemsOrder.Added

    override fun setSystemsOrder(order: SystemsOrder) = settings.putString(KEY_SYSTEMS_ORDER, order.name)
}
