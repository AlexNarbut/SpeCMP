package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
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
        is IntegerSetting, is DecimalSetting, is TextSetting, is TimeSetting, is InputSetting, is OutputSetting -> true
        else -> false
    }

/** Можно ли применить набранное значение (диапазон, длина, шаблон устройства). */
internal fun Setting.accepts(draft: SettingValue): Boolean = when {
    this is IntegerSetting && draft is SettingValue.Integer -> draft.value in range
    this is DecimalSetting && draft is SettingValue.Decimal -> draft.value in minValue..maxValue
    this is TextSetting && draft is SettingValue.Text -> draft.value.length <= maxLength && matchesPattern(draft.value)
    this is OptionSetting && draft is SettingValue.Option -> draft.index in options.indices
    else -> draft::class == value::class
}

/** Шаблон устройства; некорректный шаблон не блокирует ввод. */
internal fun TextSetting.matchesPattern(text: String): Boolean {
    val pattern = validationPattern ?: return true
    val regex = try {
        Regex(pattern)
    } catch (ignored: IllegalArgumentException) {
        return true
    }
    return regex.matches(text)
}
