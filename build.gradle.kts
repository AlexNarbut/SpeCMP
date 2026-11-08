import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
    id("specmp.detekt").apply(false)
    alias(libs.plugins.spotless)
}

var giteaUsername = System.getenv("GITEA_USERNAME")
var giteaPassword = System.getenv("GITEA_PASSWORD")
val localPropFile = file("local.properties")
val kotlinxDatetimeVersion = libs.versions.kotlinx.datetime.get()

if (localPropFile.exists()) {
    val properties = Properties().apply {
        load(localPropFile.reader())
    }
    if (properties["signing.GITEA_USERNAME"] != null) {
        giteaUsername = properties["signing.GITEA_USERNAME"].toString()
    }
    if (properties["signing.GITEA_PASSWORD"] != null) {
        giteaPassword = properties["signing.GITEA_PASSWORD"].toString()
    }
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
        // usb-serial-for-android (JitPack) — транзитивно из serialport-android Specialist SDK (common 2.0.114+)
        maven {
            url = uri("https://jitpack.io")
            content { includeGroup("com.github.mik3y") }
        }
        maven {
            url = uri("http://git.pandora.pri/api/packages/mobile_guild/maven")
            isAllowInsecureProtocol = true
            credentials {
                username = giteaUsername
                password = giteaPassword
            }
        }
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-datetime") {
                useVersion(kotlinxDatetimeVersion)
                because("AlarmTrade Specialist SDK 1.1.76 iOS artifacts are built against kotlinx-datetime 0.6.2")
            }
        }
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
    }
}

// Качество кода: docs/code-quality.md.
// Один корневой Spotless на весь репозиторий (ktlint + compose-rules). По умолчанию проверяются только файлы,
// изменённые относительно HEAD; для проверки ветки — -PspotlessRatchetFrom=origin/develop.
// В check/build не входит: запускается только spotlessCheck / spotlessApply.
spotless {
    isEnforceCheck = false
    ratchetFrom(providers.gradleProperty("spotlessRatchetFrom").getOrElse("HEAD"))
    kotlin {
        target(
            fileTree(rootDir) {
                include("**/src/**/*.kt")
                exclude("**/build/**", "**/generated/**", "**/.gradle/**", "**/.git/**", "**/.kotlin/**")
            }
        )
        ktlint(libs.versions.ktlint.get())
            .customRuleSets(listOf("io.nlopez.compose.rules:ktlint:${libs.versions.composeRules.get()}"))
    }
    kotlinGradle {
        target(
            fileTree(rootDir) {
                include("*.gradle.kts", "*/*.gradle.kts", "*/*/*.gradle.kts", "*/*/*/*.gradle.kts")
                exclude("**/build/**", "**/.gradle/**", "**/.git/**", "**/.kotlin/**")
            }
        )
        ktlint(libs.versions.ktlint.get())
    }
}

// detekt (build-logic, specmp.detekt) — всем Kotlin-модулям: KMP-библиотекам, composition roots и обёрткам.
// quality/* — инструменты проверок, сами detekt не проходят (detekt-rules подключается к остальным как плагин).
subprojects {
    if (path.startsWith(":quality:")) return@subprojects
    listOf(
        "org.jetbrains.kotlin.multiplatform",
        "org.jetbrains.kotlin.android",
        "org.jetbrains.kotlin.jvm",
        "com.android.application"
    ).forEach { id ->
        pluginManager.withPlugin(id) { pluginManager.apply("specmp.detekt") }
    }
}

// Один конфиг стабильности на все модули с Compose-компилятором: доменные модели собираются без него и иначе
// считаются нестабильными (docs/compose-best-prac.md, §3.5 «Конфиг стабильности»).
subprojects {
    plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        extensions.configure<org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("config/compose/stability.conf"))
        }
    }
}
