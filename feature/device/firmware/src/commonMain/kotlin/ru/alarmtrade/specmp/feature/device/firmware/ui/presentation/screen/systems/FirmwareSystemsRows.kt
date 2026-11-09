package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

internal fun systemRows(
    systems: LCEState<List<FirmwareSystem>, FirmwareError>,
    query: String,
    expanded: Set<Long>,
    order: SystemsOrder,
): LCEState<List<SystemRowUi>, FirmwareError> = when (systems) {
    LCEState.None, LCEState.Loading -> LCEState.Loading
    is LCEState.Error -> systems
    is LCEState.Content -> LCEState.Content(rowsOf(ordered(systems.content, order), query.trim(), expanded))
}

/** [SystemsOrder.Added] — порядок базы как есть (SDK отдаёт последние добавленные первыми). */
private fun ordered(systems: List<FirmwareSystem>, order: SystemsOrder): List<FirmwareSystem> = when (order) {
    SystemsOrder.Added -> systems
    SystemsOrder.Alphabet -> systems.sortedBy { it.name.lowercase() }
}

private fun rowsOf(systems: List<FirmwareSystem>, query: String, expanded: Set<Long>): List<SystemRowUi> {
    if (query.isEmpty()) return systems.map { SystemRowUi(it, it.subsystems, expanded = it.id in expanded) }
    return systems.mapNotNull { system ->
        val subsystems = if (system.name.contains(query, ignoreCase = true)) {
            system.subsystems
        } else {
            system.subsystems.filter {
                it.title.contains(query, ignoreCase = true) || it.systemName.contains(query, ignoreCase = true)
            }
        }
        subsystems.takeIf { it.isNotEmpty() }?.let { SystemRowUi(system, it, expanded = true) }
    }
}
