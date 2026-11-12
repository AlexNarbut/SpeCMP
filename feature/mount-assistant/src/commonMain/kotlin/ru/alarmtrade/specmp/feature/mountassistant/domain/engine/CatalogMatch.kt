package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic

data class CatalogMatch(val topics: List<AssistantTopic>, val actions: List<AssistantAction>) {
    val isEmpty: Boolean get() = topics.isEmpty() && actions.isEmpty()
}

private const val MIN_WORD = 3
private const val STEM_LENGTH = 6
private val WordSeparators = Regex("""[\s,.;:!?«»"]+""")

/** Основы слов запроса: слова от трёх букв, не длиннее шести символов. */
fun stemsOf(query: String): List<String> = query.lowercase()
    .split(WordSeparators)
    .filter { it.length >= MIN_WORD }
    .map { it.take(STEM_LENGTH) }

/**
 * Названия и ключевые слова приходят из строк интерфейса: ядро языка не знает. Тема находится по ключевому слову в
 * запросе или по основе в названии, действие — по своей теме или по основе в названии.
 */
fun matchCatalog(
    query: String,
    actionLabels: Map<AssistantAction, String>,
    topicLabels: Map<AssistantTopic, String>,
    keywords: Map<AssistantTopic, List<String>>,
): CatalogMatch {
    val text = query.trim().lowercase()
    val stems = stemsOf(text)
    if (text.isEmpty()) return CatalogMatch(emptyList(), emptyList())
    val topics = AssistantTopic.entries.filter { topic ->
        keywords[topic].orEmpty().any { it.isNotBlank() && it.trim().lowercase() in text } ||
            stems.any { it in topicLabels[topic].orEmpty().lowercase() }
    }
    val actions = AssistantAction.entries.filter { action ->
        action.topic in topics || stems.any { it in actionLabels[action].orEmpty().lowercase() }
    }
    return CatalogMatch(topics, actions)
}

/** Диапазоны подсветки в названии: первое вхождение каждой основы запроса, по порядку в тексте. */
fun stemRanges(text: String, query: String): List<IntRange> {
    val lower = text.lowercase()
    return stemsOf(query).distinct()
        .mapNotNull { stem -> lower.indexOf(stem).takeIf { it >= 0 }?.let { it until it + stem.length } }
        .sortedBy { it.first }
}
