plugins {
    id("specmp.kmp.compose")
}

kotlin {
    android {
        namespace = "ru.alarmtrade.specmp.feature.device.presentation.core"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:ui"))
            api(project(":feature:device:interaction"))
            implementation(project(":feature:configurator"))

            api(project(":core:speech"))

            implementation(project(":core:common"))
            implementation(project(":core:localization"))

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:testing"))
            implementation(project(":feature:device:interaction:testing"))
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
