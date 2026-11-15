package ru.alarmtrade.specmp.push

import co.touchlab.kermit.Logger
import java.awt.Desktop
import java.awt.EventQueue
import java.awt.Frame
import java.awt.SystemTray
import java.awt.Taskbar
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.desktop.AppReopenedListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.push.PushOpenRequests
import ru.alarmtrade.specmp.core.push.PushTarget

/**
 * Баннер ОС, бейдж и внимание в Dock/панели задач; клик — окно вперёд и чат ([PushOpenRequests]).
 * Windows/Linux — `TrayIcon.displayMessage`, клик по баннеру/иконке трея. macOS — `osascript` (см. [showMacBanner]),
 * клик — по иконке в Dock, пока есть непросмотренные. Вызовы — на EDT (Main у Compose Desktop).
 */
internal class DesktopChatNotifier(private val title: String, private val isChatVisible: () -> Boolean) {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private var trayIcon: TrayIcon? = null
    private var unread = 0
    private val hasUnseenChat = MutableStateFlow(false)

    private val reopenListener = AppReopenedListener { if (hasUnseenChat.value) openChat() }

    private val focusListener = object : WindowAdapter() {
        override fun windowGainedFocus(e: WindowEvent) {
            clearBadge()
            // Позже события «клик по Dock» (оно тоже фокусирует окно) — чтобы тот успел открыть чат.
            EventQueue.invokeLater { hasUnseenChat.update { false } }
        }
    }

    fun start() {
        window()?.addWindowFocusListener(focusListener)
        if (isMac) appEvents()?.addAppEventListener(reopenListener)
    }

    fun show(text: String) {
        val window = window()
        if (window?.isFocused == true && isChatVisible()) return
        if (isMac) showMacBanner(text) else tray()?.displayMessage(title, text, TrayIcon.MessageType.INFO)
        hasUnseenChat.update { true }
        unread++
        taskbar(Taskbar.Feature.ICON_BADGE_TEXT)?.setIconBadge(unread.toString())
        if (window?.isFocused != true) taskbar(Taskbar.Feature.USER_ATTENTION)?.requestUserAttention(true, false)
    }

    fun dispose() {
        window()?.removeWindowFocusListener(focusListener)
        if (isMac) appEvents()?.removeAppEventListener(reopenListener)
        clearBadge()
        trayIcon?.let { icon -> safeRun { SystemTray.getSystemTray().remove(icon) } }
        trayIcon = null
    }

    private fun clearBadge() {
        unread = 0
        taskbar(Taskbar.Feature.ICON_BADGE_TEXT)?.setIconBadge(null)
    }

    // TrayIcon.displayMessage на современной macOS ничего не показывает (устаревший NSUserNotification), а
    // UNUserNotificationCenter из JVM без нативного кода недоступен. Баннер — от имени «Редактора скриптов»,
    // клик по нему в приложение не приходит. Текст — аргументами, без подстановки в скрипт.
    private fun showMacBanner(text: String) {
        safeRun {
            ProcessBuilder(
                "osascript",
                "-e", "on run argv",
                "-e", "display notification (item 2 of argv) with title (item 1 of argv)",
                "-e", "end run",
                title,
                text.take(MAX_BANNER_LENGTH),
            ).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start()
        }.onError { logger.w(it) { "macOS notification is not shown" } }
    }

    private fun appEvents(): Desktop? = safeRun {
        Desktop.getDesktop().takeIf {
            Desktop.isDesktopSupported() && it.isSupported(Desktop.Action.APP_EVENT_REOPENED)
        }
    }.getValueIfSuccess()

    private fun tray(): TrayIcon? = trayIcon ?: createTrayIcon()?.also { trayIcon = it }

    private fun createTrayIcon(): TrayIcon? {
        if (!SystemTray.isSupported()) return null
        val image = Thread.currentThread().contextClassLoader?.getResource(ICON)
            ?.let(Toolkit.getDefaultToolkit()::getImage) ?: return null
        return safeRun {
            TrayIcon(image, title).apply {
                isImageAutoSize = true
                addActionListener { EventQueue.invokeLater(::openChat) }
                SystemTray.getSystemTray().add(this)
            }
        }.onError { logger.w(it) { "Tray icon is not added" } }.getValueIfSuccess()
    }

    private fun openChat() {
        window()?.apply {
            if (extendedState and Frame.ICONIFIED != 0) extendedState = extendedState and Frame.ICONIFIED.inv()
            isVisible = true
            toFront()
            requestFocus()
        }
        hasUnseenChat.update { false }
        PushOpenRequests.open(PushTarget.Chat)
    }

    private fun window(): Frame? = Frame.getFrames().firstOrNull { it.isDisplayable && it.isVisible }

    private fun taskbar(feature: Taskbar.Feature): Taskbar? = safeRun {
        Taskbar.getTaskbar().takeIf { Taskbar.isTaskbarSupported() && it.isSupported(feature) }
    }.getValueIfSuccess()

    private companion object {
        const val ICON = "icon.png"
        const val MAX_BANNER_LENGTH = 200

        val isMac = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)
    }
}
