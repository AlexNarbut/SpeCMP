package ru.alarmtrade.specmp.feature.device.settings.domain.model

/**
 * Пункт меню настроек, как его отдаёт устройство: раздел ([SettingsSection]) или настройка ([Setting]).
 * [id] уникален в пределах меню, [number] — номер для показа («3.2.1», может быть пустым).
 */
sealed interface SettingNode {
    val id: Int
    val number: String
    val name: String
    val description: String?
}

/** Раздел меню (подменю) с дочерними пунктами произвольной глубины. */
data class SettingsSection(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    val children: List<SettingNode>
) : SettingNode

/** Настройка-лист: 12 типов; [value] — текущее значение в форме, в которой оно уходит в `setValue`. */
sealed interface Setting : SettingNode {
    val value: SettingValue
}

data class SwitchSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Switch,
    /** Подписи состояний «вкл»/«выкл», если устройство их задаёт. */
    val onTitle: String?,
    val offTitle: String?
) : Setting

data class OptionSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Option,
    val options: List<String>
) : Setting

data class IntegerSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Integer,
    val range: IntRange,
    val units: String?,
    /** Подписи крайних значений вместо числа (например «Выкл» при минимуме). */
    val minValueText: String?,
    val maxValueText: String?
) : Setting

data class DecimalSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Decimal,
    val minValue: Float,
    val maxValue: Float,
    val step: Float,
    val units: String?,
    val minValueText: String?,
    val maxValueText: String?
) : Setting

data class TextSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Text,
    val maxLength: Int,
    val inputType: TextInputType,
    /** Регулярное выражение для проверки ввода и текст ошибки от устройства. */
    val validationPattern: String?,
    val validationErrorText: String?
) : Setting

data class TimeSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Time
) : Setting

data class AutoStartScheduleSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.AutoStart,
    val dayOfWeek: Weekday
) : Setting

data class SirenSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Siren,
    /** Диапазон номера тона/мелодии — один на настройку, не зависит от типа. */
    val range: IntRange,
    val units: String?,
    val event: SirenEvent
) : Setting

data class CanCodeSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Can
) : Setting

data class InputSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Input,
    val channel: Channel,
    val defaultPolarity: Polarity,
    /** Все функции, которые можно назначить входу; состояние — [InputValue.logicsState] по [ChannelLogic.id]. */
    val availableLogics: List<ChannelLogic>,
    val enabledLogics: List<ChannelLogic>
) : Setting

data class OutputSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Output,
    val channel: Channel,
    val canEditPolarity: Boolean,
    val defaultPolarity: Polarity,
    val availableLogics: List<ChannelLogic>,
    val enabledLogics: List<ChannelLogic>
) : Setting

data class SwitchOutputSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.SwitchOutput,
    val channel: Channel,
    val polarity: Polarity,
    /** Назначенные функции — только для показа. */
    val enabledLogics: List<ChannelLogic>
) : Setting
