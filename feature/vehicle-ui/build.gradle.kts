import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    android {
        namespace = "ru.alarmtrade.specmp.feature.vehicle.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Логика каталога автомобилей (feature/vehicle)
            implementation(project(":feature:vehicle"))

            implementation(libs.androidx.lifecycle.viewmodel)
            // NavigationBackHandler: системный «назад» внутри экрана (шаги каталога)
            implementation(libs.compose.nav3)

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
