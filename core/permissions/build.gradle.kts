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
        namespace = "ru.alarmtrade.specmp.core.permissions"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            // Типы permissions-common (PermissionsController, Permission, PermissionState,
            // DeniedException/DeniedAlwaysException) — публичны для фич-потребителей.
            api(libs.alarmtrade.permissions)
            // DI: PermissionsProvider регистрируется через Koin-модуль permissionsModule()
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.fragment)
        }
    }
}
