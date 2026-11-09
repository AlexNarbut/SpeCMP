package ru.alarmtrade.specmp.feature.device.settings.domain.model

/** Диапазоны — индексы символов (включительно) в [SettingNode.name], [SettingNode.number] и имени канала. */
data class SearchHighlights(
    val name: List<IntRange> = emptyList(),
    val number: List<IntRange> = emptyList(),
    val channel: List<IntRange> = emptyList(),
) {
    val isEmpty: Boolean get() = name.isEmpty() && number.isEmpty() && channel.isEmpty()
}

data class SettingNameMatch(val settingId: Int, val nameHighlights: List<IntRange>)

data class SettingsSearchHit(
    val node: SettingNode,
    val path: List<SettingsSection>,
    val highlights: SearchHighlights = SearchHighlights(),
)

/**
 * Сначала [nameMatches] в их порядке (ранжирование SDK), затем локальные совпадения (название, префикс номера,
 * имя канала) в порядке дерева, без повторов. Путь — всегда из дерева.
 */
fun SettingsSection.search(query: String, nameMatches: List<SettingNameMatch> = emptyList()): List<SettingsSearchHit> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val located = mutableListOf<Pair<SettingNode, List<SettingsSection>>>()
    fun walk(section: SettingsSection, path: List<SettingsSection>) {
        section.children.forEach { child ->
            located += child to path
            if (child is SettingsSection) walk(child, path + child)
        }
    }
    walk(this, emptyList())
    val byId = located.associateBy { it.first.id }

    val hits = LinkedHashMap<Int, SettingsSearchHit>()
    nameMatches.forEach { match ->
        val (node, path) = byId[match.settingId] ?: return@forEach
        if (match.settingId in hits) return@forEach
        val local = node.localHighlights(q)
        val name = match.nameHighlights.ifEmpty { local.name }
        hits[node.id] = SettingsSearchHit(node, path, local.copy(name = name))
    }
    located.forEach { (node, path) ->
        if (node.id in hits) return@forEach
        val local = node.localHighlights(q)
        if (!local.isEmpty) hits[node.id] = SettingsSearchHit(node, path, local)
    }
    return hits.values.toList()
}

/** Подходит ли настройка под запрос поиска: имя, префикс номера или имя канала. Одно правило для поиска и диффа. */
fun SettingNode.matches(query: String): Boolean = !localHighlights(query).isEmpty

fun SettingNode.localHighlights(query: String): SearchHighlights {
    if (query.isEmpty()) return SearchHighlights()
    return SearchHighlights(
        name = name.occurrencesOf(query),
        number = if (number.isNotEmpty() && number.startsWith(query)) listOf(query.indices) else emptyList(),
        channel = channelOrNull?.name?.occurrencesOf(query).orEmpty(),
    )
}

private fun String.occurrencesOf(query: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var from = indexOf(query, ignoreCase = true)
    while (from >= 0) {
        ranges += from until from + query.length
        from = indexOf(query, startIndex = from + query.length, ignoreCase = true)
    }
    return ranges
}
