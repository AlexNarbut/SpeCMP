import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import java.net.URI
import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("specmp.kmp.library")
}

// Голос Piper ru_RU-dmitri-medium из rhasspy/piper-voices (новая версия, дообучена от hifi_tts_92). В git не хранится:
// скачивается при сборке по закреплённой ревизии, сверяется SHA-256, кэш — вне build/ (переживает clean).
val piperVoicesRevision = "e929968c3761f1c281875787685a745863db838d"
val piperVoiceFiles = mapOf(
    "ru_RU-dmitri-medium.onnx" to "9ed9941bac7373f2ec1be36627e0e71a349a59ca5522d4363fd50701db05193a",
    "ru_RU-dmitri-medium.onnx.json" to "c3741c9c1558fc38e902fd80d39155c32a7d019d7d17e3a43eac66b7bd3fbe09",
)

abstract class DownloadPiperVoice : DefaultTask() {
    @get:Input
    abstract val baseUrl: Property<String>

    @get:Input
    abstract val files: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun download() {
        files.get().forEach { (name, sha256) ->
            val target = outputDir.file("piper/$name").get().asFile
            if (target.isFile && sha256(target) == sha256) return@forEach
            target.parentFile.mkdirs()
            val partial = File(target.parentFile, "$name.part")
            val url = "${baseUrl.get()}/$name"
            try {
                URI(url).toURL().openStream().use { input -> partial.outputStream().use { input.copyTo(it) } }
            } catch (e: java.io.IOException) {
                partial.delete()
                throw GradleException(
                    "Не удалось скачать модель голоса $url (${e.message}). Нужна сеть при первой сборке; " +
                        "или положите файл вручную в ${target.absolutePath} (SHA-256 $sha256).",
                    e,
                )
            }
            val actual = sha256(partial)
            if (actual != sha256) {
                partial.delete()
                throw GradleException("SHA-256 $name не совпал: ожидали $sha256, получили $actual ($url).")
            }
            partial.renameTo(target)
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

val downloadPiperVoice = tasks.register<DownloadPiperVoice>("downloadPiperVoice") {
    baseUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/$piperVoicesRevision/ru/ru_RU/dmitri/medium"
    files = piperVoiceFiles
    outputDir = gradle.gradleUserHomeDir.resolve("caches/specmp-piper/$piperVoicesRevision")
}

kotlin {
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.files.add(file("consumer-rules.pro"))
        }
    }

    // Нейросетевой голос (onnxruntime Java API) — общий код Android и desktop: у обоих артефактов один API.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withCompilations { it.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }

        getByName("jvmAndAndroidMain") {
            resources.srcDir(downloadPiperVoice)
            dependencies { compileOnly(libs.onnxruntime.jvm) }
        }
        androidMain.dependencies {
            implementation(libs.onnxruntime.android)
        }
        jvmMain.dependencies {
            implementation(libs.onnxruntime.jvm)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
