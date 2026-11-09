package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError

@Immutable
internal data class ModelsState(
    val list: LCEState<List<String>, ConfiguratorError> = LCEState.Loading,
    /** Имя файла — подзаголовок; `null` — «С нуля». */
    val fileName: String? = null,
    val recent: List<String> = emptyList(),
    val connected: String? = null,
    val query: String = "",
    val checking: Boolean = false,
    val fileError: String? = null,
)
