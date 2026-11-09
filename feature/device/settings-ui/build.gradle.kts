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

            implementation(project(":feature:device:settings"))
            implementation(project(":feature:vehicle"))
            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:presentation-core"))
            implementation(project(":feature:configurator"))

            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.compose.nav3)
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
            implementation(project(":core:testing"))
            implementation(project(":feature:device:interaction:testing"))
            implementation(project(":feature:configurator:testing"))
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
