package ru.alarmtrade.specmp.feature.device.firmware.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder

private const val KEY_SYSTEMS_ORDER = "systems_order"

/** [FirmwareArchivePreferences] в именованном хранилище приложения (`AppSettingsFactory`). */
class SettingsFirmwareArchivePreferences(private val settings: Settings) : FirmwareArchivePreferences {

    // Неизвестное (из будущей версии) значение — по умолчанию, а не падение.
    private val stored = MutableStateFlow(
        safeRun {
            settings.getStringOrNull(KEY_SYSTEMS_ORDER)?.let { saved ->
                SystemsOrder.entries.firstOrNull {
                    it.name ==
                        saved
                }
            }
                ?: SystemsOrder.Added
        },
    )
    override val systemsOrder: StateFlow<Response<SystemsOrder>> = stored.asStateFlow()

    override suspend fun setSystemsOrder(order: SystemsOrder): Response<Unit> = safeRun {
        settings.putString(KEY_SYSTEMS_ORDER, order.name)
        stored.update { Response.Success(order) }
    }
}
