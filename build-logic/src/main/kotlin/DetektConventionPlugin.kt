import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

// detekt для одного Kotlin-модуля; применяется из корневого build.gradle.kts (docs/code-quality.md → «detekt»).
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("dev.detekt")

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig.set(true)
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            parallel.set(true)
        }

        dependencies.add("detektPlugins", dependencies.project(mapOf("path" to ":quality:detekt-rules")))

        // Плагин detekt вешает обычный `detekt` на check — отвязываем: анализ запускается только через detektCheck.
        tasks.matching { it.name == "check" }.configureEach {
            setDependsOn(
                dependsOn.filterNot {
                    (it as? TaskProvider<*>)?.name == "detekt" || (it as? Task)?.name == "detekt"
                },
            )
        }

        // Сгенерированные source set'ы (Compose Resources, BuildConfig) сами лежат в build/ — glob относительно их
        // корня не срабатывает, поэтому отсекаем ещё и по абсолютному пути.
        tasks.withType<Detekt>().configureEach {
            exclude("**/build/**", "**/generated/**")
            exclude { it.file.absolutePath.contains("${File.separator}build${File.separator}") }
        }

        // Голая `detekt` в KMP ничего не запускает: detektCheck собирает задачи с type resolution по production- и
        // host-test-компиляциям (dev-компиляция Compose Hot Reload не анализируется) и iosMain без типов.
        tasks.register("detektCheck") {
            group = "verification"
            description = "detekt с type resolution по production- и host-test-компиляциям"
            dependsOn(
                tasks.matching {
                    if (plugins.hasPlugin("com.android.application")) {
                        // debug-варианты всех флейворов: detektDebug, detektGoogleDebug, detektHuaweiDebugUnitTest…
                        it.name.matches(Regex("detekt(?!Baseline)\\w*Debug(UnitTest)?"))
                    } else {
                        it.name in setOf(
                            "detektMainAndroid",
                            "detektMainJvm",
                            "detektTestJvm",
                            "detektIosMainSourceSet",
                            "detektMain",
                            "detektTest",
                        )
                    }
                },
            )
        }
    }
}
