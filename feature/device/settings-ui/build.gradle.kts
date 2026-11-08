plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "ru.alarmtrade.specmp.feature.device.settings.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Логика настроек (источники, сессия SDK) и сессия устройства (переподключение, шапка)
            implementation(project(":feature:device:settings"))
            implementation(project(":feature:vehicle"))
            implementation(project(":feature:device:interaction"))
            // BaseDeviceViewModel
            implementation(project(":feature:device:interaction-ui"))
            // «Мои конфигурации»: «Сохранить в…» из редактора
            implementation(project(":feature:configurator"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3-граф разделов настроек (см. docs/navigation.md)
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
