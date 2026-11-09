rootProject.name = "build-logic"

// Included build не наследует repositories корневого settings — задаём свои.
dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    // Тот же каталог, что у основной сборки: версии плагинов остаются в gradle/libs.versions.toml.
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}
