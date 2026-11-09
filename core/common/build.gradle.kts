plugins {
    id("specmp.kmp.library")
}

kotlin {
    iosArm64 {
        binaries.framework {
            baseName = "CoreCommon"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.koin.core)
            // Settings — публичный тип AppSettingsFactory, поэтому api
            api(libs.multiplatformSettings)
            api(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
