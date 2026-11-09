package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
class AutostartStepChrome(
    val topBar: @Composable (locked: Boolean, onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit,
)

@Immutable
class AutostartStepNext(val busy: Boolean, val error: String?)
