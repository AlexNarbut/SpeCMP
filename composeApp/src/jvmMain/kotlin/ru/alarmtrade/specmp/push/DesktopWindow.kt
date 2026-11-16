package ru.alarmtrade.specmp.push

import java.awt.Frame

internal object DesktopWindow {

    fun current(): Frame? = Frame.getFrames().firstOrNull { it.isDisplayable && it.isVisible }

    fun bringToFront() {
        current()?.apply {
            if (extendedState and Frame.ICONIFIED != 0) extendedState = extendedState and Frame.ICONIFIED.inv()
            isVisible = true
            toFront()
            requestFocus()
        }
    }
}
