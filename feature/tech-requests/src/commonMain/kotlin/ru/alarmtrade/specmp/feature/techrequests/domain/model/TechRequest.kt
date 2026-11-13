package ru.alarmtrade.specmp.feature.techrequests.domain.model

import kotlinx.datetime.Instant

/** Заявка на техпомощь. [id] — номер заявки на сервере, он же показывается пользователю. */
data class TechRequest(
    val id: Long,
    val status: TechRequestStatus,
    val userName: String,
    val userPhone: String,
    val issue: String,
    val createdAt: Instant,
)
