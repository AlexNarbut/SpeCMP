package ru.alarmtrade.specmp.core.files

import java.awt.GraphicsEnvironment
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.core.files.FileChooserDriver.chooseInDialog
import ru.alarmtrade.specmp.core.files.FileChooserDriver.composed
import ru.alarmtrade.specmp.core.files.FileChooserDriver.dispatchers

class DesktopFileDialogsTest {

    private val directory: File = Files.createTempDirectory("file-dialogs-test").toFile()
    private val chosenFolder: File = File(directory, "Выбранная папка").apply { mkdirs() }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `pdf is saved to the folder and name chosen in the dialog`() {
        if (GraphicsEnvironment.isHeadless()) return
        val exporter = composed { rememberBinaryFileExporter(dispatchers) }

        val result = chooseInDialog(File(chosenFolder, "Мой паспорт")) {
            exporter.save("Паспорт монтажа", BinaryFileType.Pdf, byteArrayOf(1, 2, 3))
        }

        assertEquals(BinaryFileSaveResult.Saved("Мой паспорт.pdf"), result)
        assertContentEquals(byteArrayOf(1, 2, 3), File(chosenFolder, "Мой паспорт.pdf").readBytes())
    }

    @Test
    fun `zip is copied to the folder and name chosen in the dialog`() {
        if (GraphicsEnvironment.isHeadless()) return
        val source = File(directory, "export.zip").apply { writeBytes(byteArrayOf(7, 8)) }
        val exporter = composed { rememberBinaryFileExporter(dispatchers) }

        val result = chooseInDialog(File(chosenFolder, "Архив.zip")) {
            exporter.saveFile("Паспорт монтажа", BinaryFileType.Zip, source.path)
        }

        assertEquals(BinaryFileSaveResult.Saved("Архив.zip"), result)
        assertContentEquals(byteArrayOf(7, 8), File(chosenFolder, "Архив.zip").readBytes())
    }

    @Test
    fun `text is saved to the folder and name chosen in the dialog`() {
        if (GraphicsEnvironment.isHeadless()) return
        val saver = composed { rememberTextFileSaver(dispatchers) }

        val result = chooseInDialog(File(chosenFolder, "settings")) {
            saver.save("default", TextFileType.Json, "{}")
        }

        assertEquals(TextFileSaveResult.Saved("settings.json"), result)
        assertEquals("{}", File(chosenFolder, "settings.json").readText())
    }

    @Test
    fun `text file chosen in the dialog is picked`() {
        if (GraphicsEnvironment.isHeadless()) return
        val file = File(chosenFolder, "config.json").apply { writeText("{\"a\":1}") }
        val picker = composed { rememberTextFilePicker(dispatchers) }

        val result = chooseInDialog(file) { picker.pick(TextFileType.Json) }

        assertEquals(TextFilePickResult.Picked("config.json", "{\"a\":1}"), result)
    }

    @Test
    fun `binary file chosen in the dialog is picked in place`() {
        if (GraphicsEnvironment.isHeadless()) return
        val file = File(chosenFolder, "import.zip").apply { writeBytes(byteArrayOf(5)) }
        val picker = composed { rememberBinaryFilePicker(dispatchers) }

        val result = chooseInDialog(file) { picker.pick(BinaryFileType.Zip) }

        assertEquals(BinaryFilePickResult.Picked("import.zip", file.absolutePath), result)
    }
}
