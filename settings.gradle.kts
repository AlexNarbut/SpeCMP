rootProject.name = "SpeCMP"

pluginManagement {
    // Convention-плагины (specmp.kmp.library, specmp.kmp.compose, specmp.detekt): build-logic/src/main/kotlin.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}
// Core modules
include(":core:common")
include(":core:lifecycle")
include(":core:localization")
include(":core:network")
include(":core:permissions")
include(":core:specialist")
include(":core:ui")

// Main Compose Multiplatform application
include(":composeApp")
include(":androidApp")
include(":desktopApp")

// Приложение прошивки: свой composition root и платформенные обёртки
include(":flasherApp")
include(":flasherAndroidApp")
include(":flasherDesktopApp")

// Feature modules
include(":feature:auth-state")
include(":feature:login")
include(":feature:app-settings")
include(":feature:main")
include(":feature:support-chat")
include(":feature:advanced-mount")
include(":feature:fast-mount")
include(":feature:vehicle")
include(":feature:account")
include(":feature:configurator")
include(":feature:configurator-ui")
include(":feature:vehicle-ui")

include(":feature:device")
include(":feature:device:selection")
include(":feature:device:interaction")
include(":feature:device:firmware")
include(":feature:device:autostart")
include(":feature:device:state")
include(":feature:device:settings")
include(":feature:device:interaction-ui")
include(":feature:device:settings-ui")
