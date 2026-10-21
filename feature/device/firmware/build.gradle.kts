import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    android {
        namespace = "ru.alarmtrade.specmp.feature.device.firmware"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:specialist"))

            // ConnectionTransport, BootModeTarget и т.п. — часть публичного API репозиториев фичи
            api(project(":feature:device:interaction"))

            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)

            implementation(libs.koin.core)

            implementation(libs.alarmtrade.specialist)

            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
