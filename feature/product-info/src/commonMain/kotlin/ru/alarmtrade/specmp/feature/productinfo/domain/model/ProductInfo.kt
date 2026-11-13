package ru.alarmtrade.specmp.feature.productinfo.domain.model

import kotlinx.datetime.LocalDate

/** Товар по ID устройства или цифрам штрих-кода; [kit] — комплект, в котором он продавался (может не быть). */
data class ProductInfo(val product: ProductUnit, val kit: ProductKit?)

data class ProductUnit(
    val id: Long?,
    val name: String,
    val serial: String?,
    val factoryDate: LocalDate?,
    val firmwares: List<ProductFirmware>,
)

/** [module] — подпись модуля как её присылает сервер («Основной», «CAN»). */
data class ProductFirmware(val module: String, val version: String?, val date: LocalDate?)

data class ProductKit(
    val name: String?,
    val serial: String?,
    val packageDate: LocalDate?,
    val components: List<ProductUnit>,
)
