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

// version.properties (versionName/versionCode) — из каталога версий, а не руками в resources.
val generateVersionProperties by tasks.registering {
    val name = libs.versions.app.versionName.get()
    val code = libs.versions.app.versionCode.get()
    val output = layout.buildDirectory.dir("generated/version")
    inputs.property("versionName", name)
    inputs.property("versionCode", code)
    outputs.dir(output)
    doLast {
        output.get().file("version.properties").asFile.writeText("versionName=$name\nversionCode=$code\n")
    }
}
sourceSets.main { resources.srcDir(generateVersionProperties) }

dependencies {
    implementation(project(":composeApp"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

// USB-слой SDK на JVM (usb4java 1.3.0) содержит нативную библиотеку только для darwin-x86-64:
// на macOS arm64 USB-устройства находятся только при запуске на x86_64-JDK. JDK для run/package выбирается так:
// 1) `compose.jvmHome` (Gradle-свойство или local.properties) — явный выбор;
// 2) на macOS arm64 — установленный x86_64-JDK 17 (`/usr/libexec/java_home -a x86_64 -v 17`), чтобы USB не пропадал,
//    когда IDE пересоздаёт local.properties;
// 3) иначе JDK Gradle.
val composeJvmHome: String? = providers.gradleProperty("compose.jvmHome").orNull
    ?: rootProject.file("local.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use(::load) }.getProperty("compose.jvmHome")
    }

val isMacArm = System.getProperty("os.name").startsWith("Mac") && System.getProperty("os.arch") == "aarch64"
val macX64JvmHome: String? = if (composeJvmHome == null && isMacArm) {
    providers.exec {
        commandLine("/usr/libexec/java_home", "-a", "x86_64", "-v", "17")
        isIgnoreExitValue = true
    }.standardOutput.asText.orNull?.trim()?.takeIf { it.startsWith("/") }
} else {
    null
}
val desktopJvmHome: String? = composeJvmHome ?: macX64JvmHome

// macOS arm64 без x86_64-JDK: приложение запустится, но USB-устройства не найдутся — говорим об этом при запуске.
if (isMacArm && desktopJvmHome == null) {
    tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
        doFirst {
            logger.warn(
                "WARNING: macOS arm64 без x86_64-JDK 17 — USB-устройства не будут найдены (usb4java). " +
                    "Установите x86_64-JDK 17 или задайте compose.jvmHome, см. docs/build.md."
            )
        }
    }
}

compose.desktop {
    application {
        mainClass = "ru.alarmtrade.specmp.desktopApp.MainKt"
        desktopJvmHome?.let { javaHome = it }

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "SpeCMP"
            packageVersion = libs.versions.app.versionName.get()

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
