// Только тесты: Konsist читает исходники репозитория как текст.
plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = false
    }
    // Konsist читает исходники с диска: без этих входов тест считался бы актуальным (или брался из build cache)
    // после правок кода в других модулях.
    listOf(
        "core",
        "feature",
        "composeApp",
        "flasherApp",
        "androidApp",
        "desktopApp",
        "flasherAndroidApp",
        "flasherDesktopApp",
    ).forEach { dir ->
        inputs.files(
            fileTree(rootDir.resolve(dir)) {
                include("**/src/**/*.kt", "**/build.gradle.kts")
                exclude("**/build/**")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("sources-$dir")
    }
}
