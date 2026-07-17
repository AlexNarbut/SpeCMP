package ru.alarmtrade.specmp.feature.supportchat.domain.picker

/**
 * Платформенная абстракция сохранения вложений чата в пользовательские «Загрузки».
 *
 * Реализация живёт в платформенном слое `composeApp` (Android MediaStore, Desktop ~/Downloads,
 * iOS Documents). Файл сохраняется в подпапку `Pandora Spec` директории загрузок.
 */
interface SupportChatFileSaver {

    /**
     * Сохраняет [bytes] под именем [fileName] в `Загрузки/Pandora Spec`.
     * Возвращает путь сохранённого файла или null при ошибке.
     */
    suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String?
}
