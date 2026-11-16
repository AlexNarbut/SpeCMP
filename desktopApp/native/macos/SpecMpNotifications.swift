import AppKit
import Foundation
import UserNotifications

// C ABI для JNA (composeApp jvmMain `MacNativeNotifications`). Сборка — задача `buildMacNotifications` (desktopApp).

public typealias SpecMpClickCallback = @convention(c) (UnsafePointer<CChar>?) -> Void

private final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    var onClick: SpecMpClickCallback?

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if response.actionIdentifier == UNNotificationDefaultActionIdentifier, let onClick {
            response.notification.request.identifier.withCString { onClick($0) }
        }
        completionHandler()
    }
}

private let delegate = NotificationDelegate()

private let statusTimeoutSeconds = 2.0

/// 1 — готово; 0 — процесс не из `.app` (`gradle run`: `UNUserNotificationCenter` без бандла падает).
/// Системный запрос разрешения macOS показывает только один раз — пока статус «не определён».
@_cdecl("specmp_notifications_start")
public func specmpNotificationsStart(_ onClick: SpecMpClickCallback?) -> Int32 {
    guard Bundle.main.bundleURL.pathExtension == "app", Bundle.main.bundleIdentifier != nil else { return 0 }
    delegate.onClick = onClick
    let center = UNUserNotificationCenter.current()
    center.delegate = delegate
    center.requestAuthorization(options: [.alert, .sound]) { _, _ in }
    return 1
}

/// 0 — не спрашивали (или ждёт ответа), 1 — уведомления не дойдут до пользователя, 2 — показываются; -1 — нет ответа.
/// Выключатель «Разрешить уведомления» в Системных настройках может оставить `authorizationStatus` = authorized
/// и выключить только показ (баннеры и Центр уведомлений) — это тоже «не дойдут».
@_cdecl("specmp_notifications_status")
public func specmpNotificationsStatus() -> Int32 {
    let done = DispatchSemaphore(value: 0)
    var status: Int32 = -1
    UNUserNotificationCenter.current().getNotificationSettings { settings in
        switch settings.authorizationStatus {
        case .notDetermined:
            status = 0
        case .denied:
            status = 1
        default:
            let hidden = settings.alertSetting != .enabled && settings.notificationCenterSetting != .enabled
            status = hidden ? 1 : 2
        }
        done.signal()
    }
    _ = done.wait(timeout: .now() + statusTimeoutSeconds)
    return status
}

@_cdecl("specmp_notifications_open_settings")
public func specmpNotificationsOpenSettings() {
    let id = Bundle.main.bundleIdentifier ?? ""
    let url = URL(string: "x-apple.systempreferences:com.apple.Notifications-Settings.extension?id=\(id)")
        ?? URL(string: "x-apple.systempreferences:com.apple.Notifications-Settings.extension")!
    NSWorkspace.shared.open(url)
}

@_cdecl("specmp_notifications_show")
public func specmpNotificationsShow(
    _ id: UnsafePointer<CChar>,
    _ title: UnsafePointer<CChar>,
    _ body: UnsafePointer<CChar>
) {
    let content = UNMutableNotificationContent()
    content.title = String(cString: title)
    content.body = String(cString: body)
    content.sound = .default
    let request = UNNotificationRequest(identifier: String(cString: id), content: content, trigger: nil)
    UNUserNotificationCenter.current().add(request) { error in
        if let error { NSLog("SpecMpNotifications: not shown: %@", String(describing: error)) }
    }
}

@_cdecl("specmp_notifications_remove_all")
public func specmpNotificationsRemoveAll() {
    UNUserNotificationCenter.current().removeAllDeliveredNotifications()
}
