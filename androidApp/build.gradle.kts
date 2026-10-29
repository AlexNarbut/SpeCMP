import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.alarmtrade.specmp.androidApp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ru.alarmtrade.specmp.androidApp"
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
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
    // Язык приложения в контексте Activity (attachBaseContext) — до первой композиции
    implementation(project(":core:localization"))
    implementation(libs.androidx.activityCompose)
    // Box с testTagsAsResourceId в AppActivity
    implementation(libs.compose.foundation)
    // AppActivity — FragmentActivity (нужно core/permissions для bind PermissionsController)
    implementation(libs.androidx.fragment)
}
