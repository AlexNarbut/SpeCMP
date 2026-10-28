package ru.alarmtrade.specmp.feature.configurator.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * «Моя» конфигурация — в хранилище приложения, видна только текущему пользователю. Привязана только к модели блока
 * ([deviceName], `systemName`, например `DX-9X`), автомобиля нет. [description] может быть пустым.
 */
data class LocalConfiguration(
    val id: Long,
    val name: String,
    val description: String,
    val deviceName: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
) {
    companion object {
        const val NAME_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 500
    }
}

/** Автомобиль облачной конфигурации: марка, модель, комплектация. */
data class ConfigurationCar(val id: Int, val brand: String, val model: String, val trim: String) {
    val title: String get() = listOf(brand, model, trim).filter { it.isNotBlank() }.joinToString(" ")
}

/**
 * Облачная конфигурация (с сервера, только просмотр и копирование) в списке по автомобилю.
 *
 * @property rating оценка из отзывов после быстрого монтажа; `null` — оценок нет (SDK отдаёт `0`).
 * @property onlyForAuthor конфигурация не опубликована и видна только автору.
 */
data class CloudConfigurationSummary(
    val id: Int,
    val name: String,
    val shortDescription: String,
    val deviceName: String,
    val deviceTitle: String,
    val car: ConfigurationCar,
    val author: String,
    val rating: Int?,
    val updated: LocalDateTime,
    val onlyForAuthor: Boolean
)

/** Карточка облачной конфигурации: полное описание (HTML), монтажная карта и вложения. */
data class CloudConfiguration(
    val summary: CloudConfigurationSummary,
    val fullDescriptionHtml: String,
    val mountingManual: ConfigurationAttachment?,
    val attachments: List<ConfigurationAttachment>
)

/** Вложение: картинка (с превью) или PDF. */
data class ConfigurationAttachment(val title: String, val previewUrl: String?, val url: String) {
    val isPdf: Boolean get() = url.substringBefore('?').endsWith(".pdf", ignoreCase = true)
}

/** Порядок облачного списка: «Лучшие» — по оценке, затем по дате; «Новые» — по дате. */
enum class CloudConfigurationOrder { BEST, NEWEST }

fun List<CloudConfigurationSummary>.sortedBy(order: CloudConfigurationOrder): List<CloudConfigurationSummary> =
    when (order) {
        CloudConfigurationOrder.BEST -> sortedWith(
            compareByDescending<CloudConfigurationSummary> { it.rating ?: 0 }.thenByDescending { it.updated }
        )

        CloudConfigurationOrder.NEWEST -> sortedByDescending { it.updated }
    }
