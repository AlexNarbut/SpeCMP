package ru.alarmtrade.specmp.desktopApp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.alarmtrade.specmp.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
        ),
        title = "SpeCMP",
    ) {
        App()
    }
}
