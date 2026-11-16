import AppKit
import FirebaseCore
import FirebaseMessaging
import Foundation
import ObjectiveC

public typealias SpecMpTokenCallback = @convention(c) (UnsafePointer<CChar>?, UnsafePointer<CChar>?) -> Void
public typealias SpecMpErrorCallback = @convention(c) (UnsafePointer<CChar>?) -> Void
public typealias SpecMpMessageCallback = @convention(c) (UnsafePointer<CChar>?) -> Void

// Состояние трогается только на главной очереди (AppKit-поток AWT).
private var configured = false
private var onMessage: SpecMpMessageCallback?
private var pendingTokens: [SpecMpTokenCallback] = []

/// 1 — Firebase будет настроен на главной очереди; 0 — не `.app` или plist не читается.
@_cdecl("specmp_push_configure")
public func specmpPushConfigure(_ plistPath: UnsafePointer<CChar>, _ onRemoteMessage: SpecMpMessageCallback?) -> Int32 {
    guard isAppBundle, let options = FirebaseOptions(contentsOfFile: String(cString: plistPath)) else { return 0 }
    DispatchQueue.main.async {
        guard !configured else { return }
        onMessage = onRemoteMessage
        if FirebaseApp.app() == nil { FirebaseApp.configure(options: options) }
        configured = installAppDelegateHooks()
    }
    return 1
}

/// FCM-токен; сначала APNs-токен — без него Firebase на Apple токен не выдаёт.
@_cdecl("specmp_push_token")
public func specmpPushToken(_ callback: SpecMpTokenCallback) {
    DispatchQueue.main.async {
        guard configured else { return callback(nil, "push is not configured") }
        if Messaging.messaging().apnsToken != nil { return fetchToken(callback) }
        pendingTokens.append(callback)
        if pendingTokens.count == 1 { NSApp.registerForRemoteNotifications() }
    }
}

@_cdecl("specmp_push_delete_token")
public func specmpPushDeleteToken(_ callback: SpecMpErrorCallback) {
    DispatchQueue.main.async {
        guard configured else { return callback(nil) }
        Messaging.messaging().deleteToken { error in withOptionalCString(error?.localizedDescription, callback) }
    }
}

private func fetchToken(_ callback: SpecMpTokenCallback) {
    Messaging.messaging().token { token, error in
        withOptionalCString(token) { tokenPtr in
            withOptionalCString(error?.localizedDescription) { callback(tokenPtr, $0) }
        }
    }
}

private func onApnsToken(_ token: Data) {
    // Developer ID и профиль с aps-environment = production: песочница APNs этой сборке не нужна.
    Messaging.messaging().setAPNSToken(token, type: .prod)
    let callbacks = pendingTokens
    pendingTokens = []
    callbacks.forEach(fetchToken)
}

private func onApnsError(_ error: NSError) {
    let callbacks = pendingTokens
    pendingTokens = []
    callbacks.forEach { callback in withOptionalCString("APNs: \(error.localizedDescription)") { callback(nil, $0) } }
}

private func onRemoteNotification(_ userInfo: NSDictionary) {
    Messaging.messaging().appDidReceiveMessage(userInfo as? [AnyHashable: Any] ?? [:])
    guard let onMessage, let json = jsonString(userInfo) else { return }
    json.withCString { onMessage($0) }
}

// Делегат NSApplication принадлежит AWT: колбэки APNs добавляются в его класс. Прокси Firebase выключен в Info.plist
// (FirebaseAppDelegateProxyEnabled), чтобы не подменял класс делегата AWT.
private func installAppDelegateHooks() -> Bool {
    guard let delegate = NSApp.delegate, let cls = object_getClass(delegate) else {
        NSLog("SpecMpPush: NSApp has no delegate")
        return false
    }
    let didRegister: @convention(block) (AnyObject, NSApplication, NSData) -> Void = { _, _, token in
        onApnsToken(token as Data)
    }
    let didFail: @convention(block) (AnyObject, NSApplication, NSError) -> Void = { _, _, error in
        onApnsError(error)
    }
    let didReceive: @convention(block) (AnyObject, NSApplication, NSDictionary) -> Void = { _, _, userInfo in
        onRemoteNotification(userInfo)
    }
    addMethod(cls, #selector(NSApplicationDelegate.application(_:didRegisterForRemoteNotificationsWithDeviceToken:)), didRegister)
    addMethod(cls, #selector(NSApplicationDelegate.application(_:didFailToRegisterForRemoteNotificationsWithError:)), didFail)
    addMethod(cls, #selector(NSApplicationDelegate.application(_:didReceiveRemoteNotification:)), didReceive)
    return true
}

private func addMethod(_ cls: AnyClass, _ selector: Selector, _ block: Any) {
    if !class_addMethod(cls, selector, imp_implementationWithBlock(block), "v@:@@") {
        NSLog("SpecMpPush: %@ is already implemented by the app delegate", NSStringFromSelector(selector))
    }
}

func jsonString(_ object: Any) -> String? {
    guard JSONSerialization.isValidJSONObject(object),
          let data = try? JSONSerialization.data(withJSONObject: object) else { return nil }
    return String(data: data, encoding: .utf8)
}

func withOptionalCString<R>(_ value: String?, _ body: (UnsafePointer<CChar>?) -> R) -> R {
    guard let value else { return body(nil) }
    return value.withCString(body)
}

var isAppBundle: Bool {
    Bundle.main.bundleURL.pathExtension == "app" && Bundle.main.bundleIdentifier != nil
}
