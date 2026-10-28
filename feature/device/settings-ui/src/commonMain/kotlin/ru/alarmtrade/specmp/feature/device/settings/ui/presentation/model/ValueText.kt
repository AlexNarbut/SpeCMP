package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.runtime.Composable
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_format_decimal_separator
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_nc
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_no
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_empty
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_functions
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_off
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_on
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_minus
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_plus
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_schedule
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_siren_melody
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_siren_tone
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartScheduleSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenToneType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting

private const val MAX_DECIMALS = 3
private const val TEN = 10.0
private const val TIME_DIGITS = 2

/** Текущее значение настройки строкой для показа. */
@Composable
internal fun valueText(setting: Setting): String = valueText(setting, setting.value)

/** Значение [value] настройки [setting] строкой для показа (в т. ч. «Было: …» в окне редактирования). */
@Suppress("CyclomaticComplexMethod") // плоский when по типам настроек
@Composable
internal fun valueText(setting: Setting, value: SettingValue): String = when {
    setting is SwitchSetting && value is SettingValue.Switch -> switchText(setting, value.enabled)

    setting is SwitchOutputSetting && value is SettingValue.SwitchOutput ->
        stringResource(
            if (value.enabled) Strings.string.settings_common_value_on else Strings.string.settings_common_value_off
        )

    setting is OptionSetting && value is SettingValue.Option -> setting.options.getOrElse(value.index) { "" }

    setting is IntegerSetting && value is SettingValue.Integer ->
        edgeText(value.value, setting.range.first, setting.range.last, setting.minValueText, setting.maxValueText)
            ?: withUnits(value.value.toString(), setting.units)

    setting is DecimalSetting && value is SettingValue.Decimal -> decimalText(setting, value.value)

    setting is TextSetting && value is SettingValue.Text ->
        value.value.ifEmpty { stringResource(Strings.string.settings_common_value_empty) }

    setting is TimeSetting && value is SettingValue.Time -> value.value.text()

    setting is AutoStartScheduleSetting && value is SettingValue.AutoStart -> {
        val empty = stringResource(Strings.string.settings_common_value_empty)
        val schedule = value.value
        stringResource(
            Strings.string.settings_common_value_schedule,
            if (schedule.isMorningStartEnabled) schedule.morningStartTime.text() else empty,
            if (schedule.isEveningStartEnabled) schedule.eveningStartTime.text() else empty
        )
    }

    setting is SirenSetting && value is SettingValue.Siren -> stringResource(
        if (value.value.type == SirenToneType.TONE) {
            Strings.string.settings_common_value_siren_tone
        } else {
            Strings.string.settings_common_value_siren_melody
        },
        value.value.number
    )

    setting is CanCodeSetting && value is SettingValue.Can -> value.value.code

    setting is InputSetting && value is SettingValue.Input -> listOfNotNull(
        when (value.value.type) {
            InputContactType.NORMALLY_OPEN -> stringResource(Strings.string.settings_common_value_contact_no)
            InputContactType.NORMALLY_CLOSED -> stringResource(Strings.string.settings_common_value_contact_nc)
            InputContactType.UNDEFINED -> null
        },
        stringResource(Strings.string.settings_common_value_functions, value.value.logicsState.count { it })
    ).joinToString(" · ")

    setting is OutputSetting && value is SettingValue.Output -> listOfNotNull(
        polarityText(value.value.polarity),
        stringResource(Strings.string.settings_common_value_functions, value.value.logicsState.count { it })
    ).joinToString(" · ")

    else -> ""
}

@Composable
private fun switchText(setting: SwitchSetting, enabled: Boolean): String = if (enabled) {
    setting.onTitle ?: stringResource(Strings.string.settings_common_value_on)
} else {
    setting.offTitle ?: stringResource(Strings.string.settings_common_value_off)
}

@Composable
private fun polarityText(polarity: Polarity): String? = when (polarity) {
    Polarity.PLUS -> stringResource(Strings.string.settings_common_value_polarity_plus)
    Polarity.MINUS -> stringResource(Strings.string.settings_common_value_polarity_minus)
    Polarity.UNDEFINED -> null
}

@Composable
internal fun decimalText(setting: DecimalSetting, value: Float): String =
    edgeText(value, setting.minValue, setting.maxValue, setting.minValueText, setting.maxValueText)
        ?: withUnits(decimalNumber(setting, value), setting.units)

/** Число дробной настройки с количеством знаков по шагу и разделителем языка (без единиц). */
@Composable
internal fun decimalNumber(setting: DecimalSetting, value: Float): String = formatDecimal(
    value,
    decimalsOf(setting.step),
    stringResource(Strings.string.settings_common_format_decimal_separator)
)

internal fun TimeOfDay.text(): String =
    "${hours.toString().padStart(TIME_DIGITS, '0')}:${minutes.toString().padStart(TIME_DIGITS, '0')}"

/** Знаков после запятой, достаточных для шага (0.5 → 1, 0.25 → 2), не больше трёх. */
internal fun decimalsOf(step: Float): Int = (0..MAX_DECIMALS).firstOrNull { decimals ->
    val scaled = step * TEN.pow(decimals)
    abs(scaled - scaled.roundToLong()) < STEP_EPSILON
} ?: MAX_DECIMALS

private const val STEP_EPSILON = 1e-4

/** Десятичная запись без `String.format` (нет в common): округление до [decimals] знаков. */
internal fun formatDecimal(value: Float, decimals: Int, separator: String): String {
    val factor = TEN.pow(decimals).toLong()
    val scaled = (value.toDouble() * factor).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val whole = abs(scaled) / factor
    if (decimals == 0) return "$sign$whole"
    val fraction = (abs(scaled) % factor).toString().padStart(decimals, '0')
    return "$sign$whole$separator$fraction"
}
