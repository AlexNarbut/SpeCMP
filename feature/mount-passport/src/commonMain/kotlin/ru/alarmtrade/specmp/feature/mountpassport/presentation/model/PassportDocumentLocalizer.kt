package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import ru.alarmtrade.specmp.core.files.PdfFooter
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings

/** Тексты и форматы документа на языке приложения: документ уходит клиенту, язык — как в интерфейсе. */
internal interface PassportDocumentLocalizer {
    suspend fun strings(): PassportDocumentStrings

    suspend fun format(): PassportDocumentFormat

    suspend fun footer(number: String): PdfFooter

    /** `Паспорт_<№>_<автомобиль или ID блока>.pdf`. */
    suspend fun fileName(number: String, subject: String): String
}
