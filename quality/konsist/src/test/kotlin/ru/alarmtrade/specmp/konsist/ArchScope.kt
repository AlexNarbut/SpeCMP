package ru.alarmtrade.specmp.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Что видят правила: [project] — весь код (с тестами), [production] — продакшен модулей core/feature/приложений без
 * модулей testing, [root] — корень проекта (build.gradle.kts модулей). Боевой — [ArchScope.production], в тестах обхода —
 * [fixture] во временном каталоге с той же раскладкой путей.
 */
internal class ArchScope(val root: File, val project: KoScope, productionScope: KoScope) {
    val production: KoScope = productionScope.slice {
        (it.isCore || it.isFeature || it.modulePath in APP_MODULES) && !it.isTestingModule
    }
    val libraryFiles = production.files.filter { it.isCore || it.isFeature }
    val classes = production.classes(includeNested = true)

    companion object {
        val production: ArchScope by lazy {
            ArchScope(projectRootOf(File("").absoluteFile), Konsist.scopeFromProject(), Konsist.scopeFromProduction())
        }
    }
}

/** Мини-проект из пар «путь от корня репозитория → код»: `fixture("feature/foo/src/commonMain/kotlin/A.kt" to "…")`. */
internal fun fixture(vararg files: Pair<String, String>): ArchScope {
    val root = kotlin.io.path.createTempDirectory("konsist-fixture").toFile().canonicalFile
    root.deleteOnExit()
    File(root, SETTINGS).writeText("")
    files.forEach { (path, code) ->
        File(root, path).apply { parentFile.mkdirs() }.writeText(code.trimIndent())
    }
    val project = Konsist.scopeFromExternalDirectory(root.path)
    return ArchScope(root, project, project.slice { !it.isTestSource })
}

private const val SETTINGS = "settings.gradle.kts"

private val roots = ConcurrentHashMap<String, File>()

private fun projectRootOf(start: File): File =
    generateSequence(start) { it.parentFile }.first { File(it, SETTINGS).isFile }

// Путь модуля от корня проекта до src/ (`feature/device/settings-ui`): как moduleName Konsist, но и для фикстур.
internal val KoFileDeclaration.modulePath: String
    get() {
        val dir = File(path).parentFile
        val root = roots.getOrPut(dir.path) { projectRootOf(dir) }
        return File(path).relativeTo(root).invariantSeparatorsPath.substringBefore("/src/")
    }
