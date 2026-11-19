package ru.alarmtrade.specmp.core.ui.render

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopGraphicsSettingsTest {
    private val root: File = Files.createTempDirectory("graphics-settings").toFile()
    private val resourcesDir = root.resolve("resources").apply { mkdirs() }
    private val savedFile = root.resolve("home/PandoraSpec/tmp/graphics.settings")
    private val settingsFile = resourcesDir.resolve("graphics.settings")
    private val set = mutableMapOf<String, String>()

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    private fun settings(
        osName: String = "Windows 11",
        resources: File? = resourcesDir,
        properties: Map<String, String> = emptyMap(),
        env: Map<String, String> = emptyMap(),
    ) = DesktopGraphicsSettings(
        osName = osName,
        resourcesDir = resources,
        savedFile = savedFile,
        property = properties::get,
        env = env::get,
        setProperty = { key, value -> set[key] = value },
    )

    private fun writeSaved(text: String) {
        savedFile.parentFile.mkdirs()
        savedFile.writeText(text)
    }

    @Test
    fun `parse skips blank, malformed and commented lines and strips whitespace`() {
        val text = "# comment\n\n renderApi = DIRECT3D # inline\nbroken\n=x\nkey=\na=b=c\r\ngpuPriority\t=discrete\r\n"
        assertEquals(mapOf("renderApi" to "DIRECT3D", "gpuPriority" to "discrete"), parseGraphicsSettings(text))
    }

    @Test
    fun `render api aliases map to skiko names`() {
        assertEquals("SOFTWARE_COMPAT", renderApiOf("software_compat"))
        assertEquals("SOFTWARE_FAST", renderApiOf("SOFTWARE_FAST"))
        assertEquals("SOFTWARE_FAST", renderApiOf("DIRECT_SOFTWARE"))
        assertEquals("SOFTWARE_FAST", renderApiOf("software"))
        assertEquals("OPENGL", renderApiOf("OpenGL"))
        assertEquals("DIRECT3D", renderApiOf("direct3d"))
    }

    @Test
    fun `invalid or missing render api falls back to OpenGL`() {
        assertEquals("OPENGL", renderApiOf("METAL"))
        assertEquals("OPENGL", renderApiOf(null))
    }

    @Test
    fun `gpu priority maps to skiko values, auto and invalid are not set`() {
        assertEquals("integrated", gpuPriorityOf("INTEGRATED"))
        assertEquals("discrete", gpuPriorityOf("Discrete"))
        assertNull(gpuPriorityOf("AUTO"))
        assertNull(gpuPriorityOf("fast"))
        assertNull(gpuPriorityOf(null))
    }

    @Test
    fun `missing settings file sets OpenGL only`() {
        settings().apply(restoreSavedSettings = true)
        assertEquals(mapOf("skiko.renderApi" to "OPENGL"), set)
    }

    @Test
    fun `unknown resources dir sets OpenGL only`() {
        settings(resources = null).apply(restoreSavedSettings = true)
        assertEquals(mapOf("skiko.renderApi" to "OPENGL"), set)
    }

    @Test
    fun `settings file sets render api and gpu priority`() {
        settingsFile.writeText("renderApi=DIRECT3D\ngpuPriority=DISCRETE\n")
        settings().apply(restoreSavedSettings = false)
        assertEquals(mapOf("skiko.renderApi" to "DIRECT3D", "skiko.directx.gpu.priority" to "discrete"), set)
    }

    @Test
    fun `invalid render api in file falls back to OpenGL`() {
        settingsFile.writeText("renderApi=VULKAN\ngpuPriority=AUTO\n")
        settings().apply(restoreSavedSettings = false)
        assertEquals(mapOf("skiko.renderApi" to "OPENGL"), set)
    }

    @Test
    fun `explicit render api property wins`() {
        settingsFile.writeText("renderApi=DIRECT3D\n")
        settings(properties = mapOf("skiko.renderApi" to "SOFTWARE")).apply(restoreSavedSettings = false)
        assertEquals(emptyMap(), set)
    }

    @Test
    fun `explicit render api env wins`() {
        settingsFile.writeText("renderApi=DIRECT3D\n")
        settings(env = mapOf("SKIKO_RENDER_API" to "SOFTWARE")).apply(restoreSavedSettings = false)
        assertEquals(emptyMap(), set)
    }

    @Test
    fun `explicit gpu priority property wins`() {
        settingsFile.writeText("gpuPriority=DISCRETE\n")
        settings(properties = mapOf("skiko.gpu.priority" to "integrated")).apply(restoreSavedSettings = false)
        assertEquals(mapOf("skiko.renderApi" to "OPENGL"), set)
    }

    @Test
    fun `non-Windows is a no-op`() {
        settingsFile.writeText("renderApi=DIRECT3D\n")
        writeSaved("renderApi=SOFTWARE\n")
        settings(osName = "Mac OS X").apply(restoreSavedSettings = true)
        settings(osName = "Linux").saveForUpdate()
        assertEquals(emptyMap(), set)
        assertEquals("renderApi=DIRECT3D\n", settingsFile.readText())
        assertEquals("renderApi=SOFTWARE\n", savedFile.readText())
    }

    @Test
    fun `saved user settings merge over shipped file and are deleted`() {
        settingsFile.writeText("renderApi=OPENGL\ngpuPriority=AUTO\nnewKey=1\n")
        writeSaved("renderApi=DIRECT3D\ngpuPriority=INTEGRATED\n")
        savedFile.parentFile.resolve("other.bin").writeText("x")
        settings().apply(restoreSavedSettings = true)
        assertEquals(
            mapOf("renderApi" to "DIRECT3D", "gpuPriority" to "INTEGRATED", "newKey" to "1"),
            parseGraphicsSettings(settingsFile.readText()),
        )
        assertEquals(mapOf("skiko.renderApi" to "DIRECT3D", "skiko.directx.gpu.priority" to "integrated"), set)
        assertFalse(savedFile.exists())
        assertTrue(savedFile.parentFile.resolve("other.bin").exists())
    }

    @Test
    fun `empty tmp dir is removed after restore, PandoraSpec dir is kept`() {
        writeSaved("renderApi=SOFTWARE\n")
        settings().apply(restoreSavedSettings = true)
        assertEquals(mapOf("renderApi" to "SOFTWARE"), parseGraphicsSettings(settingsFile.readText()))
        assertFalse(savedFile.parentFile.exists())
        assertTrue(savedFile.parentFile.parentFile.exists())
    }

    @Test
    fun `saved settings are kept when restore is off or resources dir is unknown`() {
        settingsFile.writeText("renderApi=OPENGL\n")
        writeSaved("renderApi=DIRECT3D\n")
        settings().apply(restoreSavedSettings = false)
        settings(resources = null).apply(restoreSavedSettings = true)
        assertEquals("renderApi=OPENGL\n", settingsFile.readText())
        assertTrue(savedFile.exists())
    }

    @Test
    fun `save for update copies settings file over previous copy`() {
        settingsFile.writeText("renderApi=DIRECT3D\n")
        writeSaved("renderApi=SOFTWARE\n")
        settings().saveForUpdate()
        assertEquals("renderApi=DIRECT3D\n", savedFile.readText())
    }

    @Test
    fun `save for update without settings file does nothing`() {
        settings().saveForUpdate()
        assertFalse(savedFile.exists())
    }
}
