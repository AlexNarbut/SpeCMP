package ru.alarmtrade.specmp.feature.device.settings.domain.model

fun SettingsSection.findNode(id: Int): SettingNode? {
    if (this.id == id) return this
    for (child in children) {
        when {
            child.id == id -> return child
            child is SettingsSection -> child.findNode(id)?.let { return it }
            else -> Unit
        }
    }
    return null
}

fun SettingsSection.pathTo(id: Int): List<SettingsSection>? {
    for (child in children) {
        if (child.id == id) return emptyList()
        if (child is SettingsSection) {
            child.pathTo(id)?.let { return listOf(child) + it }
        }
    }
    return null
}

fun SettingsSection.settings(): List<Setting> = children.flatMap { child ->
    when (child) {
        is SettingsSection -> child.settings()
        is Setting -> listOf(child)
    }
}

fun SettingsSection.replace(setting: Setting): SettingsSection = copy(
    children = children.map { child ->
        when {
            child.id == setting.id -> setting
            child is SettingsSection -> child.replace(setting)
            else -> child
        }
    },
)

/** Равные узлы (по id) — из [old]: неизменённые строки не перерисовываются; ничего не изменилось — сам [old]. */
fun SettingsSection.reusing(old: SettingsSection): SettingsSection {
    val oldById = old.children.associateBy { it.id }
    val reused = children.map { child ->
        when (val prev = oldById[child.id]) {
            null -> child
            is SettingsSection -> if (child is SettingsSection) child.reusing(prev) else child
            else -> if (child == prev) prev else child
        }
    }
    val sameChildren = reused.size == old.children.size && reused.indices.all { reused[it] === old.children[it] }
    return if (sameChildren && copy(children = old.children) == old) old else copy(children = reused)
}

data class SettingsSearchHit(val node: SettingNode, val path: List<SettingsSection>)

fun SettingsSection.search(query: String): List<SettingsSearchHit> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val hits = mutableListOf<SettingsSearchHit>()
    fun walk(section: SettingsSection, path: List<SettingsSection>) {
        section.children.forEach { child ->
            if (child.matches(q)) hits += SettingsSearchHit(child, path)
            if (child is SettingsSection) walk(child, path + child)
        }
    }
    walk(this, emptyList())
    return hits
}

/** Подходит ли настройка под запрос поиска: имя, префикс номера или имя канала. Одно правило для поиска и диффа. */
fun SettingNode.matches(query: String): Boolean = name.contains(query, ignoreCase = true) ||
    (number.isNotEmpty() && number.startsWith(query)) ||
    channelOrNull?.name?.contains(query, ignoreCase = true) == true
