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

        androidMain.dependencies {
            // ProcessLifecycleOwner — жизненный цикл всего приложения
            implementation(libs.androidx.lifecycle.process)
        }
    }
}
