plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:specialist"))
            implementation(project(":feature:account"))
            implementation(project(":core:ui"))
            implementation(project(":feature:auth-state"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3-граф фичи (см. docs/navigation.md)
            implementation(libs.compose.nav3)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.alarmtrade.specialist)

            implementation(libs.kermit)
        }
    }
}

// Ресурсы фичи — только картинки (drawable); строки — в core/localization (docs/localization.md)
compose.resources {
    publicResClass = false
    generateResClass = always
}
