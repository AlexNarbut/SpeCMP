package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents

import androidx.compose.runtime.Immutable

/** Режим выбора для экспорта: строка отмечается нажатием, вместо «⋯» — галочка. */
@Immutable
internal class PassportRowPick(val picked: Boolean, val onToggle: () -> Unit)
