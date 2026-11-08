plugins {
    id("specmp.kmp.compose")
}

kotlin {
    iosArm64 {
        binaries.framework {
            baseName = "CoreUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            // Жизненный цикл приложения для LegacyBaseViewModel (onAppForeground / onAppBackground)
            implementation(project(":core:lifecycle"))
            // Сохранённая тема приложения (AppThemeStore)
            implementation(libs.multiplatformSettings)

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
            // api: collectAsStateWithLifecycle — сбор state экранов во всех фичах (docs/compose-best-prac.md §4)
            api(libs.androidx.lifecycle.runtime)

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
            implementation(libs.kotlinx.coroutines.test)
        }

        // Галерея компонентов кита (PNG без окна) — см. KitGalleryTest
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
