rootProject.name = "SpeCMP"

pluginManagement {
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

// Feature modules
include(":feature:auth-state")
include(":feature:login")
include(":feature:main")
include(":feature:support-chat")
include(":feature:advanced-mount")

include(":feature:device")
include(":feature:device:selection")
include(":feature:device:interaction")
include(":feature:device:selection-ui")
include(":feature:device:interaction-ui")
