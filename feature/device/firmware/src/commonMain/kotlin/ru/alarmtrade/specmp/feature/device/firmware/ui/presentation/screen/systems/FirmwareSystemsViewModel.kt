package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

/**
 * Выбор подсистемы для архива без блока: все системы базы устройств с подсистемами в выбранном порядке
 * ([SystemsOrder]: как в базе — последние добавленные сверху, или по алфавиту; выбор запоминается); поиск — по
 * названию системы и названию / имени прошивки подсистемы. Без поиска система раскрывается нажатием; с поиском
 * совпавшие системы раскрыты и показывают совпавшие подсистемы (совпала сама система — все её подсистемы).
 */
class FirmwareSystemsViewModel(
    private val repository: FirmwareRepository,
    private val preferences: FirmwareArchivePreferences,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<FirmwareSystemsState, Nothing>(
    FirmwareSystemsState(order = preferences.systemsOrder.value.getValueIfSuccess() ?: SystemsOrder.Added),
) {

    override suspend fun onStart() {
        combine(select { systems }, select { query }, select { expanded }, select { order }, ::systemRows)
            .flowOn(dispatchers.default)
            .collectIntoState { content -> copy(content = content) }
        executeWithProceedLock { load() }
    }

    private suspend fun load() {
        reduce { copy(systems = LCEState.Loading) }
        val response = withContext(dispatchers.io) { repository.systems() }
        reduce {
            copy(
                systems = when (response) {
                    is Response.Success -> LCEState.Content(response.value)
                    is Response.Error -> LCEState.Error(FirmwareError.from(response.exception))
                },
            )
        }
    }

    fun onRetry() {
        intent { executeWithProceedLock { load() } }
    }

    fun onQueryChange(query: String) {
        reduce { copy(query = query) }
    }

    /** Порядок списка (меню в шапке): сразу в списке, запоминается между запусками. */
    fun onOrderSelect(order: SystemsOrder) {
        if (currentState.order == order) return
        reduce { copy(order = order) }
        intent {
            withContext(dispatchers.io) { preferences.setSystemsOrder(order) }
                .onError { logger.w(it) { "Systems order was not saved" } }
        }
    }

    /** Нажатие на систему (без поиска): раскрыть / свернуть подсистемы. */
    fun onToggle(systemId: Long) {
        reduce { copy(expanded = if (systemId in expanded) expanded - systemId else expanded + systemId) }
    }
}
