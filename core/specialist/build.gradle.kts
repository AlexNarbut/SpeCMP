plugins {
    id("specmp.kmp.library")
}

kotlin {
    iosArm64 {
        binaries.framework {
            baseName = "CoreSpecialist"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:network"))
            implementation(libs.alarmtrade.specialist)
            // Типы сетевых исключений для SdkErrorMapper (версия — общая с core/network)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
