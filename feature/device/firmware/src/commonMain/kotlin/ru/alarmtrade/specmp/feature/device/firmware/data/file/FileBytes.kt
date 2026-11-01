package ru.alarmtrade.specmp.feature.device.firmware.data.file

/** Содержимое локального файла (блокирующее чтение — вызывать на IO). Размер ограничен пикером (16 МБ). */
internal expect fun readFileBytes(path: String): ByteArray
