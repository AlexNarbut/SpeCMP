package ru.alarmtrade.specmp.feature.device.firmware.data.file

import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo

// Заголовок .frm (TFirmwareHeaderComm, little-endian): длина файла, длина заголовка, тип (8 байт),
// имя системы (16 байт), версия (8 байт). SDK (`PcBaseFrmFirmware`) читает из него только длины и тип —
// имя и версию разбираем здесь; при изменении формата сверять с SDK.
private const val HEADER_LENGTH_POSITION = 4
private const val NAME_POSITION = 16
private const val NAME_SIZE = 16
private const val VERSION_POSITION = NAME_POSITION + NAME_SIZE
private const val VERSION_SIZE = 8
private const val BYTE_MASK = 0xFF
private const val BITS_PER_BYTE = 8
private const val UINT_SIZE = 4

internal fun parseFrmFileInfo(bytes: ByteArray): FrmFileInfo? {
    if (bytes.size < VERSION_POSITION) return null
    if (bytes.uintAt(0) != bytes.size.toLong()) return null
    val headerLength = bytes.uintAt(HEADER_LENGTH_POSITION)
    if (headerLength < VERSION_POSITION || headerLength > bytes.size) return null
    val name = bytes.textAt(NAME_POSITION, NAME_SIZE) ?: return null
    val version = if (headerLength >=
        VERSION_POSITION + VERSION_SIZE
    ) {
        bytes.textAt(VERSION_POSITION, VERSION_SIZE)
    } else {
        null
    }
    return FrmFileInfo(systemName = name, version = version)
}

private fun ByteArray.uintAt(position: Int): Long = (0 until UINT_SIZE).fold(0L) { acc, i ->
    acc or
        ((this[position + i].toLong() and BYTE_MASK.toLong()) shl (BITS_PER_BYTE * i))
}

// ASCII-поле, дополненное пробелами или нулями; пустое — null.
private fun ByteArray.textAt(position: Int, size: Int): String? =
    decodeToString(position, position + size).substringBefore('\u0000').trim().ifEmpty { null }
