plugins {
    id("specmp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui"))
            implementation(project(":core:localization"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.mlkit.vision)
            // Модель внутри APK: работает без Google Play Services (Huawei и др.) и без сети.
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.androidx.lifecycle.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
