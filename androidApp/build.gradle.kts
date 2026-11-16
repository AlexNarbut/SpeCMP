import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

// Подпись release — ключом старого «Pandora Specialist» (та же карточка в Google Play): файл `keys/release_key.jks`
// и пароли — только локально, в `local.properties` (`release.storePassword`, `release.keyPassword`). Нет паролей —
// release собирается без подписи.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val releaseStoreFile = file(localProperties.getProperty("release.storeFile") ?: "keys/release_key.jks")
val releaseStorePassword: String? = localProperties.getProperty("release.storePassword")

android {
    namespace = "ru.alarmtrade.specmp.androidApp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ru.alarmtrade.pandoraspecialist"
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }

    signingConfigs {
        if (releaseStorePassword != null && releaseStoreFile.exists()) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = localProperties.getProperty("release.keyAlias") ?: "key"
                keyPassword = localProperties.getProperty("release.keyPassword") ?: releaseStorePassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:push"))
    implementation(project(":core:localization"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.compose.foundation)
    // AppActivity — FragmentActivity (нужно core/permissions для bind PermissionsController)
    implementation(libs.androidx.fragment)
}
