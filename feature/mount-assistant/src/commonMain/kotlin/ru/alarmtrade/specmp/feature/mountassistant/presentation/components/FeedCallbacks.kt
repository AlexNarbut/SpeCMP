package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingsSourceKind

/** Действия над карточками ленты (кнопок «следующих шагов» в ленте нет); первый аргумент — id карточки. */
internal class FeedCallbacks(
    val onRetry: (Long) -> Unit,
    val onModuleArchive: (moduleId: Long) -> Unit,
    val onModuleFlash: (moduleId: Long) -> Unit,
    val onEditStart: (Long, Int) -> Unit,
    val onDraftChange: (Long, AssistantSettingValue) -> Unit,
    val onEditCancel: (Long) -> Unit,
    val onEditNext: (Long) -> Unit,
    val onOpenInSettings: (Int) -> Unit,
    val onWrite: (Long) -> Unit,
    val onWriteCancel: (Long) -> Unit,
    val onSourcePick: (Long, SettingsSourceKind) -> Unit,
)
