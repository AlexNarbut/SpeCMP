plugins {
    id("specmp.kmp.library")
}

kotlin {
    iosArm64 {
        binaries.framework {
            baseName = "CoreNetwork"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        // HTTP-движки Ktor для всего процесса: Specialist SDK и Coil (coil-network-ktor3) используют ktor-client,
        // но движок не тянут — без этих зависимостей сеть SDK и загрузка картинок не работают.
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
