import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("specmp.kmp.library")
}

kotlin {
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(file("consumer-rules.pro"))
        }
    }

    // Нейросетевой голос (onnxruntime Java API) — общий код Android и desktop: у обоих артефактов один API.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withCompilations { it.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:lifecycle"))
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }

        getByName("jvmAndAndroidMain").dependencies {
            compileOnly(libs.onnxruntime.jvm)
        }
        androidMain.dependencies {
            implementation(libs.onnxruntime.android)
        }
        jvmMain.dependencies {
            implementation(libs.onnxruntime.jvm)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
