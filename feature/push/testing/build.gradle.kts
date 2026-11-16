plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature:push"))
            api(project(":core:push"))
            api(project(":core:localization"))
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.core)
        }
    }
}
