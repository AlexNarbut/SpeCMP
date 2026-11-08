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
    alias(libs.plugins.module.graph.assert)
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

// Поиск скопированного кода (PMD CPD): дубли длиннее 100 токенов Kotlin роняют задачу. Корневая задача без
// плагина: CLI PMD в отдельной конфигурации, исходники — объявленный input (совместимо с configuration cache).
// Исключения — только повторы по построению, у каждого причина; подробно в docs/code-quality.md → «CPD».
val cpd: Configuration by configurations.creating

dependencies {
    cpd(libs.pmd.cli)
    cpd(libs.pmd.kotlin)
}

tasks.register<JavaExec>("cpdCheck") {
    group = "verification"
    description = "Дубли Kotlin-кода длиннее 100 токенов (PMD CPD)"
    val sources = fileTree(rootDir) {
        include(
            "core/**/src/**/*.kt",
            "feature/**/src/**/*.kt",
            "composeApp/src/**/*.kt",
            "flasherApp/src/**/*.kt",
            "quality/**/src/**/*.kt"
        )
        exclude(
            "**/build/**",
            "**/generated/**",
            // Тесты: фикстуры и arrange-блоки повторяются намеренно — читаемость теста важнее DRY.
            "**/src/test/**",
            "**/src/*Test/**"
        )
    }
    val fileList = layout.buildDirectory.file("cpd/files.txt")
    inputs.files(sources).withPathSensitivity(PathSensitivity.RELATIVE)
    classpath = cpd
    mainClass = "net.sourceforge.pmd.cli.PmdCli"
    args("cpd", "--language", "kotlin", "--minimum-tokens", "100", "--format", "text")
    argumentProviders.add(CommandLineArgumentProvider { listOf("--file-list", fileList.get().asFile.path) })
    doFirst {
        fileList.get().asFile.apply {
            parentFile.mkdirs()
            writeText(sources.files.sorted().joinToString("\n"))
        }
    }
}

// Направления зависимостей между Gradle-модулями (modules-graph-assert): задача assertModuleGraph. Плагин в корне
// строит граф по всем подпроектам при настройке задачи. Импорты в коде проверяет Konsist (module-direction);
// правила — docs/code-quality.md → «graph-assert».
moduleGraphAssert {
    restricted = arrayOf(
        ":core:.* -X> :feature:.*",
        ":(core|feature):.* -X> :(composeApp|flasherApp)",
        ".* -X> :(androidApp|desktopApp|flasherAndroidApp|flasherDesktopApp)",
        ".* -X> :quality:.*",
        ":quality:.* -X> :(core|feature):.*",
        // От *-ui зависят только composition roots. Временно разрешены рёбра на interaction-ui (уходят в 2b,
        // presentation-core) и configurator-ui → settings-ui (уходит в 4a). Регэксп матчит всю строку
        // «from -X> to», поэтому конец имени модуля слева — пробел, а не $.
        ":feature:(?!(advanced-mount|fast-mount|configurator-ui|device:(autostart|firmware|state|settings-ui)) ).* " +
            "-X> :feature:.*-ui",
        ":feature:(advanced-mount|fast-mount|device:(autostart|firmware|state|settings-ui)) " +
            "-X> :feature:(?!device:interaction-ui$).*-ui",
        ":feature:configurator-ui -X> :feature:(?!device:(interaction-ui|settings-ui)$).*-ui"
    )
    configurations = setOf(
        "api",
        "implementation",
        "testImplementation",
        "commonMainApi",
        "commonMainImplementation",
        "commonTestImplementation",
        "androidMainApi",
        "androidMainImplementation",
        "iosMainApi",
        "iosMainImplementation",
        "jvmMainApi",
        "jvmMainImplementation",
        "jvmTestImplementation"
    )
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
