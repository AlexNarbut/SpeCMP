package ru.alarmtrade.specmp.feature.mountassistant.domain.model

/** [path] — названия разделов от корня; [editor] `null` — тип правится только в редакторе настроек. */
data class FoundSetting(
    val id: Int,
    val name: String,
    val path: List<String>,
    val nameHighlights: List<IntRange>,
    val value: AssistantSettingValue?,
    val editor: SettingEditor?,
)

sealed interface SettingEditor {
    data class Switch(val onTitle: String?, val offTitle: String?) : SettingEditor

    data class Options(val options: List<String>) : SettingEditor

    data class Number(val range: IntRange, val step: Int, val units: String?) : SettingEditor
}

sealed interface AssistantSettingValue {
    data class Switch(val enabled: Boolean) : AssistantSettingValue

    data class Option(val index: Int) : AssistantSettingValue

    data class Number(val value: Int) : AssistantSettingValue
}

enum class WriteFailure { WRONG_PIN, FAILED }

class AssistantWriteException(val failure: WriteFailure, cause: Throwable? = null) :
    Exception("Setting write failed: $failure", cause)

/** Откуда загрузить настройки: выбор продолжается в редакторе. */
enum class SettingsSourceKind { FILE, MINE, CLOUD, DEFAULTS }

/**
 * «Вариант бесключевого обхода» в меню настроек блока. Id — из SDK (`SettingsId.AUTOSTART_NO_KEY_CRAWLER_VARIANT_ID`,
 * там он `internal`): по нему, а не по названию, настройка находится на любом языке; у блоков без неё ответа нет.
 */
const val BYPASS_VARIANT_SETTING_ID: Int = 233
