package ru.alarmtrade.specmp.feature.techrequests.domain.model

enum class TechRequestStatus(val isActive: Boolean) {
    OPEN(isActive = true),
    IN_PROCESS(isActive = true),
    SUSPENDED(isActive = true),
    FINISHED(isActive = false),
    FINISHED_BY_USER(isActive = false),
    CANCELED_BY_USER(isActive = false),
}
