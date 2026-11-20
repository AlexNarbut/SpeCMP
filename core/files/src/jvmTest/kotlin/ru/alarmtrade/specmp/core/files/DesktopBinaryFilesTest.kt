package ru.alarmtrade.specmp.core.files

import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DesktopBinaryFilesTest {

    private val directory: File = Files.createTempDirectory("binary-files-test").toFile()

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `file is copied to the chosen place with the type extension`() {
        val source = File(directory, "export.zip").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val saved = copyFileTo(source.path, File(directory, "Паспорта"), BinaryFileType.Zip)

        assertEquals("Паспорта.zip", saved.name)
        assertContentEquals(byteArrayOf(1, 2, 3), saved.readBytes())
    }

    @Test
    fun `copy over an existing file replaces it`() {
        val source = File(directory, "export.zip").apply { writeBytes(byteArrayOf(9)) }
        File(directory, "old.zip").writeBytes(ByteArray(100))

        val saved = copyFileTo(source.path, File(directory, "old.zip"), BinaryFileType.Zip)

        assertContentEquals(byteArrayOf(9), saved.readBytes())
    }

    @Test
    fun `missing source fails without creating the target`() {
        val target = File(directory, "Паспорта.zip")

        assertFailsWith<IOException> { copyFileTo(File(directory, "none.zip").path, target, BinaryFileType.Zip) }

        assertFalse(target.exists())
    }
}
