package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwarePreselect

/** Экран сразу предлагает перевести модуль в загрузчик; [version] `null` — прошить последней версией с сервера. */
data class FirmwareAutoBoot(val moduleId: Long, val version: FirmwareVersion?)

internal val FirmwareAutoBoot.preselect: FirmwarePreselect
    get() = version?.let(FirmwarePreselect::Version) ?: FirmwarePreselect.Latest
