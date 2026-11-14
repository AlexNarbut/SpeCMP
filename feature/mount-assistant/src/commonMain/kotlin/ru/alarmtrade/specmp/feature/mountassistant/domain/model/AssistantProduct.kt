package ru.alarmtrade.specmp.feature.mountassistant.domain.model

import kotlinx.datetime.LocalDate

/** Кратко о товаре для карточки журнала: полный экран — «Информация о товаре». */
data class AssistantProduct(
    val id: Long?,
    val name: String,
    val serial: String?,
    val factoryDate: LocalDate?,
    val kit: AssistantProductKit?,
)

data class AssistantProductKit(val name: String?, val components: Int)

/** Товара с таким номером на сервере нет — блок мог быть продан не через систему, это не ошибка. */
class AssistantProductNotFoundException(cause: Throwable? = null) : Exception("Product not found", cause)
