plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            // BLE-типы публичного API SDK живут в bluetooth-common (ru.alarmtrade.bluetoothControl.*).
            implementation(libs.alarmtrade.bluetooth)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))

            implementation(project(":feature:device:interaction"))

            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.compose.nav3)
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
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
