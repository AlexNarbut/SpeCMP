package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals

class TextFileContentTest {

    @Test
    fun `utf-8 text is decoded with the file name`() {
        val result = decodeTextFile("theme.json", "{\"name\":\"Тёмная\"}".encodeToByteArray(), maxBytes = 1024)

        assertEquals(TextFilePickResult.Picked("theme.json", "{\"name\":\"Тёмная\"}"), result)
    }

    @Test
    fun `utf-8 bom is stripped`() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "{}".encodeToByteArray()

        assertEquals(TextFilePickResult.Picked("a.json", "{}"), decodeTextFile("a.json", bytes, maxBytes = 1024))
    }

    @Test
    fun `empty file is picked as empty text`() {
        assertEquals(TextFilePickResult.Picked("a.json", ""), decodeTextFile("a.json", ByteArray(0), maxBytes = 1024))
    }

    @Test
    fun `file over the limit is too large`() {
        assertEquals(TextFilePickResult.TooLarge, decodeTextFile("a.json", ByteArray(5), maxBytes = 4))
    }

    @Test
    fun `file exactly at the limit is accepted`() {
        assertEquals(
            TextFilePickResult.Picked("a.json", "abcd"),
            decodeTextFile("a.json", "abcd".encodeToByteArray(), 4),
        )
    }

    @Test
    fun `invalid utf-8 fails`() {
        val bytes = byteArrayOf(0xC3.toByte(), 0x28)

        assertEquals(TextFilePickResult.Failed, decodeTextFile("a.json", bytes, maxBytes = 1024))
    }

    @Test
    fun `missing extension is appended`() {
        assertEquals("pandora-theme.json", fileNameWithExtension("pandora-theme", "json"))
    }

    @Test
    fun `existing extension is kept in any case`() {
        assertEquals("theme.JSON", fileNameWithExtension("theme.JSON", "json"))
    }

    @Test
    fun `other extension gets the expected one appended`() {
        assertEquals("theme.txt.json", fileNameWithExtension("theme.txt", "json"))
    }

    @Test
    fun `path and forbidden characters are stripped`() {
        assertEquals("my_theme.json", fileNameWithExtension("../dir/my:theme", "json"))
    }

    @Test
    fun `blank name falls back to a default`() {
        assertEquals("file.json", fileNameWithExtension("  ", "json"))
    }
}
