package ru.alarmtrade.specmp

import androidx.compose.ui.window.ComposeUIViewController

// Имя — точка входа для Swift (MainViewControllerKt.MainViewController), PascalCase сохраняется
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(appVersionName: String = "2.0.40", appVersionCode: Int = 240) = ComposeUIViewController {
    App(
        appVersionName = appVersionName,
        appVersionCode = appVersionCode
    )
}
