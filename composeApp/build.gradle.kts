import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("specmp.kmp.compose")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "ru.alarmtrade.specmp"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:network"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))
            implementation(project(":core:specialist"))
            implementation(project(":feature:account"))
            implementation(project(":core:ui"))

            implementation(project(":feature:auth-state"))
            implementation(project(":feature:login"))
            implementation(project(":feature:app-settings"))
            implementation(project(":feature:main"))
            implementation(project(":feature:support-chat"))
            implementation(project(":feature:tech-requests"))
            implementation(project(":feature:product-info"))
            implementation(project(":feature:device:selection"))
            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:firmware"))
            implementation(project(":feature:device:autostart"))
            implementation(project(":feature:device:state"))
            implementation(project(":feature:device:peripherals"))
            implementation(project(":feature:device:settings"))
            implementation(project(":feature:device:presentation-core"))
            implementation(project(":feature:vehicle"))
            implementation(project(":feature:vehicle-ui"))
            implementation(project(":feature:configurator"))
            implementation(project(":feature:configurator-ui"))
            implementation(project(":feature:device:settings-ui"))
            implementation(project(":feature:device:interaction-ui"))
            implementation(project(":feature:advanced-mount"))
            implementation(project(":feature:fast-mount"))
            implementation(project(":feature:mount-assistant"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            implementation(libs.compose.nav3)
            implementation(libs.androidx.lifecycle.viewmodel.nav3)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.serialization.json)
        }

        iosMain.dependencies {
            implementation(libs.kermit)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":feature:device:interaction:testing"))
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activityCompose)
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "composeApp"
                    isStatic = true
                }
            }
        }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

// Плагин moko-resources не подключён (у SDK свой форк): bundle ресурсов SDK из iOS-klib копирует в приложение эта
// задача (Xcode зовёт её с embedAndSignAppleFrameworkForXcode); без них на iOS нет базы моделей блоков.
val copyMokoBundlesToXcode by tasks.registering(Copy::class) {
    description = "Copies moko-resources bundles from iOS klib dependencies into the Xcode app bundle"
    val klibs = configurations.named("iosArm64CompileKlibraries")
    from(klibs.map { config -> config.filter { it.extension == "klib" }.map { zipTree(it) } }) {
        include("default/resources/*.bundle/**")
        eachFile { path = path.removePrefix("default/resources/") }
    }
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    into(
        providers.environmentVariable("TARGET_BUILD_DIR")
            .zip(providers.environmentVariable("UNLOCALIZED_RESOURCES_FOLDER_PATH")) { dir, path -> "$dir/$path" },
    )
}
