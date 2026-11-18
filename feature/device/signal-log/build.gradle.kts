plugins {
    id("specmp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:files"))
            implementation(project(":core:localization"))
            implementation(project(":core:specialist"))
            implementation(project(":core:ui"))

            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:presentation-core"))

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.alarmtrade.specialist)
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

compose.resources {
    publicResClass = false
    generateResClass = always
}
