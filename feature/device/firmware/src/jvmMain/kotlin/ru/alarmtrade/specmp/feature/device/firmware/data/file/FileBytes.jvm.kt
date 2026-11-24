package ru.alarmtrade.specmp.feature.device.firmware.data.file

import java.io.File

internal actual fun readFileBytes(path: String): ByteArray = File(path).readBytes()

internal actual fun writeTempFile(name: String, bytes: ByteArray): String =
    File(System.getProperty("java.io.tmpdir"), name).apply { writeBytes(bytes) }.absolutePath
