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
        namespace = "ru.alarmtrade.specmp.feature.device.autostart"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // обязательно для модулей с compose resources (см. docs/build.md): иллюстрации шагов
        androidResources.enable = true
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            // SDK: обучение бесключевому обходу, процедура Clone, режим программирования.
            implementation(project(":core:specialist"))
            implementation(libs.alarmtrade.specialist)
            implementation(libs.kermit)

            implementation(project(":core:common"))

            implementation(project(":core:localization"))
            implementation(project(":core:ui"))

            // Сессия устройства: связь, транспорт, PIN программирования
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

// Иллюстрации шагов (два слоя vector XML на картинку, см. .claude/tasks/autostart-bypass-clone.md, D8).
compose.resources {
    publicResClass = false
    generateResClass = always
}
