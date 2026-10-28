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
        namespace = "ru.alarmtrade.specmp.core.ui"
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
            baseName = "CoreUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            // Жизненный цикл приложения для BaseViewModel (onAppForeground / onAppBackground)
            implementation(project(":core:lifecycle"))

            // Compose — api so consumers get runtime/foundation/ui transitively
            // material3 stays implementation: feature modules use Cmp* wrappers, not material3 directly
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            // Nav3 — api: тип NavBackStack фигурирует в публичном API навигационных
            // хелперов (core.ui.navigation.NavBackStackSafe), доступных всем фичам.
            api(libs.compose.nav3)
            implementation(libs.compose.material3)
            // Каталог material-иконок для CmpIcons (резолвится в CmpIconsResolver)
            implementation(compose.materialIconsExtended)

            // Image loading — единый ImageLoader приложения, обёртки CmpAsyncImage/CmpSubcomposeAsyncImage
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Логирование: Logger — часть protected API BaseViewModel, поэтому api
            api(libs.kermit)

            // Material Kolor
            implementation(libs.materialKolor)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activityCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Галерея компонентов кита (PNG без окна) — см. KitGalleryTest
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

// Общие строковые ресурсы (см. docs/localization.md, правило 2)
compose.resources {
    publicResClass = false
    generateResClass = always
}
