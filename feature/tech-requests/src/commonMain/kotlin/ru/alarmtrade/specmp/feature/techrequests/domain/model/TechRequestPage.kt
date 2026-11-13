package ru.alarmtrade.specmp.feature.techrequests.domain.model

/** Страница списка. [hasMore] даёт источник (по `total` сервера или по неполной странице). */
data class TechRequestPage(val items: List<TechRequest>, val hasMore: Boolean)
