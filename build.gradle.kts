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
    alias(libs.plugins.detekt).apply(false)
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

// detekt подключается к каждому подпроекту: у каждого модуля свои source set'ы и type resolution.
subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        parallel.set(true)
    }

    // Плагин detekt вешает обычный `detekt` на check — отвязываем: анализ запускается только через detektCheck.
    tasks.matching { it.name == "check" }.configureEach {
        setDependsOn(
            dependsOn.filterNot {
                (it as? TaskProvider<*>)?.name == "detekt" || (it as? Task)?.name == "detekt"
            }
        )
    }

    // Compose Resources, BuildConfig и прочий генерируемый код лежат в build/ и попадают в type-resolution задачи.
    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        exclude("**/build/**", "**/generated/**")
        // Шаблоны выше — относительно корня source set; у сгенерированных source set'ов (аксессоры Strings) сам корень
        // лежит в build/, поэтому отсекаем по абсолютному пути.
        exclude { it.file.absolutePath.contains("${File.separator}build${File.separator}") }
    }

    // Агрегатор `detekt` в KMP ничего не запускает. detektCheck собирает задачи с type resolution по production- и
    // host-test-компиляциям (dev-компиляция Compose Hot Reload не анализируется); iosMain — отдельной
    // source-set-задачей (без типов). В check и в сборку APK detekt не входит.
    tasks.register("detektCheck") {
        group = "verification"
        description = "detekt с type resolution по production- и host-test-компиляциям"
        dependsOn(
            tasks.matching {
                val names = if (plugins.hasPlugin("com.android.application")) {
                    // androidApp: только debug-вариант и его unit-тесты
                    setOf("detektDebug", "detektDebugUnitTest")
                } else {
                    setOf(
                        // KMP: commonMain + androidMain, commonMain + jvmMain, commonTest + jvmTest, iosMain
                        "detektMainAndroid",
                        "detektMainJvm",
                        "detektTestJvm",
                        "detektIosMainSourceSet",
                        // desktopApp (kotlin("jvm"))
                        "detektMain",
                        "detektTest"
                    )
                }
                it.name in names
            }
        )
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
