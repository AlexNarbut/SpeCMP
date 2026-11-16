import AppKit
import Foundation
import UserNotifications

// C ABI для JNA (composeApp jvmMain `MacNativeBridge`). Сборка — задача `buildMacNotifications` (desktopApp).

/// Клик: идентификатор уведомления и `userInfo` в JSON.
public typealias SpecMpClickCallback = @convention(c) (UnsafePointer<CChar>?, UnsafePointer<CChar>?) -> Void

/// Удалённый push при открытом приложении: `userInfo` в JSON → 1 — показать баннер, 0 — нет.
public typealias SpecMpPresentCallback = @convention(c) (UnsafePointer<CChar>?) -> Int32

private final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    var onClick: SpecMpClickCallback?
    var shouldPresent: SpecMpPresentCallback?

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let isRemote = notification.request.trigger is UNPushNotificationTrigger
        let present = !isRemote || shouldPresent.map { callback in
            withOptionalCString(jsonString(notification.request.content.userInfo)) { callback($0) } == 1
        } ?? true
        completionHandler(present ? [.banner, .list, .sound] : [])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if response.actionIdentifier == UNNotificationDefaultActionIdentifier, let onClick {
            let request = response.notification.request
            request.identifier.withCString { id in
                withOptionalCString(jsonString(request.content.userInfo)) { onClick(id, $0) }
            }
        }
        completionHandler()
    }
}

private let delegate = NotificationDelegate()

private let statusTimeoutSeconds = 2.0

/// Делегат — до запуска AppKit (из `main`): клик по уведомлению, запустивший приложение, приходит сразу после старта.
/// 1 — готово; 0 — процесс не из `.app` (`gradle run`: `UNUserNotificationCenter` без бандла падает).
@_cdecl("specmp_notifications_install")
public func specmpNotificationsInstall(
    _ onClick: SpecMpClickCallback?,
    _ shouldPresent: SpecMpPresentCallback?
) -> Int32 {
    guard isAppBundle else { return 0 }
    delegate.onClick = onClick
    delegate.shouldPresent = shouldPresent
    UNUserNotificationCenter.current().delegate = delegate
    return 1
}

/// Системный запрос разрешения macOS показывает только один раз — пока статус «не определён».
@_cdecl("specmp_notifications_request_authorization")
public func specmpNotificationsRequestAuthorization() {
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { _, _ in }
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

/// `userInfoJson` — объект JSON со строковыми значениями (данные push для клика) или `nil`.
@_cdecl("specmp_notifications_show")
public func specmpNotificationsShow(
    _ id: UnsafePointer<CChar>,
    _ title: UnsafePointer<CChar>,
    _ body: UnsafePointer<CChar>,
    _ userInfoJson: UnsafePointer<CChar>?
) {
    let content = UNMutableNotificationContent()
    content.title = String(cString: title)
    content.body = String(cString: body)
    content.sound = .default
    if let userInfoJson,
       let data = String(cString: userInfoJson).data(using: .utf8),
       let userInfo = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
        content.userInfo = userInfo
    }
    let request = UNNotificationRequest(identifier: String(cString: id), content: content, trigger: nil)
    UNUserNotificationCenter.current().add(request) { error in
        if let error { NSLog("SpecMpNotifications: not shown: %@", String(describing: error)) }
    }
}

@_cdecl("specmp_notifications_remove_all")
public func specmpNotificationsRemoveAll() {
    UNUserNotificationCenter.current().removeAllDeliveredNotifications()
}
