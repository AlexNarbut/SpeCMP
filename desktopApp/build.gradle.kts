import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":composeApp"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

// USB-слой SDK на JVM (usb4java 1.3.0) содержит нативную библиотеку только для darwin-x86-64:
// на macOS arm64 USB-устройства находятся только при запуске на x86_64-JDK. JDK для run/package —
// `compose.jvmHome` (Gradle-свойство или local.properties), иначе JDK Gradle.
val composeJvmHome: String? = providers.gradleProperty("compose.jvmHome").orNull
    ?: rootProject.file("local.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use(::load) }.getProperty("compose.jvmHome")
    }

compose.desktop {
    application {
        mainClass = "ru.alarmtrade.specmp.desktopApp.MainKt"
        composeJvmHome?.let { javaHome = it }

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "SpeCMP"
            packageVersion = "2.0.40"

            macOS {
                iconFile.set(project.file("icons/macos.icns"))
            }
            windows {
                iconFile.set(project.file("icons/windows.ico"))
            }
            linux {
                iconFile.set(project.file("icons/linux.png"))
            }
        }
    }
}
