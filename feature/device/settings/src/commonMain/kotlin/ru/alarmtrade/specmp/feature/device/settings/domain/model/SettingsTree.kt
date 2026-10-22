package ru.alarmtrade.specmp.feature.device.settings.domain.model

// Операции над деревом меню: поиск узлов, путь, замена настройки, поиск по тексту. Чистые, без побочных эффектов.

/** Узел с [id] на любой глубине (сам раздел — тоже), или `null`. */
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

/** Разделы от первого уровня до родителя узла [id] включительно (корень не входит); `null` — узла нет. */
fun SettingsSection.pathTo(id: Int): List<SettingsSection>? {
    for (child in children) {
        if (child.id == id) return emptyList()
        if (child is SettingsSection) {
            child.pathTo(id)?.let { return listOf(child) + it }
        }
    }
    return null
}

/** Все настройки-листья раздела на любой глубине. */
fun SettingsSection.settings(): List<Setting> = children.flatMap { child ->
    when (child) {
        is SettingsSection -> child.settings()
        is Setting -> listOf(child)
    }
}

/** Сколько настроек раздела (на любой глубине) входит в [ids] — счётчик изменений раздела. */
fun SettingsSection.countIn(ids: Set<Int>): Int = settings().count { it.id in ids }

/** Дерево, в котором настройка с тем же id заменена на [setting]; без совпадения — то же дерево. */
fun SettingsSection.replace(setting: Setting): SettingsSection = copy(
    children = children.map { child ->
        when {
            child.id == setting.id -> setting
            child is SettingsSection -> child.replace(setting)
            else -> child
        }
    }
)

/** Результат поиска: найденный узел и разделы до него (для подписи пути). */
data class SettingsSearchHit(val node: SettingNode, val path: List<SettingsSection>)

/**
 * Поиск по названию (подстрока без учёта регистра), номеру (префикс: `3.2` найдёт `3.2.4`) и имени канала
 * входа/выхода. Корень не возвращается; порядок — порядок меню. Пустой запрос — пустой результат.
 */
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

private fun SettingNode.matches(query: String): Boolean = name.contains(query, ignoreCase = true) ||
    (number.isNotEmpty() && number.startsWith(query)) ||
    channelName?.contains(query, ignoreCase = true) == true

private val SettingNode.channelName: String?
    get() = when (this) {
        is InputSetting -> channel.name
        is OutputSetting -> channel.name
        is SwitchOutputSetting -> channel.name
        else -> null
    }
