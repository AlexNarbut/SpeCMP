package ru.alarmtrade.specmp

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import kotlin.experimental.ExperimentalNativeApi
import ru.alarmtrade.common.logger.FixedLogLevel
import ru.alarmtrade.common.logger.KmLogging
import ru.alarmtrade.common.logger.LogLevelController
import ru.alarmtrade.common.logger.Logger as SdkLogger
import ru.alarmtrade.common.logger.PlatformLogger

// Имя — точка входа для Swift (MainViewControllerKt.MainViewController), PascalCase сохраняется
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(appVersionName: String, appVersionCode: Int) = ComposeUIViewController(
    configure = { enableDebugConsoleLogs() }
) {
    App(appVersionName = appVersionName, appVersionCode = appVersionCode)
}

/**
 * Отладочная сборка: логи Kermit и логи SDK (KmLogging) дублируются в stdout. Системный журнал iOS не сохраняет
 * debug-сообщения, а stdout видно при запуске с Mac (`xcrun devicectl device process launch --console …`) и в
 * консоли Xcode.
 */
@OptIn(ExperimentalNativeApi::class)
private fun enableDebugConsoleLogs() {
    if (!Platform.isDebugBinary) return
    Logger.setLogWriters(platformLogWriter(), CommonWriter())
    KmLogging.setLoggers(PlatformLogger(FixedLogLevel(true)), ConsoleSdkLogger)
}

/** Логгер SDK в stdout (только отладка). */
// stdout здесь и есть цель (см. enableDebugConsoleLogs): PlatformLogger SDK уже пишет в системный журнал.
@Suppress("ForbiddenPrintln")
private object ConsoleSdkLogger : SdkLogger, LogLevelController by FixedLogLevel(true) {
    override fun verbose(tag: String, msg: String) = println("SDK V/$tag: $msg")

    override fun debug(tag: String, msg: String) = println("SDK D/$tag: $msg")

    override fun info(tag: String, msg: String) = println("SDK I/$tag: $msg")

    override fun warn(tag: String, msg: String, t: Throwable?) =
        println("SDK W/$tag: $msg ${t?.stackTraceToString().orEmpty()}")

    override fun error(tag: String, msg: String, t: Throwable?) =
        println("SDK E/$tag: $msg ${t?.stackTraceToString().orEmpty()}")
}
