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
        namespace = "ru.alarmtrade.specmp.core.localization"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // обязательно для модулей с compose resources (см. docs/build.md)
        androidResources.enable = true
    }

    listOf(
        iosArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreLocalization"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.multiplatformSettings)

            // api: LocalAppLocale — публичный CompositionLocal этого модуля
            api(libs.compose.runtime)
            // api: Strings — публичный класс строк всех фич (docs/localization.md)
            api(libs.compose.resources)
        }
    }
}

// Все строки приложения: composeResources/values*/<префикс фичи>.xml → Strings.string.<ключ>
compose.resources {
    publicResClass = true
    packageOfResClass = "ru.alarmtrade.specmp.core.localization.resources"
    nameOfResClass = "Strings"
    generateResClass = always
}
