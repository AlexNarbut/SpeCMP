package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.matchesModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.modelOptions

@Immutable
internal data class MineState(
    val list: LCEState<List<LocalConfiguration>, ConfiguratorError> = LCEState.Loading,
    val query: String = "",
    /** Фильтр по модели блока; `null` — все модели. */
    val modelFilter: String? = null,
    /** Сколько показать (порции при прокрутке). */
    val shown: Int = MineViewModel.PAGE_SIZE,
    /** Выбранная (широкое окно — карточка справа). */
    val selectedId: Long? = null,
    val dialog: MineDialog? = null,
    val busy: Boolean = false,
    /** Модель подключённого блока: «Записать в блок» только при связи. */
    val connectedDeviceName: String? = null,
    val pickMode: Boolean = false,
    /** Модели, которые есть в списке, — варианты фильтра. Производное — [withLists]. */
    val models: List<String> = emptyList(),
    /** Список после поиска и фильтра по модели. Производное — [withLists]. */
    val filtered: List<LocalConfiguration> = emptyList()
) {
    val all: List<LocalConfiguration> get() = list.content.orEmpty()

    val selected: LocalConfiguration? get() = selectedId?.let { id -> all.firstOrNull { it.id == id } }
}

internal sealed interface MineDialog {
    /** «+ Создать»: с нуля / из облака / из файла. */
    data object Create : MineDialog

    /** Меню ⋮ карточки (шторка на телефоне). */
    data class Menu(val item: LocalConfiguration) : MineDialog

    data class Rename(val item: LocalConfiguration, val name: String, val description: String) : MineDialog

    data class Delete(val item: LocalConfiguration) : MineDialog
}

internal enum class MineMessage { COPIED, DELETED, RENAMED, FAILED }

/**
 * Производные списки — фильтр, поиск, варианты моделей. Считаются во VM в `combine` на `default` (один раз на изменение
 * state), а не геттером на каждую рекомпозицию на Main.
 */
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
        }
    )
}
