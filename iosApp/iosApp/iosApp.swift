import SwiftUI
import composeApp

@main
struct SpeCMPApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                // Прошивка и долгий обмен с блоком идут минутами: автоблокировка приостановила бы приложение.
                .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Версия — из Info.plist (источник для iOS); без неё сборка некорректна.
        let info = Bundle.main.infoDictionary!
        let version = info["CFBundleShortVersionString"] as! String
        let build = Int(info["CFBundleVersion"] as! String)!
        return MainViewControllerKt.MainViewController(
            appVersionName: version,
            appVersionCode: Int32(build)
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
