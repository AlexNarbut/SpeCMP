import com.android.build.api.variant.impl.VariantOutputImpl
import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Магазин: google — FCM и Google Play, huawei — HMS Push и AppGallery. Вендорный код — только в модуле
    // своего флейвора (core/push-fcm, core/push-hms), KMP-модули флейворов не поддерживают.
    flavorDimensions += "store"
    productFlavors {
        create("google") { dimension = "store" }
        create("huawei") { dimension = "store" }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Имя APK с магазином и версией: spec_android_google_10.0.2(1003).apk; у debug — суффикс _debug.
// VariantOutputImpl — внутренний класс AGP: публичного API для имени выходного APK нет.
androidComponents {
    onVariants { variant ->
        val suffix = if (variant.buildType == "release") "" else "_${variant.buildType}"
        val store = variant.flavorName
        variant.outputs.forEach { output ->
            (output as? VariantOutputImpl)?.outputFileName?.set(
                output.versionName.zip(output.versionCode) { name, code ->
                    "spec_android_${store}_$name($code)$suffix.apk"
                },
            )
        }
    }
}

// google-services.json есть только у флейвора google; huawei собирается без Firebase.
googleServices {
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.IGNORE
}

// SDK чужого магазина в сборке — отказ модерации и лишний код: google — без com.huawei, huawei — без Firebase.
// Исключение — служебные firebase-components/encoders/annotations: их тянет ML Kit (core/barcode, модель в APK),
// это DI-каркас без сервисов Firebase. Проверяется release-classpath; задача входит в check, release-сборку и pre-push.
val storeForbiddenGroups = mapOf("google" to "com.huawei", "huawei" to "com.google.firebase")
val mlKitFirebaseInfra = setOf(
    "firebase-annotations",
    "firebase-components",
    "firebase-encoders",
    "firebase-encoders-json",
    "firebase-encoders-proto",
)
val checkStoreDependencies = tasks.register("checkStoreDependencies") {
    group = "verification"
    description = "Release-classpath флейвора без SDK другого магазина"
    val allowed = mlKitFirebaseInfra
    val violations = storeForbiddenGroups.map { (store, forbidden) ->
        configurations.named("${store}ReleaseRuntimeClasspath")
            .flatMap { it.incoming.resolutionResult.rootComponent }
            .map { root ->
                val seen = mutableSetOf<ResolvedComponentResult>()
                val queue = ArrayDeque(listOf(root))
                while (queue.isNotEmpty()) {
                    queue.removeFirst().dependencies
                        .filterIsInstance<ResolvedDependencyResult>()
                        .map { it.selected }
                        .filter(seen::add)
                        .forEach(queue::addLast)
                }
                seen.mapNotNull { it.moduleVersion }
                    .filter { it.group == forbidden || it.group.startsWith("$forbidden.") }
                    .filterNot { it.group == "com.google.firebase" && it.name in allowed }
                    .map { "$store: ${it.group}:${it.name}:${it.version}" }
            }
    }
    doLast {
        val found = violations.flatMap { it.get() }.sorted()
        if (found.isNotEmpty()) {
            throw GradleException("SDK другого магазина в release-classpath флейвора:\n" + found.joinToString("\n"))
        }
    }
}
tasks.named("check") { dependsOn(checkStoreDependencies) }
tasks.matching { it.name.matches(Regex("(assemble|bundle)\\w+Release")) }.configureEach {
    dependsOn(checkStoreDependencies)
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":core:common"))
    implementation(project(":core:push"))
    implementation(project(":core:localization"))
    implementation(libs.koin.core)
    // Аксессоров googleImplementation/huaweiImplementation для флейворов этого же скрипта в Kotlin DSL нет.
    "googleImplementation"(project(":core:push-fcm"))
    "huaweiImplementation"(project(":core:push-hms"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.compose.foundation)
    // AppActivity — FragmentActivity (нужно core/permissions для bind PermissionsController)
    implementation(libs.androidx.fragment)
}
