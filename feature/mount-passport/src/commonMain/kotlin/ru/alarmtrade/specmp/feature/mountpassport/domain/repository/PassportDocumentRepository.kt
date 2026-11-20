package ru.alarmtrade.specmp.feature.mountpassport.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentFormat
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportDocumentStrings

/** HTML документа собирает SDK (фото — внутри документа); PDF из него делает приложение. */
interface PassportDocumentRepository {
    suspend fun buildHtml(
        id: String,
        strings: PassportDocumentStrings,
        format: PassportDocumentFormat,
    ): Response<String>

    /** Документ сформирован — статус `ISSUED`. */
    suspend fun markIssued(id: String): Response<MountPassport>
}
