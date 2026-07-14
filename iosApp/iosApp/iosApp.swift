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
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "2.0.40"
        let build = Int(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "240") ?? 240
        return MainViewControllerKt.MainViewController(
            appVersionName: version,
            appVersionCode: Int32(build)
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
