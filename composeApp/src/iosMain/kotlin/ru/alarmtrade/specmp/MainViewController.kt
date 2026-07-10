package ru.alarmtrade.specmp

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(
    appVersionName: String = "2.0.40",
    appVersionCode: Int = 240,
) = ComposeUIViewController {
    App(
        appVersionName = appVersionName,
        appVersionCode = appVersionCode,
    )
}
