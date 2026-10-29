package ru.alarmtrade.specmp.feature.device.firmware.data.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

// COM-порта на iOS нет (SDK), чтение нужно только для полноты expect.
@OptIn(ExperimentalForeignApi::class)
internal actual fun readFileBytes(path: String): ByteArray {
    val data = requireNotNull(NSData.dataWithContentsOfFile(path)) { "Cannot read file" }
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return bytes
}
