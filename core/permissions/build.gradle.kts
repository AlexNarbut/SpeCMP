plugins {
    id("specmp.kmp.compose")
}

kotlin {
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
