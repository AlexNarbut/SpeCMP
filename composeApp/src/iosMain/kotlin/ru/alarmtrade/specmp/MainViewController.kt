package ru.alarmtrade.specmp

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import kotlin.experimental.ExperimentalNativeApi

// Имя — точка входа для Swift (MainViewControllerKt.MainViewController), PascalCase сохраняется
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(appVersionName: String = "2.0.40", appVersionCode: Int = 240) = ComposeUIViewController(
    configure = { enableDebugConsoleLogs() }
) {
    App(
        appVersionName = appVersionName,
        appVersionCode = appVersionCode
    )
}

/**
 * Отладочная сборка: логи Kermit дублируются в stdout. Системный журнал iOS не сохраняет debug-сообщения, а stdout
 * видно при запуске с Mac (`xcrun devicectl device process launch --console …`) и в консоли Xcode.
 */
@OptIn(ExperimentalNativeApi::class)
private fun enableDebugConsoleLogs() {
    if (Platform.isDebugBinary) Logger.setLogWriters(platformLogWriter(), CommonWriter())
}
