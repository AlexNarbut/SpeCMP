package ru.alarmtrade.specmp.feature.device.settings.domain.model

const val CAN_CODE_LENGTH = 4

private val CAN_CODE_PATTERN = Regex("\\d{$CAN_CODE_LENGTH}")

@Suppress("CyclomaticComplexMethod") // плоская таблица проверок по типам настроек
fun Setting.accepts(draft: SettingValue): Boolean = when {
    this is IntegerSetting && draft is SettingValue.Integer -> draft.value in range
    this is DecimalSetting && draft is SettingValue.Decimal -> draft.value in minValue..maxValue
    this is TextSetting && draft is SettingValue.Text -> draft.value.length <= maxLength && matchesPattern(draft.value)
    this is OptionSetting && draft is SettingValue.Option -> draft.index in options.indices
    this is SirenSetting && draft is SettingValue.Siren -> draft.value.number in range
    this is CanCodeSetting && draft is SettingValue.Can -> CAN_CODE_PATTERN.matches(draft.value.code)
    else -> draft::class == value::class
}

/** Текст подходит под шаблон устройства (нет шаблона или он некорректен — подходит). */
fun TextSetting.matchesPattern(text: String): Boolean = validationRegex?.matches(text) ?: true
