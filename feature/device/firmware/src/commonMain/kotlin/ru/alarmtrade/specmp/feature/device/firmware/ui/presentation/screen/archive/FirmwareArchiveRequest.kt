package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/**
 * @property moduleId `null` — основной модуль блока [deviceName] (архив открыт не из загрузчика).
 * @property noUnit вход с главного без блока: система — [deviceName], подсистема — [deviceTitle].
 */
@Serializable
@SerialName("device.firmware.FirmwareArchiveRequest")
data class FirmwareArchiveRequest(
    val moduleId: Long?,
    val channel: FirmwareChannel,
    val installedVersion: String?,
    val deviceTitle: String,
    val profile: UpdateProfile,
    val deviceName: String? = null,
    val canCode: String? = null,
    val viewOnly: Boolean = false,
    val deviceCanCode: String? = null,
    val deviceCanVersion: Int? = null,
    val deviceCanProfile: String? = null,
    val deviceCanFactory: Boolean = false,
    val noUnit: Boolean = false,
    val canFilterAllowed: Boolean = true,
    val moduleName: String? = null,
)
