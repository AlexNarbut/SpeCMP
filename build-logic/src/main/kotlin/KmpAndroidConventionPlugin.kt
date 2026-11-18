import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// KMP только с android-таргетом: напрямую — Android-only модулям вендорных SDK (core/push-fcm, core/push-hms),
// остальным — specmp.kmp.android-jvm / specmp.kmp.library. namespace по умолчанию — из пути модуля без дефисов.
class KmpAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun intVersion(alias: String) = libs.findVersion(alias).get().requiredVersion.toInt()

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                namespace = defaultNamespace()
                compileSdk = intVersion("android-compileSdk")
                minSdk = intVersion("android-minSdk")
                compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
            }
        }
    }

    private fun Project.defaultNamespace(): String =
        "ru.alarmtrade.specmp." + path.removePrefix(":").replace(':', '.').replace("-", "")
}
