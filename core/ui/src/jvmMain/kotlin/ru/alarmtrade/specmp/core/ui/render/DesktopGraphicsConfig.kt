package ru.alarmtrade.specmp.core.ui.render

import co.touchlab.kermit.Logger
import java.io.File
import java.io.IOException

object DesktopGraphicsConfig {
    fun apply(restoreSavedSettings: Boolean) {
        DesktopGraphicsSettings.system().apply(restoreSavedSettings)
    }

    fun saveForUpdate() {
        DesktopGraphicsSettings.system().saveForUpdate()
    }
}

internal class DesktopGraphicsSettings(
    private val osName: String,
    private val resourcesDir: File?,
    private val savedFile: File,
    private val property: (String) -> String?,
    private val env: (String) -> String?,
    private val setProperty: (String, String) -> Unit,
) {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val isWindows: Boolean get() = osName.startsWith("Windows", ignoreCase = true)

    private val settingsFile: File? get() = resourcesDir?.resolve(SETTINGS_FILE)

    fun apply(restoreSavedSettings: Boolean) {
        if (!isWindows) return
        if (restoreSavedSettings) restoreSaved()
        val settings = settingsFile?.let(::readSettings).orEmpty()
        if (property(RENDER_API_PROPERTY) == null && env(RENDER_API_ENV) == null) {
            setProperty(RENDER_API_PROPERTY, renderApiOf(settings[RENDER_API_KEY]))
        }
        val gpuPriority = gpuPriorityOf(settings[GPU_PRIORITY_KEY])
        if (gpuPriority != null && GPU_PRIORITY_PROPERTIES.none { property(it) != null }) {
            setProperty(DIRECTX_GPU_PRIORITY_PROPERTY, gpuPriority)
        }
    }

    fun saveForUpdate() {
        val source = settingsFile ?: return
        if (!isWindows || !source.isFile) return
        try {
            savedFile.parentFile?.mkdirs()
            source.copyTo(savedFile, overwrite = true)
        } catch (e: IOException) {
            logger.e(e) { "Graphics settings are not saved before update" }
        } catch (e: SecurityException) {
            logger.e(e) { "Graphics settings are not saved before update" }
        }
    }

    private fun restoreSaved() {
        val target = settingsFile
        if (target == null || !savedFile.isFile) return
        try {
            val merged = readSettings(target).orEmpty() + readSettings(savedFile).orEmpty()
            target.writeText(merged.entries.joinToString(separator = "") { "${it.key}=${it.value}\n" })
            savedFile.delete()
            savedFile.parentFile?.takeIf { it.list()?.isEmpty() == true }?.delete()
        } catch (e: IOException) {
            logger.e(e) { "Saved graphics settings are not restored" }
        } catch (e: SecurityException) {
            logger.e(e) { "Saved graphics settings are not restored" }
        }
    }

    private fun readSettings(file: File): Map<String, String>? = try {
        if (file.isFile) parseGraphicsSettings(file.readText()) else null
    } catch (e: IOException) {
        logger.e(e) { "Graphics settings are not read: $file" }
        null
    }

    companion object {
        const val SETTINGS_FILE = "graphics.settings"
        const val RENDER_API_KEY = "renderApi"
        const val GPU_PRIORITY_KEY = "gpuPriority"
        const val RENDER_API_PROPERTY = "skiko.renderApi"
        const val RENDER_API_ENV = "SKIKO_RENDER_API"
        const val DIRECTX_GPU_PRIORITY_PROPERTY = "skiko.directx.gpu.priority"
        private const val RESOURCES_DIR_PROPERTY = "compose.application.resources.dir"
        private val GPU_PRIORITY_PROPERTIES = listOf("skiko.gpu.priority", DIRECTX_GPU_PRIORITY_PROPERTY)

        // Путь как у апдейтера старого desktop «Pandora Spec»: его сохранённые настройки переживают переход на SpeCMP.
        private const val SAVED_FILE_PATH = "PandoraSpec/tmp/$SETTINGS_FILE"

        fun system(): DesktopGraphicsSettings = DesktopGraphicsSettings(
            osName = System.getProperty("os.name").orEmpty(),
            resourcesDir = System.getProperty(RESOURCES_DIR_PROPERTY)?.let(::File),
            savedFile = File(System.getProperty("user.home"), SAVED_FILE_PATH),
            property = System::getProperty,
            env = System::getenv,
            setProperty = { key, value -> System.setProperty(key, value) },
        )
    }
}

internal fun parseGraphicsSettings(text: String): Map<String, String> = text.lineSequence()
    .map { it.substringBefore('#').filterNot(Char::isWhitespace) }
    .mapNotNull { line ->
        val key = line.substringBefore('=', missingDelimiterValue = "")
        val value = line.substringAfter('=', missingDelimiterValue = "")
        (key to value).takeIf { key.isNotEmpty() && value.isNotEmpty() && '=' !in value }
    }
    .toMap()

// По умолчанию OpenGL: на DirectX-рендере Skiko окно на Windows виснет при прокрутке.
internal fun renderApiOf(value: String?): String = when (value?.uppercase()) {
    "SOFTWARE_COMPAT" -> "SOFTWARE_COMPAT"
    "SOFTWARE_FAST", "DIRECT_SOFTWARE", "SOFTWARE" -> "SOFTWARE_FAST"
    "DIRECT3D" -> "DIRECT3D"
    else -> "OPENGL"
}

internal fun gpuPriorityOf(value: String?): String? = when (value?.uppercase()) {
    "INTEGRATED" -> "integrated"
    "DISCRETE" -> "discrete"
    else -> null
}
