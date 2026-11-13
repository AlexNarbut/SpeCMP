package ru.alarmtrade.specmp.feature.techrequests.presentation.platform

/** Подсказки сочетаний клавиш (Ctrl+Enter, Esc) — только там, где клавиатура физическая (desktop). */
internal expect fun showsKeyboardHints(): Boolean
