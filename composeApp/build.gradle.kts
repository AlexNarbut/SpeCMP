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
            // Core modules
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:network"))
            implementation(project(":core:permissions"))
            implementation(project(":core:lifecycle"))
            implementation(project(":core:specialist"))
            implementation(project(":feature:account"))
            implementation(project(":core:ui"))

            // Feature modules
            implementation(project(":feature:auth-state"))
            implementation(project(":feature:login"))
            implementation(project(":feature:app-settings"))
            implementation(project(":feature:main"))
            implementation(project(":feature:support-chat"))
            implementation(project(":feature:device:selection"))
            implementation(project(":feature:device:interaction"))
            implementation(project(":feature:device:firmware"))
            implementation(project(":feature:device:autostart"))
            implementation(project(":feature:device:state"))
            implementation(project(":feature:device:settings"))
            // Порты файлов настроек и запрос выбора конфигурации (filepicker, слоты конфигуратора)
            implementation(project(":feature:device:presentation-core"))
            implementation(project(":feature:vehicle"))
            implementation(project(":feature:vehicle-ui"))
            implementation(project(":feature:configurator"))
            implementation(project(":feature:configurator-ui"))
            implementation(project(":feature:device:settings-ui"))
            implementation(project(":feature:device:interaction-ui"))
            implementation(project(":feature:advanced-mount"))
            implementation(project(":feature:fast-mount"))

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.compose.ui.tooling.preview)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            // Navigation
            implementation(libs.compose.nav3)
            // ViewModelStore-декоратор Nav3: per-entry ViewModelStore → очистка VM при pop
            implementation(libs.androidx.lifecycle.viewmodel.nav3)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Image loading
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization (for nav3 routes)
            implementation(libs.kotlinx.serialization.json)
        }

        iosMain.dependencies {
            // Дублирование логов в stdout в отладочной сборке (MainViewController)
            implementation(libs.kermit)
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

// Ресурсы SDK на moko-resources (встроенная база настроек и моделей блоков `settings.json` и др.) лежат в
// iOS-klib как `default/resources/<id>.bundle`. Плагин moko-resources в проекте не подключён (у SDK свой форк),
// поэтому bundle копируются в приложение этой задачей — Xcode вызывает её вместе с
// embedAndSignAppleFrameworkForXcode. moko находит bundle по идентификатору, перебирая содержимое приложения.
// Без неё на iOS синхронизация базы падает: модели нет — блок «не поддерживается».
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
