plugins {
    id("specmp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
