// Набор правил detekt `specmp` (config/detekt/detekt.yml). Подключается ко всем Kotlin-модулям
// через detektPlugins в build-logic (specmp.detekt); сам модуль detekt не проверяет.
plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    // SpecmpRuleSetProviderTest сверяет набор правил с config/detekt/detekt.yml.
    systemProperty("specmp.detektConfig", rootProject.file("config/detekt/detekt.yml").absolutePath)
    inputs.file(rootProject.file("config/detekt/detekt.yml"))
}
