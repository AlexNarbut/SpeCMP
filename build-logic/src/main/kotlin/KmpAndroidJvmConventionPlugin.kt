import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// KMP без iOS: android (android.kmp.library) и jvm, JVM 17. Напрямую — только модулям без iOS (flasherApp: COM-порта
// на iOS в SDK нет), остальные берут specmp.kmp.library. namespace по умолчанию — из пути модуля без дефисов
// (`:feature:auth-state` → `ru.alarmtrade.specmp.feature.authstate`); иной модуль задаёт в `kotlin { android { } }`.
class KmpAndroidJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun intVersion(alias: String) = libs.findVersion(alias).get().requiredVersion.toInt()

        extensions.configure<KotlinMultiplatformExtension> {
            jvm {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
            }

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
