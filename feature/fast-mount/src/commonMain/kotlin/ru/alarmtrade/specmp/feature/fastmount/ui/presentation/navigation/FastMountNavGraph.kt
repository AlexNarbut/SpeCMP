package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountScreen

/** App-level ключ: поиск блока для быстрого монтажа (только сигнализации). Регистрируется в графе `composeApp`. */
@Serializable
@SerialName("fast-mount.FastMountSelectionRoute")
data object FastMountSelectionRoute : NavKey

/** App-level ключ: мастер быстрого монтажа с подключённым блоком (внутри `DeviceInteractionHost`). */
@Serializable
@SerialName("fast-mount.FastMountRoute")
data object FastMountRoute : NavKey

/**
 * Мастер быстрого монтажа — контент `DeviceInteractionHost` в `composeApp` (потеря связи, переподключение и экран
 * загрузчика — у обёртки).
 *
 * @param onExit выход из монтажа: обёртка закрывает сессию.
 * @param onChooseAnother блок не подходит — выход с возвратом к поиску.
 */
@Composable
fun FastMountEntry(onExit: () -> Unit, onChooseAnother: () -> Unit) {
    FastMountScreen(onExit = onExit, onChooseAnother = onChooseAnother)
}
