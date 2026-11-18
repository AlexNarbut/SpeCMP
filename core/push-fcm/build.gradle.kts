plugins {
    id("specmp.kmp.android")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:push"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.kotlinx.coroutines.play.services)
        }
    }
}
