package ru.alarmtrade.specmp.core.ui.view.effect

internal actual val isHazeBlurSupported: Boolean =
    !System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)
