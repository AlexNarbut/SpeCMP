package ru.alarmtrade.specmp.feature.device.autostart.domain.model

/** Что блок сообщает об автозапуске без запуска процедур: [bypassLearned] — последнее обучение обхода завершилось. */
data class AutostartState(val bypassLearned: Boolean, val clone: CloneCondition)

enum class CloneCondition { NOT_SUPPORTED, REQUIRED, COMPLETED, FAILED }
