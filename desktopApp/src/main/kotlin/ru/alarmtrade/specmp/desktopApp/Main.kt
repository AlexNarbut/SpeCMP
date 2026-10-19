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

private const val DEFAULT_VERSION_NAME = "2.0.40"
private const val DEFAULT_VERSION_CODE = 240

private fun loadDesktopAppVersion(): Pair<String, Int> = try {
    val properties = java.util.Properties().apply {
        val stream = Thread.currentThread().contextClassLoader
            ?.getResourceAsStream("version.properties")
        if (stream != null) {
            load(stream)
        }
    }
    val versionName = properties.getProperty("versionName", DEFAULT_VERSION_NAME)
    val versionCode = properties.getProperty("versionCode", DEFAULT_VERSION_CODE.toString()).toIntOrNull()
        ?: DEFAULT_VERSION_CODE
    versionName to versionCode
} catch (ignored: Throwable) {
    DEFAULT_VERSION_NAME to DEFAULT_VERSION_CODE
}
