package ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole

/** Экран другой фичи, который открывает ассистент; переводит в маршруты composition root. */
sealed interface AssistantNavigationTarget {
    data object FirmwareUpdate : AssistantNavigationTarget

    /** Сразу в загрузчик и прошить модуль последней версией с сервера (обновление или перепрошивка). */
    data class FirmwareFlash(val moduleId: Long) : AssistantNavigationTarget

    /** Архив конкретного модуля блока; [installed] — версия на блоке, если прочитана. */
    data class FirmwareArchive(val moduleId: Long, val role: FirmwareModuleRole, val installed: String?) :
        AssistantNavigationTarget

    data object BypassLearning : AssistantNavigationTarget

    data object Clone : AssistantNavigationTarget

    /** [focusSettingId] — открыть редактор на настройке; [openLoad] — сразу выбор источника загрузки. */
    data class SettingsEditor(val focusSettingId: Int? = null, val openLoad: Boolean = false) :
        AssistantNavigationTarget

    data object Configurator : AssistantNavigationTarget

    data object CheckState : AssistantNavigationTarget

    data object Vehicles : AssistantNavigationTarget

    data object SystemDevices : AssistantNavigationTarget

    data object Pairing : AssistantNavigationTarget

    /** Сразу форма «Новая заявка» на техпомощь. */
    data object TechRequestNew : AssistantNavigationTarget

    data object TechRequests : AssistantNavigationTarget

    data object SupportChat : AssistantNavigationTarget
}
