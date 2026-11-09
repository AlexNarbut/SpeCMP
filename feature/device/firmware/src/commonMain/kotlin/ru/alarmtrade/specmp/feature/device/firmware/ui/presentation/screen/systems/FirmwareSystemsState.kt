package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SystemsOrder
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

/** Система в списке: [expanded] — подсистемы видны (раскрыта пользователем или совпала с поиском). */
@Immutable
data class SystemRowUi(val system: FirmwareSystem, val subsystems: List<FirmwareSubsystem>, val expanded: Boolean)

/**
 * [content] — список для экрана, считается из [systems], [query], [expanded] и [order] в фоне; [query] меняется
 * синхронно с вводом, поэтому поле поиска не отстаёт от списка.
 */
@Immutable
data class FirmwareSystemsState(
    val content: LCEState<List<SystemRowUi>, FirmwareError> = LCEState.Loading,
    val query: String = "",
    val order: SystemsOrder = SystemsOrder.Added,
    val systems: LCEState<List<FirmwareSystem>, FirmwareError> = LCEState.Loading,
    val expanded: Set<Long> = emptySet(),
)
