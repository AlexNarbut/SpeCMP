package ru.alarmtrade.specmp.feature.device.settings.domain.model

sealed interface SettingNode {
    val id: Int
    val number: String
    val name: String
    val description: String?
}

data class SettingsSection(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    val children: List<SettingNode>,
) : SettingNode

/** [value] — текущее значение в форме, в которой оно уходит в `setValue`. */
sealed interface Setting : SettingNode {
    val value: SettingValue
}

val SettingNode.channelOrNull: Channel?
    get() = when (this) {
        is InputSetting -> channel
        is OutputSetting -> channel
        is SwitchOutputSetting -> channel
        else -> null
    }

data class SwitchSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Switch,
    val onTitle: String?,
    val offTitle: String?,
) : Setting

data class OptionSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Option,
    val options: List<String>,
) : Setting

data class IntegerSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Integer,
    val range: IntRange,
    val units: String?,
    // Подпись края из описания настройки в SDK (в core/localization её не перенести).
    @Suppress("konsist.domain-model-ui-text")
    val minValueText: String?,
    // Подпись края из описания настройки в SDK (в core/localization её не перенести).
    @Suppress("konsist.domain-model-ui-text")
    val maxValueText: String?,
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
    // Подпись края из описания настройки в SDK (в core/localization её не перенести).
    @Suppress("konsist.domain-model-ui-text")
    val minValueText: String?,
    // Подпись края из описания настройки в SDK (в core/localization её не перенести).
    @Suppress("konsist.domain-model-ui-text")
    val maxValueText: String?,
) : Setting

data class TextSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Text,
    val maxLength: Int,
    val inputType: TextInputType,
    val validationPattern: String?,
    // Сообщение к validationPattern из описания настройки в SDK.
    @Suppress("konsist.domain-model-ui-text")
    val validationErrorText: String?,
) : Setting {
    /**
     * Скомпилированный [validationPattern] — один раз на настройку; некорректный шаблон — `null` (ввод не блокирует).
     */
    val validationRegex: Regex? by lazy {
        validationPattern?.let { pattern ->
            try {
                Regex(pattern)
            } catch (ignored: IllegalArgumentException) {
                null
            }
        }
    }
}

data class TimeSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Time,
) : Setting

data class AutoStartScheduleSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.AutoStart,
    val dayOfWeek: Weekday,
) : Setting

data class SirenSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Siren,
    val range: IntRange,
    val units: String?,
    val event: SirenEvent,
) : Setting

data class CanCodeSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Can,
) : Setting

data class InputSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.Input,
    val channel: Channel,
    val defaultPolarity: Polarity,
    val availableLogics: List<ChannelLogic>,
    val enabledLogics: List<ChannelLogic>,
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
    val enabledLogics: List<ChannelLogic>,
) : Setting

data class SwitchOutputSetting(
    override val id: Int,
    override val number: String,
    override val name: String,
    override val description: String?,
    override val value: SettingValue.SwitchOutput,
    val channel: Channel,
    val polarity: Polarity,
    val enabledLogics: List<ChannelLogic>,
) : Setting
