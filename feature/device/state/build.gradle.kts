plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SDK: живое состояние блока (телеметрия) и команды управления — только BLE.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            // DeviceState и команды управления (ru.alarmtrade.devices.*)
            implementation(libs.alarmtrade.devices)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Сессия устройства: связь, транспорт, режим программирования (PIN)
            implementation(project(":feature:device:interaction"))
            // LegacyBaseDeviceViewModel, ServicePinDialog
            implementation(project(":feature:device:interaction-ui"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3 (см. docs/navigation.md)
            implementation(libs.compose.nav3)
            // ViewModelStore-декоратор: per-entry ViewModelStore → очистка VM при pop
            implementation(libs.androidx.lifecycle.viewmodel.nav3)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Галерея состояний экранов (headless-рендер в PNG)
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

// Слои силуэта и значки показателей/команд (vector XML из design/features/check-state).
compose.resources {
    publicResClass = false
    generateResClass = always
}
