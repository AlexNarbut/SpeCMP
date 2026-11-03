package ru.alarmtrade.specmp.feature.device.state.ui.presentation.model

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
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
 * @property chrome шапка и нижняя панель мастера (экран — шаг мастера в его каркасе); `null` — свои.
 * @property nextBusy мастер ждёт ответа на «Далее» — кнопка недоступна.
 */
@Immutable
data class CheckStateFastMount(
    val checklist: CheckStateChecklist,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onSkip: (taskId: String) -> Unit,
    val onAnswer: (taskId: String, option: Int) -> Unit,
    val chrome: CheckStateChrome? = null,
    val nextBusy: Boolean = false
)

/** Шапка ([topBar] с «Назад» экрана) и нижняя панель ([dock]) мастера быстрого монтажа. */
@Immutable
class CheckStateChrome(
    val topBar: @Composable (onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit
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

/**
 * Задание чек-листа.
 *
 * @property zone авто-задание по зоне — рядом показывается текущее значение зоны; `null` — без зоны (охрана, ручное).
 * @property guard авто-задание «поставьте на охрану» — рядом текущий статус охраны.
 * @property options варианты ответа ручного задания (пусто — задание автоматическое).
 * @property active текущее задание в строгом порядке ([CheckStateChecklist.sequential]).
 */
@Immutable
data class ChecklistTask(
    val id: String,
    val text: String,
    val status: ChecklistStatus = ChecklistStatus.PENDING,
    val zone: Zone? = null,
    val guard: Boolean = false,
    val options: List<String> = emptyList(),
    val active: Boolean = true
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
