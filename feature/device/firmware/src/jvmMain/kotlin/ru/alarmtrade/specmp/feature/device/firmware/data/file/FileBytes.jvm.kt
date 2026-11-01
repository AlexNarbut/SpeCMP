package ru.alarmtrade.specmp.feature.device.firmware.data.file

import java.io.File

internal actual fun readFileBytes(path: String): ByteArray = File(path).readBytes()
