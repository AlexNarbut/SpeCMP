plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:lifecycle"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
