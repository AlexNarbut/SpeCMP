plugins {
    `kotlin-dsl`
}

// compileOnly: AGP и KGP нужны только для компиляции конвенций; в рантайме их классы приходят из classpath корневого
// проекта (alias(...).apply(false) в корневом build.gradle.kts), иначе плагины загрузятся дважды.
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    // specmp.detekt применяет dev.detekt к модулям — плагин нужен в рантайме.
    implementation(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "specmp.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "specmp.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("detekt") {
            id = "specmp.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
