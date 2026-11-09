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

            // material3 — implementation: фичи используют Cmp*-обёртки, не material3
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            // api: NavBackStack — в публичном API навигационных хелперов
            api(libs.compose.nav3)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            implementation(libs.androidx.lifecycle.viewmodel)
            // api: collectAsStateWithLifecycle — сбор state экранов во всех фичах (docs/compose-best-prac.md §4)
            api(libs.androidx.lifecycle.runtime)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Логирование: Logger — часть protected API BaseViewModel, поэтому api
            api(libs.kermit)

            implementation(libs.materialKolor)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activityCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
