// swift-tools-version:5.9
import PackageDescription

// Firebase — та же версия, что у iosApp (Package.resolved): один проект Firebase и один bundle ID на обеих платформах.
let package = Package(
    name: "SpecMpNotifications",
    platforms: [.macOS(.v12)],
    products: [
        .library(name: "specmp_notifications", type: .dynamic, targets: ["SpecMpNotifications"]),
    ],
    dependencies: [
        .package(url: "https://github.com/firebase/firebase-ios-sdk", exact: "12.19.3"),
    ],
    targets: [
        .target(
            name: "SpecMpNotifications",
            dependencies: [.product(name: "FirebaseMessaging", package: "firebase-ios-sdk")]
        ),
    ]
)
