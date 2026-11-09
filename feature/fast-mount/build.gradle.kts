plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SDK: мастер быстрого монтажа (FastMountInteractor), облачные конфигурации, база устройств.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Сессия устройства: связь, транспорт, режим программирования
            implementation(project(":feature:device:interaction"))
            // Общий presentation устройства: база VM блока, диалог PIN
            implementation(project(":feature:device:presentation-core"))

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
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
            implementation(project(":feature:device:interaction:testing"))
        }
        // Галерея состояний экранов (headless-рендер в PNG)
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

// Ресурсы фичи — только картинки (drawable); строки — в core/localization (docs/localization.md)
compose.resources {
    publicResClass = false
    generateResClass = always
}
