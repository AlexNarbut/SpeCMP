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
        namespace = "ru.alarmtrade.specmp.feature.fastmount"
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
            // SDK: мастер быстрого монтажа (FastMountInteractor), облачные конфигурации, база устройств.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Сессия устройства: связь, транспорт, режим программирования
            implementation(project(":feature:device:interaction"))
            // BaseDeviceViewModel, ServicePinDialog
            implementation(project(":feature:device:interaction-ui"))

            implementation(libs.androidx.lifecycle.viewmodel)

            // Nav3 (см. docs/navigation.md)
            implementation(libs.compose.nav3)
            // ViewModelStore-декоратор: per-entry ViewModelStore → очистка VM при pop
            implementation(libs.androidx.lifecycle.viewmodel.nav3)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
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

// Ресурсы фичи — только картинки (drawable); строки — в core/localization (docs/localization.md)
compose.resources {
    publicResClass = false
    generateResClass = always
}
