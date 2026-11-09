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
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:network"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))
            implementation(project(":core:specialist"))
            implementation(project(":core:ui"))

            implementation(project(":feature:auth-state"))
            implementation(project(":feature:login"))
            implementation(project(":feature:device:selection"))
            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:interaction-ui"))
            implementation(project(":feature:device:firmware"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            implementation(libs.compose.nav3)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            implementation(libs.kotlinx.serialization.json)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
        }
    }
}
