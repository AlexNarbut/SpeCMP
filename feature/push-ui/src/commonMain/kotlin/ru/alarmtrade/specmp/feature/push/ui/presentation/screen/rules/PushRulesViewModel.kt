package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.push.PushType
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.push.domain.repository.PushRulesRepository
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.PushRulesError

internal class PushRulesViewModel(
    private val repository: PushRulesRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PushRulesState, PushRulesEffect>(PushRulesState()) {

    override suspend fun IntentScope.onStart() {
        load()
    }

    fun onRetry() {
        intent { load() }
    }

    fun onToggle(type: PushType, enabled: Boolean) {
        intent { reduce { if (isSaving) this else copy(selected = selected?.with(type, enabled)) } }
    }

    fun onSave() {
        intent { executeWithProceedLock { save() } }
    }

    private suspend fun load() = subIntent {
        reduce { copy(saved = LCEState.Loading, selected = null) }
        val response = withContext(dispatchers.io) { repository.rules() }
        val saved = response.asLCEState(PushRulesError::from)
        reduce { copy(saved = saved, selected = saved.content) }
    }

    private suspend fun save() = subIntent {
        val rules = currentState.selected ?: return@subIntent
        if (!currentState.canSave) return@subIntent
        reduce { copy(isSaving = true) }
        when (val response = withContext(dispatchers.io) { repository.save(rules) }) {
            is Response.Success -> {
                reduce { copy(saved = LCEState.Content(rules), isSaving = false) }
                postSideEffect(PushRulesEffect.Saved)
            }

            is Response.Error -> {
                reduce { copy(isSaving = false) }
                postSideEffect(PushRulesEffect.SaveFailed(PushRulesError.from(response.exception)))
            }
        }
    }
}
