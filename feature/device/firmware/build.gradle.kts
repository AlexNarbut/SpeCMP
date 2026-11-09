plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SDK прошивки (бывший логический модуль firmware): версии, обновление, загрузчик.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Сессия устройства: подключение, переход в загрузчик
            implementation(project(":feature:device:interaction"))
            // Общий presentation устройства: база VM блока, диалог PIN
            implementation(project(":feature:device:presentation-core"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3-граф экранов в режиме загрузчика (см. docs/navigation.md)
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

        // rememberFirmwareFilePicker: SAF-лаунчер (rememberLauncherForActivityResult)
        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
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
