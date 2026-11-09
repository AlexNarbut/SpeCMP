package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings

/**
 * Учёт изменённых настроек редактора (неизменяемый, хранится в state; без I/O и корутин — тестируется напрямую):
 * - «как было» ([withBaseline]) — значения после чтения меню или записи;
 * - «загружено» ([withLoaded]) — изменённое последней загрузкой конфигурации: SDK такие правки в `unsavedIds`
 *   не отмечает;
 * - «изменено» ([changesOf]) — отличается от «как было» и при этом есть в `unsavedIds` SDK или загружено. Возврат
 *   исходного значения снимает подсветку, даже если SDK ещё считает настройку несохранённой.
 *
 * Обход дерева — линейный по числу настроек; VM вызывает на `default`.
 */
internal data class SettingsChangeTracker(
    private val baseline: Map<Int, SettingValue> = emptyMap(),
    private val loaded: Set<Int> = emptySet(),
) {

    /** Значение «как было» настройки [settingId]. */
    fun original(settingId: Int): SettingValue? = baseline[settingId]

    /** Новый снимок «как было» — текущее меню [root]. */
    fun withBaseline(root: SettingsSection): SettingsChangeTracker =
        copy(baseline = root.settings().associate { it.id to it.value })

    fun withoutLoaded(): SettingsChangeTracker = copy(loaded = emptySet())

    /** Загрузка конфигурации: «загружено» — всё, что в [root] отличается от «как было». */
    fun withLoaded(root: SettingsSection): SettingsChangeTracker =
        copy(loaded = root.settings().differingFrom(baseline))

    /** Изменённые настройки [root] (id) и их число по разделам; [unsaved] `null` — SDK не ответил: все отличающиеся. */
    fun changesOf(root: SettingsSection, settings: List<Setting>, unsaved: Set<Int>?): Changes {
        val differing = settings.differingFrom(baseline)
        val changed = if (unsaved == null) differing else differing.intersect(unsaved + loaded)
        return Changes(changed, root.sectionChanges(changed))
    }

    data class Changes(val ids: Set<Int>, val bySection: Map<Int, Int>)
}

private fun List<Setting>.differingFrom(baseline: Map<Int, SettingValue>): Set<Int> =
    filter { it.value != baseline[it.id] }.mapTo(mutableSetOf()) { it.id }

/** Число изменённых настроек в каждом разделе дерева (на любой глубине); разделы без изменений не входят. */
private fun SettingsSection.sectionChanges(changed: Set<Int>): Map<Int, Int> {
    val result = mutableMapOf<Int, Int>()
    fun count(section: SettingsSection): Int {
        var total = 0
        section.children.forEach { child ->
            total += when (child) {
                is SettingsSection -> count(child)
                is Setting -> if (child.id in changed) 1 else 0
            }
        }
        if (total > 0) result[section.id] = total
        return total
    }
    count(this)
    return result
}
