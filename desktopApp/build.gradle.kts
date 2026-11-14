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

// USB на JVM в SDK — нативный HID через JNA (с common 2.0.125, раньше usb4java только под x86_64): JDK любой
// архитектуры, на Apple Silicon — нативный arm64. Пакету нужен полный JDK с jpackage (JBR из Android Studio, которым
// часто запущен Gradle, его не содержит). Выбор: `compose.jvmHome` → на macOS установленный JDK 17 → JDK Gradle.
val localProperties: Properties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

val composeJvmHome: String? = providers.gradleProperty("compose.jvmHome").orNull
    ?: localProperties.getProperty("compose.jvmHome")

val macJdk17Home: String? = if (composeJvmHome == null && System.getProperty("os.name").startsWith("Mac")) {
    providers.exec {
        commandLine("/usr/libexec/java_home", "-v", "17")
        isIgnoreExitValue = true
    }.standardOutput.asText.orNull?.trim()?.takeIf { it.startsWith("/") }
} else {
    null
}
val desktopJvmHome: String? = composeJvmHome ?: macJdk17Home

compose.desktop {
    application {
        mainClass = "ru.alarmtrade.specmp.desktopApp.MainKt"
        desktopJvmHome?.let { javaHome = it }

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
            )
            // Имя, bundleID и upgradeUuid — как у старого desktop «Pandora Spec»: SpeCMP ставится поверх него, а его
            // апдейтер ищет «/Applications/Pandora Spec.app» и в канале Windows принимает только .exe.
            packageName = "Pandora Spec"
            // jlink кладёт в пакет только перечисленные модули JDK (`suggestRuntimeModules` + то, что грузится
            // рефлексией): без java.sql упакованное приложение падает на старте (локальная база SDK).
            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.prefs",
                "java.sql",
                "jdk.charsets",
                "jdk.crypto.ec",
                "jdk.localedata",
                "jdk.unsupported",
                "jdk.xml.dom",
            )
            packageVersion = libs.versions.app.versionName.get()

            macOS {
                bundleID = "ru.alarmtrade.specialist"
                iconFile.set(project.file("icons/macos.icns"))
                localProperties.getProperty("mac.signIdentity")?.let { signIdentity ->
                    signing {
                        sign.set(true)
                        identity.set(signIdentity)
                    }
                    notarization {
                        appleID.set(localProperties.getProperty("appleID"))
                        password.set(localProperties.getProperty("appleIDPassword"))
                        teamID.set(localProperties.getProperty("appleTeamId"))
                    }
                }
            }
            windows {
                menuGroup = packageName
                upgradeUuid = "CDF66F6C-79FC-4DE9-B1B5-9FD062E43912"
                perUserInstall = true
                dirChooser = false
                shortcut = true
                iconFile.set(project.file("icons/windows.ico"))
            }
            linux {
                iconFile.set(project.file("icons/linux.png"))
            }
        }
    }
}
