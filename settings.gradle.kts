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
include(":core:barcode")
include(":core:specialist")
include(":core:ui")
include(":core:testing")

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
include(":feature:tech-requests")
include(":feature:product-info")
include(":feature:advanced-mount")
include(":feature:fast-mount")
include(":feature:mount-assistant")
include(":feature:vehicle")
include(":feature:account")
include(":feature:configurator")
include(":feature:configurator:testing")
include(":feature:configurator-ui")
include(":feature:vehicle-ui")

include(":feature:device")
include(":feature:device:selection")
include(":feature:device:interaction")
include(":feature:device:interaction:testing")
include(":feature:device:firmware")
include(":feature:device:autostart")
include(":feature:device:state")
include(":feature:device:peripherals")
include(":feature:device:settings")
include(":feature:device:presentation-core")
include(":feature:device:interaction-ui")
include(":feature:device:settings-ui")

// Свой набор правил detekt `specmp` (docs/code-quality.md → «detekt»): ./gradlew :quality:detekt-rules:test
include(":quality:detekt-rules")

// Архитектурные тесты Konsist (docs/code-quality.md → «Konsist»): ./gradlew :quality:konsist:test
include(":quality:konsist")
