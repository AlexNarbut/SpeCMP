import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    id("specmp.kmp.android")
}

kotlin {
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(file("consumer-rules.pro"))
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:push"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.hms.push)
            implementation(libs.agconnect.core)
        }
    }
}
