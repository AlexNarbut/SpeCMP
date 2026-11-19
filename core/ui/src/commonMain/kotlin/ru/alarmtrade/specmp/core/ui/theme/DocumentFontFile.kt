package ru.alarmtrade.specmp.core.ui.theme

import ru.alarmtrade.specmp.core.ui.resources.Res

class DocumentFontFile(val family: String, val weight: Int, val bytes: ByteArray)

/** Шрифт приложения файлами — для вшивания в документы (PDF); в Compose — только через `CmpTheme.typography`. */
suspend fun readDocumentFontFiles(): List<DocumentFontFile> = DOCUMENT_FONTS.map { (weight, path) ->
    DocumentFontFile(DOCUMENT_FONT_FAMILY, weight, Res.readBytes(path))
}

const val DOCUMENT_FONT_FAMILY = "IBM Plex Sans"

private val DOCUMENT_FONTS = listOf(
    400 to "font/ibm_plex_sans_regular.ttf",
    500 to "font/ibm_plex_sans_medium.ttf",
    700 to "font/ibm_plex_sans_bold.ttf",
)
