plugins {
    id("specmp.kmp.compose")
}

kotlin {
    iosArm64 {
        binaries.framework {
            baseName = "CoreLocalization"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(libs.koin.core)

            api(libs.compose.runtime)
            // api: Strings — публичный класс строк всех фич (docs/localization.md)
            api(libs.compose.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ru.alarmtrade.specmp.core.localization.resources"
    nameOfResClass = "Strings"
    generateResClass = always
}
