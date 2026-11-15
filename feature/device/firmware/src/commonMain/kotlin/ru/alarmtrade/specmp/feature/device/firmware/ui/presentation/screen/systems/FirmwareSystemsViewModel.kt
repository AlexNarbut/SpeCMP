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

class FirmwareSystemsViewModel(
    private val repository: FirmwareRepository,
    private val preferences: FirmwareArchivePreferences,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<FirmwareSystemsState, Nothing>(
    FirmwareSystemsState(order = preferences.systemsOrder.value.getValueIfSuccess() ?: SystemsOrder.Added),
) {

    override suspend fun IntentScope.onStart() {
        combine(select { systems }, select { query }, select { expanded }, select { order }, ::systemRows)
            .flowOn(dispatchers.default)
            .collectIntoState { content -> copy(content = content) }
        executeWithProceedLock { load() }
    }

    private suspend fun load() = subIntent {
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
        intent {
            reduce { copy(query = query) }
        }
    }

    fun onOrderSelect(order: SystemsOrder) {
        intent {
            if (currentState.order == order) return@intent
            reduce { copy(order = order) }
            withContext(dispatchers.io) { preferences.setSystemsOrder(order) }
                .onError { logger.w(it) { "Systems order was not saved" } }
        }
    }

    fun onToggle(systemId: Long) {
        intent {
            reduce { copy(expanded = if (systemId in expanded) expanded - systemId else expanded + systemId) }
        }
    }
}
