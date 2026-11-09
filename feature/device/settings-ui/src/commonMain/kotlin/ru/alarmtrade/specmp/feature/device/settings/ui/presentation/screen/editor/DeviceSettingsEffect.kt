package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/** One-shot события экрана. */
internal sealed interface DeviceSettingsEffect {
    data class Written(val count: Int) : DeviceSettingsEffect
    data object SetValueFailed : DeviceSettingsEffect
    data object ResetDone : DeviceSettingsEffect
    data class Exit(val target: ExitTarget) : DeviceSettingsEffect

    /** Конфигурация подставлена в редактор: [count] изменений (0 — настройки уже совпадали). */
    data class Loaded(val count: Int) : DeviceSettingsEffect

    /** Открыть системный выбор файла настроек. */
    data object PickFile : DeviceSettingsEffect

    /** Отдать готовый файл [path] пользователю через системное сохранение под именем [fileName]. */
    data class ExportFile(val path: String, val fileName: String) : DeviceSettingsEffect

    data class FileSaved(val name: String) : DeviceSettingsEffect

    /** Сохранено в «Мои конфигурации» — тост с «Открыть». */
    data class SavedToMine(val configuration: SettingsSource.LocalConfiguration) : DeviceSettingsEffect

    /** Правки сохранены в источник (конфигурацию, файл). */
    data object Saved : DeviceSettingsEffect

    /** «Записать в блок»: открыть редактор блока с загрузкой [source]. */
    data class WriteToDevice(val source: SettingsSource) : DeviceSettingsEffect
}
