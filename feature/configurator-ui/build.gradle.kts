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
        namespace = "ru.alarmtrade.specmp.feature.configurator.ui"
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
            implementation(project(":core:ui"))

            // Логика конфигураций (feature/configurator), сессии настроек и редактор (feature/device/settings*)
            implementation(project(":feature:configurator"))
            implementation(project(":feature:device:settings"))
            implementation(project(":feature:device:settings-ui"))
            // Подключён ли блок — «Записать в блок» только при связи
            implementation(project(":feature:device:interaction"))
            // Автомобиль облачной конфигурации (CarSummary из каталога)
            implementation(project(":feature:vehicle"))

            implementation(libs.androidx.lifecycle.viewmodel)
            // Nav3-граф конфигуратора (см. docs/navigation.md)
            implementation(libs.compose.nav3)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

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
    }
}

// Строковые ресурсы фичи (см. docs/localization.md)
compose.resources {
    publicResClass = false
    generateResClass = always
}
