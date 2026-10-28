package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_error
import ru.alarmtrade.specmp.core.ui.error.messageText

/** Ошибка загрузки уровня каталога (марки, модели, комплектации, карточка, поиск по CAN). */
internal sealed interface CatalogError {
    data class Common(val error: AppError) : CatalogError

    /** Сервер не отдал данные. */
    data object LoadFailed : CatalogError

    companion object {
        fun from(error: Throwable): CatalogError = error.appError?.let(::Common) ?: LoadFailed
    }
}

/** Текст ошибки для снекбара (вне composition). */
internal suspend fun CatalogError.messageText(): String = when (this) {
    is CatalogError.Common -> error.messageText()
    CatalogError.LoadFailed -> getString(Strings.string.vehicle_catalog_title_error)
}
