package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings

/**
 * «Изменено» — отличается от «как было» и есть в `unsavedIds` SDK или загружено ([withLoaded]: SDK загруженное не
 * отмечает). Возврат исходного значения снимает подсветку, даже если SDK ещё считает настройку несохранённой.
 */
internal data class SettingsChangeTracker(
    private val baseline: Map<Int, SettingValue> = emptyMap(),
    private val loaded: Set<Int> = emptySet(),
) {

    fun original(settingId: Int): SettingValue? = baseline[settingId]

    fun withBaseline(root: SettingsSection): SettingsChangeTracker =
        copy(baseline = root.settings().associate { it.id to it.value })

    fun withoutLoaded(): SettingsChangeTracker = copy(loaded = emptySet())

    fun withLoaded(root: SettingsSection): SettingsChangeTracker =
        copy(loaded = root.settings().differingFrom(baseline))

    /** [unsaved] `null` — SDK не ответил: изменены все отличающиеся. */
    fun changesOf(root: SettingsSection, settings: List<Setting>, unsaved: Set<Int>?): Changes {
        val differing = settings.differingFrom(baseline)
        val changed = if (unsaved == null) differing else differing.intersect(unsaved + loaded)
        return Changes(changed, root.sectionChanges(changed))
    }

    data class Changes(val ids: Set<Int>, val bySection: Map<Int, Int>)
}

private fun List<Setting>.differingFrom(baseline: Map<Int, SettingValue>): Set<Int> =
    filter { it.value != baseline[it.id] }.mapTo(mutableSetOf()) { it.id }

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
