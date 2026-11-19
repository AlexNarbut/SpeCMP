import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.alarmtrade.specmp.flasher.androidApp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ru.alarmtrade.specmp.flasher"
        versionCode = libs.versions.flasher.versionCode.get().toInt()
        versionName = libs.versions.flasher.versionName.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":flasherApp"))
    implementation(project(":core:localization"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.compose.foundation)
    // FlasherActivity — FragmentActivity (нужно core/permissions для bind PermissionsController)
    implementation(libs.androidx.fragment)
}
