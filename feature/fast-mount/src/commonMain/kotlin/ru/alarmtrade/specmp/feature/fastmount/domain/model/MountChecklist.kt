package ru.alarmtrade.specmp.feature.fastmount.domain.model

/**
 * Чек-лист шага «Проверка системы»: все вопросы конфигурации по порядку SDK. Отвечать можно только на
 * [MountTask.active] (курсор SDK), остальные незакрытые — «Ожидает».
 */
data class MountChecklist(val tasks: List<MountTask> = emptyList()) {
    val closed: Int get() = tasks.count { it.status != MountTaskStatus.PENDING }
    val skipped: Int get() = tasks.count { it.status == MountTaskStatus.SKIPPED }
    val failed: Int get() = tasks.count { it.status == MountTaskStatus.FAILED }
    val allClosed: Boolean get() = closed == tasks.size
}

/**
 * Задание проверки.
 *
 * @property auto что отмечает задание автоматически (по телеметрии); `null` — ручное или без отображаемой зоны.
 * @property options варианты ответа ручного задания; пусто — задание автоматическое.
 */
data class MountTask(
    val id: Int,
    val text: String,
    val status: MountTaskStatus,
    val active: Boolean,
    val auto: AutoTaskKind?,
    val options: List<String>
)

enum class MountTaskStatus { PENDING, PASSED, FAILED, SKIPPED }

/** Что проверяет автоматическое задание: охрана или зона кузова/управления (id вопросов SDK 1–13, 46, 47). */
enum class AutoTaskKind {
    GUARD,
    DOOR_FRONT_LEFT,
    DOOR_REAR_LEFT,
    DOOR_FRONT_RIGHT,
    DOOR_REAR_RIGHT,
    HOOD,
    TRUNK,
    IGNITION,
    BRAKE,
    HANDBRAKE;

    companion object {
        /** По id автоматического вопроса SDK (`AutomaticChecksFactory`); неизвестный — `null`. */
        @Suppress("MagicNumber")
        fun of(questionId: Int): AutoTaskKind? = when (questionId) {
            1, 2, 3, 4, 46, 47 -> GUARD
            5 -> DOOR_FRONT_LEFT
            6 -> DOOR_REAR_LEFT
            7 -> DOOR_FRONT_RIGHT
            8 -> DOOR_REAR_RIGHT
            9 -> HOOD
            10 -> TRUNK
            11 -> IGNITION
            12 -> BRAKE
            13 -> HANDBRAKE
            else -> null
        }
    }
}
