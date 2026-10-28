import SwiftUI
import composeApp

@main
struct SpeCMPApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
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
