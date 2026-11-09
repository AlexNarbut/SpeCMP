plugins {
    id("specmp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            // api: типы permissions-common — в публичном API для фич
            api(libs.alarmtrade.permissions)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.fragment)
        }
    }
}
