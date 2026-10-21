import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    android {
        namespace = "ru.alarmtrade.specmp.feature.device.selection"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:specialist"))

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)

            implementation(libs.alarmtrade.specialist)
            // BLE-типы из публичного API specialist (ScannedDeviceState и т.д.) живут
            // в bluetooth-common (пакеты ru.alarmtrade.bluetoothControl.*). USB-типы —
            // в самом specialist (ru.alarmtrade.specialist.domain.usb.*), отдельная
            // usb-либа не нужна.
            implementation(libs.alarmtrade.bluetooth)

            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
