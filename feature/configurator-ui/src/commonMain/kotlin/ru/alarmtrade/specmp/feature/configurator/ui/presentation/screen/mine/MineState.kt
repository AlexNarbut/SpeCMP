package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.matchesModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.modelOptions
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

@Immutable
internal data class MineState(
    val list: LCEState<List<LocalConfiguration>, ConfiguratorError> = LCEState.Loading,
    val query: String = "",
    /** Фильтр по модели блока; `null` — все модели. */
    val modelFilter: String? = null,
    val shown: Int = PAGE_SIZE,
    val selectedId: Long? = null,
    val dialog: MineDialog? = null,
    val busy: Boolean = false,
    val connectedDeviceName: String? = null,
    val pickMode: Boolean = false,
    val models: List<String> = emptyList(),
    val filtered: List<LocalConfiguration> = emptyList(),
    /** Снимок «Сохранить в файл», отданный системному сохранению; удаляется по его результату или с экраном. */
    val exportSnapshot: SettingsSource.File? = null,
) {
    val all: List<LocalConfiguration> get() = list.content.orEmpty()

    val selected: LocalConfiguration? get() = selectedId?.let { id -> all.firstOrNull { it.id == id } }

    companion object {
        const val PAGE_SIZE = 20
    }
}

internal sealed interface MineDialog {
    data object Create : MineDialog

    data class Menu(val item: LocalConfiguration) : MineDialog

    data class Rename(val item: LocalConfiguration, val name: String, val description: String) : MineDialog

    data class Delete(val item: LocalConfiguration) : MineDialog
}

internal enum class MineMessage { COPIED, DELETED, RENAMED, FAILED }

/** Считаются во VM одним `reduce` с изменением входов — список и производные не расходятся ни на кадр. */
internal fun MineState.withLists(): MineState {
    val q = query.trim()
    return copy(
        models = all.modelOptions { it.deviceName },
        filtered = all.filter { item ->
            matchesModel(item.deviceName, modelFilter) &&
                (
                    q.isEmpty() || item.name.contains(q, ignoreCase = true) ||
                        item.description.contains(q, ignoreCase = true)
                    )
        },
    )
}
