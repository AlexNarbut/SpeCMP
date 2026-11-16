import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import composeApp

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    private let messaging = FirebasePushMessaging()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        // Делегат — до конца запуска: иначе тап, запустивший приложение, не придёт в didReceive.
        UNUserNotificationCenter.current().delegate = self
        PushBridge.shared.install(messaging: messaging)
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
        messaging.apnsRegistered(error: nil)
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        messaging.apnsRegistered(error: error)
    }

    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        Messaging.messaging().appDidReceiveMessage(userInfo)
        PushBridge.shared.onRemoteMessage(userInfo: userInfo)
        completionHandler(.newData)
    }

    // Приложение открыто: показываем как в фоне (чат — тоже, решение владельца).
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
        if response.actionIdentifier == UNNotificationDefaultActionIdentifier {
            PushBridge.shared.onNotificationOpened(userInfo: response.notification.request.content.userInfo)
        }
        completionHandler()
    }
}

/// FCM-токен для Kotlin (`MessagingClientPushTokenSource`). Без APNs-токена Firebase токен не выдаёт — запросы ждут регистрации.
final class FirebasePushMessaging: NSObject, IosFirebaseMessaging {
    private var apnsReady = false
    private var apnsError: String?
    private var pending: [(String?, String?) -> Void] = []

    func apnsRegistered(error: Error?) {
        DispatchQueue.main.async {
            self.apnsReady = true
            self.apnsError = error?.localizedDescription
            let waiting = self.pending
            self.pending.removeAll()
            waiting.forEach(self.fetch)
        }
    }

    func fetchToken(completion: @escaping (String?, String?) -> Void) {
        DispatchQueue.main.async {
            if self.apnsReady {
                self.fetch(completion)
            } else {
                self.pending.append(completion)
            }
        }
    }

    func deleteToken(completion: @escaping (String?) -> Void) {
        Messaging.messaging().deleteToken { error in completion(error?.localizedDescription) }
    }

    private func fetch(_ completion: @escaping (String?, String?) -> Void) {
        if let apnsError {
            completion(nil, "APNs: \(apnsError)")
            return
        }
        Messaging.messaging().token { token, error in completion(token, error?.localizedDescription) }
    }
}
