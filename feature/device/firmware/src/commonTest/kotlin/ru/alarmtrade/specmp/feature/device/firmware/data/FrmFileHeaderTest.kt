package ru.alarmtrade.specmp.feature.device.firmware.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.device.firmware.data.file.parseFrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo

class FrmFileHeaderTest {

    // Как у Brelok_R480_dbg_v100.frm: заголовок 56 байт, тип "PLD", имя "grc-key", версия "VER1.00".
    private fun frm(
        size: Int = 100,
        headerLength: Int = 56,
        name: String = "grc-key",
        version: String = "VER1.00 "
    ): ByteArray {
        val bytes = ByteArray(size)
        fun putUInt(position: Int, value: Int) = repeat(4) { bytes[position + it] = (value shr (8 * it)).toByte() }
        fun putText(position: Int, length: Int, text: String) =
            text.padEnd(length, ' ').encodeToByteArray().copyInto(bytes, position, 0, length)
        putUInt(0, size)
        putUInt(4, headerLength)
        putText(8, 8, "PLD")
        putText(16, 16, name)
        putText(32, 8, version)
        return bytes
    }

    @Test
    fun `name and version are read from the header without padding`() {
        assertEquals(FrmFileInfo("grc-key", "VER1.00"), parseFrmFileInfo(frm()))
    }

    @Test
    fun `header without the version field gives null version`() {
        assertEquals(FrmFileInfo("grc-key", null), parseFrmFileInfo(frm(headerLength = 32)))
    }

    @Test
    fun `wrong total length or short file is not frm`() {
        val bytes = frm()
        bytes[0] = 99
        assertNull(parseFrmFileInfo(bytes))
        assertNull(parseFrmFileInfo(ByteArray(10)))
    }

    @Test
    fun `device name is compared without padding and case`() {
        assertTrue(FrmFileInfo("grc-key", null).isFor("GRC-KEY     "))
    }
}
