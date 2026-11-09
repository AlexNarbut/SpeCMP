@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package ru.alarmtrade.specmp.filepicker

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToURL
import ru.alarmtrade.specmp.core.common.file.safeFileName

internal fun writeTempFile(dirName: String, fileName: String, bytes: ByteArray): NSURL? {
    val directory = "${NSTemporaryDirectory()}$dirName/${NSUUID().UUIDString}"
    val created = NSFileManager.defaultManager.createDirectoryAtPath(
        path = directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    if (!created) return null
    val url = NSURL.fileURLWithPath("$directory/${safeFileName(fileName)}")
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    return if (data.writeToURL(url, atomically = true)) url else null
}
