plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
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

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))

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
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
        }
        // Галерея состояний экранов (headless-рендер в PNG)
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
