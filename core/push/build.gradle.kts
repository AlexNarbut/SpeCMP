import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    id("specmp.kmp.library")
}

kotlin {
    // Иконки уведомлений — android-ресурсы модуля (res/drawable).
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.kotlinx.coroutines.play.services)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
