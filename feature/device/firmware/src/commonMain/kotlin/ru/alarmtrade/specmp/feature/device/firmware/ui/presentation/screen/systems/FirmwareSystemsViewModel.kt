package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

/** Система в списке: [expanded] — подсистемы видны (раскрыта пользователем или совпала с поиском). */
@Immutable
data class SystemRowUi(val system: FirmwareSystem, val subsystems: List<FirmwareSubsystem>, val expanded: Boolean)

@Immutable
data class FirmwareSystemsState(
    val content: LCEState<List<SystemRowUi>, FirmwareError> = LCEState.Loading,
    val query: String = "",
    val order: SystemsOrder = SystemsOrder.Added
)

/**
 * Выбор подсистемы для архива без блока: все системы базы устройств с подсистемами в выбранном порядке
 * ([SystemsOrder]: как в базе — последние добавленные сверху, или по алфавиту; выбор запоминается); поиск — по
 * названию системы и названию / имени прошивки подсистемы. Без поиска система раскрывается нажатием; с поиском
 * совпавшие системы раскрыты и показывают совпавшие подсистемы (совпала сама система — все её подсистемы).
 */
class FirmwareSystemsViewModel(
    private val repository: FirmwareRepository,
    private val preferences: FirmwareArchivePreferences,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private data class Core(
        val load: LCEState<List<FirmwareSystem>, FirmwareError> = LCEState.Loading,
        val query: String = "",
        val expanded: Set<Long> = emptySet(),
        val order: SystemsOrder
    )

    // Порядок — из настроек сразу: первый кадр списка уже в нём.
    private val core = MutableStateFlow(Core(order = preferences.systemsOrder()))

    // Текст поиска — отдельно и синхронно: список считается в фоне, и поле с ним отставало бы от ввода.
    private val queryText = MutableStateFlow("")

    /** Текст поля поиска — для самого поля (отфильтрованный список — в [state]). */
    val query: StateFlow<String> = queryText.asStateFlow()

    val state: StateFlow<FirmwareSystemsState> = core
        .launchOnFirstStart { safeLaunch { executeWithProceedLock { load() } } }
        .map(::toState)
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), toState(core.value))

    private suspend fun load() {
        core.update { it.copy(load = LCEState.Loading) }
        val response = withContext(dispatchers.io) { repository.systems() }
        core.update {
            it.copy(
                load = when (response) {
                    is Response.Success -> LCEState.Content(response.value)
                    is Response.Error -> LCEState.Error(FirmwareError.from(response.exception))
                }
            )
        }
    }

    fun onRetry() {
        safeLaunch { executeWithProceedLock { load() } }
    }

    fun onQueryChange(query: String) {
        queryText.update { query }
        core.update { it.copy(query = query) }
    }

    /** Порядок списка (меню в шапке): сразу в списке, запоминается между запусками. */
    fun onOrderSelect(order: SystemsOrder) {
        if (!core.updateIf({ it.order != order }) { it.copy(order = order) }) return
        safeLaunch { withContext(dispatchers.io) { preferences.setSystemsOrder(order) } }
    }

    /** Нажатие на систему (без поиска): раскрыть / свернуть подсистемы. */
    fun onToggle(systemId: Long) {
        core.update { state ->
            val expanded = if (systemId in state.expanded) state.expanded - systemId else state.expanded + systemId
            state.copy(expanded = expanded)
        }
    }

    private fun toState(core: Core): FirmwareSystemsState {
        val content = when (val load = core.load) {
            LCEState.None, LCEState.Loading -> LCEState.Loading

            is LCEState.Error -> load

            is LCEState.Content -> LCEState.Content(
                rowsOf(ordered(load.content, core.order), core.query.trim(), core.expanded)
            )
        }
        return FirmwareSystemsState(content = content, query = core.query, order = core.order)
    }

    /** [SystemsOrder.Added] — порядок базы как есть (SDK отдаёт последние добавленные первыми). */
    private fun ordered(systems: List<FirmwareSystem>, order: SystemsOrder): List<FirmwareSystem> = when (order) {
        SystemsOrder.Added -> systems
        SystemsOrder.Alphabet -> systems.sortedBy { it.name.lowercase() }
    }

    private fun rowsOf(systems: List<FirmwareSystem>, query: String, expanded: Set<Long>): List<SystemRowUi> {
        if (query.isEmpty()) return systems.map { SystemRowUi(it, it.subsystems, expanded = it.id in expanded) }
        return systems.mapNotNull { system ->
            val subsystems = if (system.name.contains(query, ignoreCase = true)) {
                system.subsystems
            } else {
                system.subsystems.filter {
                    it.title.contains(query, ignoreCase = true) || it.systemName.contains(query, ignoreCase = true)
                }
            }
            subsystems.takeIf { it.isNotEmpty() }?.let { SystemRowUi(system, it, expanded = true) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
