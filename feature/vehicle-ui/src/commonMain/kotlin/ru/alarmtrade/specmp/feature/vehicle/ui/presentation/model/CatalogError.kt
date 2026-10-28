package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.appError

/** Ошибка загрузки уровня каталога (марки, модели, комплектации, карточка, поиск по CAN). */
internal sealed interface CatalogError {
    data class Common(val error: AppError) : CatalogError

    /** Сервер не отдал данные. */
    data object LoadFailed : CatalogError

    companion object {
        fun from(error: Throwable): CatalogError = error.appError?.let(::Common) ?: LoadFailed
    }
}
