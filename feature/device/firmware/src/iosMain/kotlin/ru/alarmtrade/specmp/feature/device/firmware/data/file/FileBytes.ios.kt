package ru.alarmtrade.specmp.feature.device.firmware.data.file

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

// COM-порта на iOS нет (SDK), чтение и запись нужны только для полноты expect.
@OptIn(ExperimentalForeignApi::class)
internal actual fun readFileBytes(path: String): ByteArray {
    val data = requireNotNull(NSData.dataWithContentsOfFile(path)) { "Cannot read file" }
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return bytes
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun writeTempFile(name: String, bytes: ByteArray): String {
    val path = NSTemporaryDirectory() + name
    val data = if (bytes.isEmpty()) {
        NSData()
    } else {
        bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    }
    check(data.writeToFile(path, atomically = true)) { "Cannot write file" }
    return path
}
