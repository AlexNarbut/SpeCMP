import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Compose Multiplatform поверх specmp.kmp.library; модуль без iOS ставит specmp.kmp.android-jvm перед этим плагином.
// Конфиг стабильности подключает корневой build.gradle.kts.
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        if (!pluginManager.hasPlugin("specmp.kmp.android-jvm")) pluginManager.apply("specmp.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        // Без Android-ресурсов composeResources модуля не попадают в APK (MissingResourceException в рантайме).
        if (file("src/commonMain/composeResources").isDirectory) {
            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    androidResources.enable = true
                }
            }
        }
    }
}
