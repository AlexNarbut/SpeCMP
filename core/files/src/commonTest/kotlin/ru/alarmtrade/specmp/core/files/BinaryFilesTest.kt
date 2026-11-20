package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BinaryFilesTest {

    @Test
    fun `binary save results map to text save results`() {
        assertEquals(TextFileSaveResult.Saved("a.json"), BinaryFileSaveResult.Saved("a.json").toTextResult())
        assertEquals(TextFileSaveResult.Cancelled, BinaryFileSaveResult.Cancelled.toTextResult())
        assertEquals(TextFileSaveResult.Failed, BinaryFileSaveResult.Failed.toTextResult())
    }

    @Test
    fun `exported file name gets the type extension`() {
        assertEquals("Паспорт № 1.pdf", fileNameWithExtension("Паспорт № 1", BinaryFileType.Pdf.extension))
    }

    @Test
    fun `zip is picked by any archive mime type`() {
        val zip = BinaryFileType.Zip

        assertEquals("zip", zip.extension)
        assertEquals("application/zip", zip.mimeType)
        assertTrue("application/x-zip-compressed" in zip.pickMimeTypes)
        assertTrue("application/octet-stream" in zip.pickMimeTypes)
    }

    @Test
    fun `pdf is picked by its own mime type`() {
        assertEquals(listOf("application/pdf"), BinaryFileType.Pdf.pickMimeTypes)
    }

    @Test
    fun `picked file keeps a safe name without path`() {
        assertEquals("Паспорта.zip", pickedFileName("../tmp/Паспорта.zip", BinaryFileType.Zip))
        assertEquals("file.zip", pickedFileName(null, BinaryFileType.Zip))
        assertEquals("archive.bin", pickedFileName("archive.bin", BinaryFileType.Zip))
    }
}
