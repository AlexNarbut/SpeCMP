plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature:configurator"))
            api(project(":feature:device:settings"))
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
