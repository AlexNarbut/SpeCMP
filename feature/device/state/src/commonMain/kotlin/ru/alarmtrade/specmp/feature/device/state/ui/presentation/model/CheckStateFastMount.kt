package ru.alarmtrade.specmp.feature.device.state.ui.presentation.model

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone

/** Данные чек-листа ведёт мастер быстрого монтажа (автоответы считает SDK): экран их только показывает. */
@Immutable
data class CheckStateFastMount(
    val checklist: CheckStateChecklist,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onSkip: (taskId: String) -> Unit,
    val onAnswer: (taskId: String, option: Int) -> Unit,
    val chrome: CheckStateChrome? = null,
    val nextBusy: Boolean = false,
)

@Immutable
class CheckStateChrome(
    val topBar: @Composable (onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit,
)

/**
 * @property sequential строгий порядок (курсор SDK быстрого монтажа): ответить можно только на [ChecklistTask.active],
 *   остальные незакрытые — «Ожидает»; порядок заданий не меняется.
 */
@Immutable
data class CheckStateChecklist(val tasks: List<ChecklistTask> = emptyList(), val sequential: Boolean = false) {
    val closed: Int get() = tasks.count { it.status != ChecklistStatus.PENDING }
    val failed: Int get() = tasks.count { it.status == ChecklistStatus.FAILED }
    val allClosed: Boolean get() = closed == tasks.size
}

@Immutable
data class ChecklistTask(
    val id: String,
    val text: String,
    val status: ChecklistStatus = ChecklistStatus.PENDING,
    val zone: Zone? = null,
    val guard: Boolean = false,
    val options: List<String> = emptyList(),
    val active: Boolean = true,
) {
    val manual: Boolean get() = options.isNotEmpty()
}

enum class ChecklistStatus {
    PENDING,

    PASSED,

    FAILED,
    SKIPPED,
}
