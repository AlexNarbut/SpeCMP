package ru.alarmtrade.specmp.feature.device.settings.domain.model

// «Что изменится» перед загрузкой конфигурации: сравнение двух меню одной модели по id настроек. Чистые функции.

/** Настройка, значение которой отличается: [before] — в текущем меню, [after] — в загружаемом. */
data class SettingChange(val before: Setting, val after: Setting)

/** Изменения внутри раздела первого уровня; [section] `null` — настройки на корневом уровне меню. */
data class SectionChanges(val section: SettingsSection?, val changes: List<SettingChange>)

/**
 * Итог сравнения: [canCode] — изменение CAN-кода (показывается первым, отдельно от разделов: от него зависит прошивка),
 * [sections] — остальные изменения по разделам первого уровня, [sectionCount] — во скольких разделах есть изменения
 * (вместе с разделом CAN-кода).
 */
data class SettingsDiff(val canCode: SettingChange?, val sections: List<SectionChanges>, val sectionCount: Int) {
    val total: Int get() = sections.sumOf { it.changes.size } + if (canCode != null) 1 else 0
    val isEmpty: Boolean get() = total == 0
}

/** Изменения, подходящие под запрос (по настройке «после», правило — [SettingNode.matches]); пустой — все. */
fun SettingsDiff.filter(query: String): SettingsDiff {
    val q = query.trim()
    if (q.isEmpty()) return this
    return copy(
        canCode = canCode?.takeIf { it.after.matches(q) },
        sections = sections.map { section -> section.copy(changes = section.changes.filter { it.after.matches(q) }) }
            .filter { it.changes.isNotEmpty() },
    )
}

/**
 * Что изменится, если заменить значения этого меню значениями [next]. Сравниваются настройки с одинаковым id;
 * настройки, которых нет в одном из меню, не учитываются. Порядок — порядок этого меню, корневые настройки — первыми.
 */
fun SettingsSection.diffTo(next: SettingsSection): SettingsDiff {
    val after = next.settings().associateBy { it.id }
    fun changesIn(settings: List<Setting>) = settings.mapNotNull { before ->
        after[before.id]?.takeIf { it.value != before.value }?.let { SettingChange(before, it) }
    }
    val groups = buildList {
        add(SectionChanges(null, changesIn(children.filterIsInstance<Setting>())))
        children.filterIsInstance<SettingsSection>().forEach { add(SectionChanges(it, changesIn(it.settings()))) }
    }.filter { it.changes.isNotEmpty() }
    val canCode = groups.firstNotNullOfOrNull { group -> group.changes.firstOrNull { it.after is CanCodeSetting } }
    val sections = groups
        .map { group -> group.copy(changes = group.changes.filter { it !== canCode }) }
        .filter { it.changes.isNotEmpty() }
    return SettingsDiff(canCode = canCode, sections = sections, sectionCount = groups.size)
}

/** Функции канала: включённые загрузкой ([added]) и выключенные ([removed]) — «+ Капот, − Багажник». */
data class ChannelLogicsChange(val added: List<ChannelLogic>, val removed: List<ChannelLogic>)

/** Как меняются функции входа/выхода; для других настроек — `null`. */
fun SettingChange.logicsChange(): ChannelLogicsChange? {
    val logics = when (val setting = after) {
        is InputSetting -> setting.availableLogics
        is OutputSetting -> setting.availableLogics
        else -> return null
    }
    val was = before.logicsState() ?: return null
    val now = after.logicsState() ?: return null
    fun List<Boolean>.on(logic: ChannelLogic) = getOrElse(logic.id) { false }
    return ChannelLogicsChange(
        added = logics.filter { now.on(it) && !was.on(it) },
        removed = logics.filter { was.on(it) && !now.on(it) },
    )
}

private fun Setting.logicsState(): List<Boolean>? = when (val value = value) {
    is SettingValue.Input -> value.value.logicsState
    is SettingValue.Output -> value.value.logicsState
    else -> null
}
