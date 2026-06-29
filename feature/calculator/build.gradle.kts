import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    android {
        namespace = "ru.alarmtrade.specmp.feature.calculator"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // обязательно для модулей с compose resources (см. docs/build.md)
        androidResources.enable = true
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            // UI kit — brings compose.runtime/foundation/material3/ui transitively
            implementation(project(":core:ui"))

            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}

// Строковые ресурсы фичи (см. docs/localization.md)
compose.resources {
    publicResClass = false        // ресурсы фичи приватны
    generateResClass = always
}
