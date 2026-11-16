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
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.push.PushOpenRequests
import ru.alarmtrade.specmp.core.push.PushPayload
import ru.alarmtrade.specmp.core.push.PushTarget
import ru.alarmtrade.specmp.core.push.PushType
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatNotifier

/**
 * Баннер ОС, бейдж и внимание в Dock/панели задач; клик — окно вперёд и чат ([PushOpenRequests]).
 * Windows/Linux — `TrayIcon.displayMessage`, клик по баннеру/иконке трея. macOS в собранном `.app` — нативный
 * баннер ([MacNativeNotifications]), иначе (`gradle run`) — `osascript` (см. [showMacBanner]); на macOS чат
 * открывает и клик по иконке в Dock, пока есть непросмотренные. Вызовы — на EDT (Main у Compose Desktop).
 */
internal class DesktopChatNotifier(
    private val title: String,
    private val attachmentText: String,
    private val isChatVisible: () -> Boolean,
) : SupportChatNotifier {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val started = MutableStateFlow(false)

    private val macNotifications = MutableStateFlow<MacNativeNotifications?>(null)
    private val trayIcon = MutableStateFlow<TrayIcon?>(null)
    private val unread = MutableStateFlow(0)

    private val hasUnseenChat = MutableStateFlow(false)

    private val reopenListener = AppReopenedListener { if (hasUnseenChat.value) openChat() }

    private val focusListener = object : WindowAdapter() {
        override fun windowGainedFocus(e: WindowEvent) {
            clearBadge()
            // Позже события «клик по Dock» (оно тоже фокусирует окно) — чтобы тот успел открыть чат.
            EventQueue.invokeLater { hasUnseenChat.update { false } }
        }
    }

    override fun start() {
        if (!started.compareAndSet(expect = false, update = true)) return
        window()?.addWindowFocusListener(focusListener)
        if (isMac) {
            appEvents()?.addAppEventListener(reopenListener)
            macNotifications.update { MacNativeNotifications.create() }
        }
    }

    override fun isDenied(): Boolean = macNotifications.value?.isDenied == true

    override fun openSettings() {
        macNotifications.value?.openSettings()
    }

    override fun show(text: String?) {
        showBanner(text ?: attachmentText)
    }

    private fun showBanner(text: String) {
        val window = window()
        if (window?.isFocused == true && isChatVisible()) return
        when {
            !isMac -> tray()?.displayMessage(title, text, TrayIcon.MessageType.INFO)

            else -> macNotifications.value?.show(CHAT_NOTIFICATION_ID, title, text.take(MAX_BANNER_LENGTH), CHAT_DATA)
                ?: showMacBanner(text)
        }
        hasUnseenChat.update { true }
        val count = unread.updateAndGet { it + 1 }
        taskbar(Taskbar.Feature.ICON_BADGE_TEXT)?.setIconBadge(count.toString())
        if (window?.isFocused != true) taskbar(Taskbar.Feature.USER_ATTENTION)?.requestUserAttention(true, false)
    }

    override fun dispose() {
        if (!started.compareAndSet(expect = true, update = false)) return
        window()?.removeWindowFocusListener(focusListener)
        if (isMac) appEvents()?.removeAppEventListener(reopenListener)
        macNotifications.getAndUpdate { null }?.removeAll()
        clearBadge()
        trayIcon.getAndUpdate { null }?.let { icon -> safeRun { SystemTray.getSystemTray().remove(icon) } }
    }

    private fun clearBadge() {
        unread.update { 0 }
        taskbar(Taskbar.Feature.ICON_BADGE_TEXT)?.setIconBadge(null)
    }

    // Запасной путь macOS вне `.app`: TrayIcon.displayMessage там ничего не показывает (устаревший NSUserNotification).
    // Баннер — от имени «Редактора скриптов», клик по нему в приложение не приходит. Текст — аргументами скрипта.
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

    // Создание — не в update: лямбда повторяется при конфликте, а иконка добавляется в системный трей.
    private fun tray(): TrayIcon? = trayIcon.value ?: createTrayIcon()?.let { created ->
        if (trayIcon.compareAndSet(null, created)) {
            created
        } else {
            safeRun { SystemTray.getSystemTray().remove(created) }
            trayIcon.value
        }
    }

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
        DesktopWindow.bringToFront()
        hasUnseenChat.update { false }
        PushOpenRequests.open(PushTarget.Chat)
    }

    private fun window(): Frame? = DesktopWindow.current()

    private fun taskbar(feature: Taskbar.Feature): Taskbar? = safeRun {
        Taskbar.getTaskbar().takeIf { Taskbar.isTaskbarSupported() && it.isSupported(feature) }
    }.getValueIfSuccess()

    private companion object {
        const val ICON = "icon.png"
        const val MAX_BANNER_LENGTH = 200

        // Один идентификатор: новое сообщение заменяет баннер прежнего, как на Android/iOS.
        const val CHAT_NOTIFICATION_ID = "support_chat"

        // Клик по баннеру — через MacNativeBridge, как по push чата.
        val CHAT_DATA = mapOf(PushPayload.KEY_TYPE to PushType.CHAT.code.toString())

        val isMac = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)
    }
}
