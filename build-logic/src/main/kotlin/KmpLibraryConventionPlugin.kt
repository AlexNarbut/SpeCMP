import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// KMP-библиотека: specmp.kmp.android-jvm + iosArm64.
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("specmp.kmp.android-jvm")
        extensions.configure<KotlinMultiplatformExtension> { iosArm64() }
    }
}
