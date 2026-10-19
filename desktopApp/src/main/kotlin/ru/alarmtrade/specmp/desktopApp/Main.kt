package ru.alarmtrade.specmp.desktopApp

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.alarmtrade.specmp.App

fun main() = application {
    val (versionName, versionCode) = loadDesktopAppVersion()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp
        ),
        title = "SpeCMP",
        icon = painterResource("icon.png")
    ) {
        App(
            appVersionName = versionName,
            appVersionCode = versionCode
        )
    }
}

private fun loadDesktopAppVersion(): Pair<String, Int> = try {
    val properties = java.util.Properties().apply {
        val stream = Thread.currentThread().contextClassLoader
            ?.getResourceAsStream("version.properties")
        if (stream != null) {
            load(stream)
        }
    }
    val versionName = properties.getProperty("versionName", "2.0.40")
    val versionCode = properties.getProperty("versionCode", "240").toIntOrNull() ?: 240
    versionName to versionCode
} catch (e: Throwable) {
    "2.0.40" to 240
}
