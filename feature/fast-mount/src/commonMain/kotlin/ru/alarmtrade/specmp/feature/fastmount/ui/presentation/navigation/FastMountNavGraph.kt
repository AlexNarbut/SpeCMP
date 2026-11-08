package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountScreen

/** Поиск блока для быстрого монтажа (только сигнализации). */
@Serializable
@SerialName("fast-mount.FastMountSelectionRoute")
data object FastMountSelectionRoute : NavKey

/** Мастер быстрого монтажа (внутри обёртки сессии устройства). */
@Serializable
@SerialName("fast-mount.FastMountRoute")
data object FastMountRoute : NavKey

/**
 * Мастер быстрого монтажа.
 *
 * @param onExit выход из монтажа (сессия закрывается обёрткой).
 * @param onChooseAnother выход с возвратом к поиску блока.
 * @param onOpenAdvancedMount выход в расширенный монтаж (блок не поддерживает быстрый).
 * @param slots экраны других фич на шагах мастера.
 */
@Composable
fun FastMountEntry(
    onExit: () -> Unit,
    onChooseAnother: () -> Unit,
    onOpenAdvancedMount: () -> Unit,
    slots: FastMountSlots,
) {
    FastMountScreen(
        onExit = onExit,
        onChooseAnother = onChooseAnother,
        onOpenAdvancedMount = onOpenAdvancedMount,
        slots = slots,
    )
}
