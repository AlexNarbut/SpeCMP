plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            // DI: AppLifecycle регистрируется через Koin-модуль appLifecycleModule
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            // ProcessLifecycleOwner — жизненный цикл всего приложения
            implementation(libs.androidx.lifecycle.process)
        }
    }
}
