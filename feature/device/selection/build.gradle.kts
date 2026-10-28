import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
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
        // обязательно для модулей с compose resources (см. docs/build.md)
        androidResources.enable = true
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            // SDK устройства (бывший логический модуль selection): скан BLE/USB.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            // BLE-типы из публичного API specialist (ScannedDeviceState и т.д.) живут
            // в bluetooth-common (пакеты ru.alarmtrade.bluetoothControl.*). USB-типы —
            // в самом specialist (ru.alarmtrade.specialist.domain.usb.*), отдельная
            // usb-либа не нужна.
            implementation(libs.alarmtrade.bluetooth)
            implementation(libs.kermit)

            implementation(project(":core:common"))
            implementation(project(":core:ui"))
            implementation(project(":core:permissions"))

            // Сессия устройства (подключение, чтение информации о блоке)
            implementation(project(":feature:device:interaction"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3-граф фичи (см. docs/navigation.md)
            implementation(libs.compose.nav3)
            // ViewModelStore-декоратор: per-entry ViewModelStore → очистка VM при pop
            implementation(libs.androidx.lifecycle.viewmodel.nav3)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            // Фейковый жизненный цикл приложения для BaseViewModel (Koin)
            implementation(project(":core:lifecycle"))
            implementation(libs.koin.core)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Галерея состояний экранов (headless-рендер в PNG)
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

// Строковые ресурсы фичи (см. docs/localization.md)
compose.resources {
    publicResClass = false
    generateResClass = always
}
