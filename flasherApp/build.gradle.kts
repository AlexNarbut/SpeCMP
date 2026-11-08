// Composition root приложения прошивки: логин → поиск (BLE / USB / COM) → «Обновить из файла».
// Только Android и desktop: COM-порта на iOS в SDK нет.
plugins {
    id("specmp.kmp.android-jvm")
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "ru.alarmtrade.specmp.flasher"
    }

    sourceSets {
        commonMain.dependencies {
            // Core modules
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:network"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))
            implementation(project(":core:specialist"))
            implementation(project(":core:ui"))

            // Feature modules: авторизация (первая версия) и прошивка
            implementation(project(":feature:auth-state"))
            implementation(project(":feature:login"))
            implementation(project(":feature:device:selection"))
            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:interaction-ui"))
            implementation(project(":feature:device:firmware"))

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            // Navigation; ViewModelStore-декоратор Nav3: per-entry ViewModelStore → очистка VM при pop
            implementation(libs.compose.nav3)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            // Serialization (for nav3 routes)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
        }
    }
}
