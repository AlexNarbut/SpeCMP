package ru.alarmtrade.specmp.core.files

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
