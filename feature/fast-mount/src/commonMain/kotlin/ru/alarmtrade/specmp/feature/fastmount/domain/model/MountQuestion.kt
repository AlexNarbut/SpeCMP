package ru.alarmtrade.specmp.feature.fastmount.domain.model

/** SDK ведёт вопросы курсором; [automatic] — ответ ставит SDK по телеметрии блока. */
data class MountQuestion(
    val id: Int,
    val group: String,
    val task: String,
    val expected: String,
    val automatic: Boolean,
    val answers: List<QuestionAnswer>,
)

/** Вариант ответа. `value == 1` — «работает», `0` — ответ при пропуске (если есть). */
data class QuestionAnswer(val text: String, val value: Int) {
    val passed: Boolean get() = value == PASSED_VALUE

    companion object {
        const val PASSED_VALUE = 1
        const val SKIPPED_VALUE = 0
    }
}

data class QuestionsState(val current: MountQuestion?, val completed: Boolean)
