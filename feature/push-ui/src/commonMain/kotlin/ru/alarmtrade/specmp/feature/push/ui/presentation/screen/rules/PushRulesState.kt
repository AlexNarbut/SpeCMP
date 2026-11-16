package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.PushRulesError

/** [saved] — правила на сервере, [selected] — отмеченные на экране (уходят по «Сохранить»). */
@Immutable
internal data class PushRulesState(
    val saved: LCEState<PushRules, PushRulesError> = LCEState.None,
    val selected: PushRules? = null,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean get() = !isSaving && selected != null && selected != saved.content
}
