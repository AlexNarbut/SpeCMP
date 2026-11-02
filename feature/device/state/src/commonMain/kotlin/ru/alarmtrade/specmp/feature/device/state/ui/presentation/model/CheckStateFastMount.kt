package ru.alarmtrade.specmp.feature.device.state.ui.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone

/**
 * Режим «шаг быстрого монтажа»: чек-лист проверок и «Далее». Данные чек-листа ведёт мастер быстрого монтажа
 * (SDK `FastMountInteractor`, автоответы считает SDK) — экран их только показывает и отдаёт ответы.
 *
 * @property onNext шаг завершён («Далее», в т.ч. с незакрытыми заданиями после подтверждения).
 * @property onBack возврат на предыдущий шаг мастера (после подтверждения).
 * @property onSkip пропустить авто-задание.
 * @property onAnswer ответ на ручное задание: индекс варианта в [ChecklistTask.options].
 */
@Immutable
data class CheckStateFastMount(
    val checklist: CheckStateChecklist,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onSkip: (taskId: String) -> Unit,
    val onAnswer: (taskId: String, option: Int) -> Unit
)

@Immutable
data class CheckStateChecklist(val tasks: List<ChecklistTask> = emptyList()) {
    val closed: Int get() = tasks.count { it.status != ChecklistStatus.PENDING }
    val failed: Int get() = tasks.count { it.status == ChecklistStatus.FAILED }
    val allClosed: Boolean get() = closed == tasks.size
}

/**
 * Задание чек-листа.
 *
 * @property zone авто-задание по зоне — рядом показывается текущее значение зоны; `null` — без зоны (охрана, ручное).
 * @property guard авто-задание «поставьте на охрану» — рядом текущий статус охраны.
 * @property options варианты ответа ручного задания (пусто — задание автоматическое).
 */
@Immutable
data class ChecklistTask(
    val id: String,
    val text: String,
    val status: ChecklistStatus = ChecklistStatus.PENDING,
    val zone: Zone? = null,
    val guard: Boolean = false,
    val options: List<String> = emptyList()
) {
    val manual: Boolean get() = options.isNotEmpty()
}

enum class ChecklistStatus {
    PENDING,

    /** Авто-задание отметилось или ответ «да». */
    PASSED,

    /** Ответ «нет — не работает». */
    FAILED,
    SKIPPED
}
