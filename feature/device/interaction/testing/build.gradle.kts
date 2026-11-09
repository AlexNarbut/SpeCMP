plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature:device:interaction"))
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.test)
            implementation(libs.koin.core)
        }
    }
}
