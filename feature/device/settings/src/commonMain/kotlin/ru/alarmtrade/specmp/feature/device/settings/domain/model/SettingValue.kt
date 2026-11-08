package ru.alarmtrade.specmp.feature.device.settings.domain.model

/** Значение настройки: текущее (в [Setting.value]) и новое (для `DeviceSettingsRepository.setValue`). */
sealed interface SettingValue {
    data class Switch(val enabled: Boolean) : SettingValue
    data class Option(val index: Int) : SettingValue
    data class Integer(val value: Int) : SettingValue
    data class Decimal(val value: Float) : SettingValue
    data class Text(val value: String) : SettingValue
    data class Time(val value: TimeOfDay) : SettingValue
    data class AutoStart(val value: AutoStartSchedule) : SettingValue
    data class Siren(val value: SirenTone) : SettingValue
    data class Can(val value: CanCode) : SettingValue
    data class Input(val value: InputValue) : SettingValue
    data class Output(val value: OutputValue) : SettingValue
    data class SwitchOutput(val enabled: Boolean) : SettingValue
}

data class TimeOfDay(val hours: Int, val minutes: Int)

data class AutoStartSchedule(
    val isMorningStartEnabled: Boolean,
    val morningStartTime: TimeOfDay,
    val isEveningStartEnabled: Boolean,
    val eveningStartTime: TimeOfDay,
)

enum class Weekday { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

enum class TextInputType { TEXT, NUMBER, PHONE }

data class SirenTone(val type: SirenToneType, val number: Int)

enum class SirenToneType { TONE, MELODY }

/** Событие, для которого настраивается звук сирены. */
enum class SirenEvent { ARM, DISARM, WARNING, ALARM }

/** CAN-код автомобиля (4 цифры) и, если известен, автомобиль из базы. */
data class CanCode(val code: String, val carId: Int?)

/** Канал входа/выхода: номер, имя (`CH1`, `SIREN`) и провода жгута. */
data class Channel(val number: Int, val name: String, val wires: List<Wire>)

/** Провод: подпись, цвета изоляции (RGB, 1–3; пусто — без цвета) и номер пина разъёма. */
data class Wire(val name: String, val colors: List<Int>, val pin: Int)

/** Функция (логика), которую можно назначить каналу. */
data class ChannelLogic(val id: Int, val name: String, val description: String?)

enum class Polarity { PLUS, MINUS, UNDEFINED }

/** Тип контакта входа: нормально открытый / нормально замкнутый. */
enum class InputContactType { NORMALLY_OPEN, NORMALLY_CLOSED, UNDEFINED }

/** Значение входа: состояние каждой функции по [ChannelLogic.id] и тип контакта. */
data class InputValue(val logicsState: List<Boolean>, val type: InputContactType)

/** Значение выхода: состояние каждой функции по [ChannelLogic.id] и полярность. */
data class OutputValue(val logicsState: List<Boolean>, val polarity: Polarity)

fun InputValue.isEnabled(logic: ChannelLogic): Boolean = logicsState.getOrElse(logic.id) { false }

fun OutputValue.isEnabled(logic: ChannelLogic): Boolean = logicsState.getOrElse(logic.id) { false }
