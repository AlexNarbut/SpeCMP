package ru.alarmtrade.specmp.feature.fastmount.domain.model

/**
 * Вопрос проверки системы из конфигурации. Отвечать можно только на текущий — SDK ведёт вопросы курсором.
 *
 * @property task что сделать монтажнику (`algorithmDescription`).
 * @property expected что должно получиться (`resultDescription`).
 * @property automatic ответ ставит SDK по телеметрии блока (двери, зажигание, охрана…).
 */
data class MountQuestion(
    val id: Int,
    val group: String,
    val task: String,
    val expected: String,
    val automatic: Boolean,
    val answers: List<QuestionAnswer>
)

/** Вариант ответа. `value == 1` — «работает», `0` — ответ при пропуске (если есть). */
data class QuestionAnswer(val text: String, val value: Int) {
    val passed: Boolean get() = value == PASSED_VALUE

    companion object {
        const val PASSED_VALUE = 1
        const val SKIPPED_VALUE = 0
    }
}

/** Текущее состояние теста: вопрос под курсором; [completed] — ответ на последний вопрос дан. */
data class QuestionsState(val current: MountQuestion?, val completed: Boolean)
