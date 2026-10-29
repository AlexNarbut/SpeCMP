package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartScheduleSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting

/** Сегменты прямо в строке — для выбора из ≤ 3 вариантов по ≤ 6 символов. */
private const val SEGMENTS_MAX_OPTIONS = 3
private const val SEGMENT_MAX_LENGTH = 6

internal val OptionSetting.isSegmented: Boolean
    get() = options.size in 2..SEGMENTS_MAX_OPTIONS && options.all { it.length <= SEGMENT_MAX_LENGTH }

/** Настройка редактируется в окне (шторка / диалог), а не прямо в строке. */
internal val Setting.opensEditor: Boolean
    get() = when (this) {
        is OptionSetting -> !isSegmented

        is IntegerSetting, is DecimalSetting, is TextSetting, is TimeSetting, is InputSetting, is OutputSetting,
        is AutoStartScheduleSetting, is SirenSetting, is CanCodeSetting -> true

        else -> false
    }
