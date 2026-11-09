package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountScreen

@Serializable
@SerialName("fast-mount.FastMountSelectionRoute")
data object FastMountSelectionRoute : NavKey

@Serializable
@SerialName("fast-mount.FastMountRoute")
data object FastMountRoute : NavKey

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
