package ru.alarmtrade.specmp.feature.device.firmware.data.file

/** Содержимое локального файла (блокирующее чтение — вызывать на IO). Размер ограничен пикером (16 МБ). */
internal expect fun readFileBytes(path: String): ByteArray

/** Пишет [bytes] во временный файл приложения с именем [name] (перезаписывая) и возвращает путь; вызывать на IO. */
internal expect fun writeTempFile(name: String, bytes: ByteArray): String
