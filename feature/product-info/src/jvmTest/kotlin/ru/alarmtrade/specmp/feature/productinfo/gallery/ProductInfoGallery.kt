package ru.alarmtrade.specmp.feature.productinfo.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductFirmware
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductKit
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQueryError
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoError
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoActions
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoScreen
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoState

/** Все состояния экрана «Информация о товаре»; PNG — в `build/state-gallery/product-info`. */
class ProductInfoGallery {

    private val outDir = File("build/state-gallery/product-info")
    private val actions = ProductInfoActions({}, {}, {}, {}, {}, {}, {}, {})

    private val date = LocalDate(2024, 10, 14)
    private val product = ProductUnit(
        id = 107131,
        name = "Pandora DX-4GS",
        serial = "4G24 1003 5871",
        factoryDate = date,
        firmwares = listOf(
            ProductFirmware("Основной", "3.57", date),
            ProductFirmware("CAN", "12.05", date),
            ProductFirmware("Bluetooth", "1.14", LocalDate(2024, 9, 2)),
        ),
    )
    private val components = listOf(
        ProductUnit(
            3100217,
            "Брелок Pandora D-030",
            "0302 4410 0217",
            null,
            listOf(ProductFirmware("Основной", "2.11", date)),
        ),
        ProductUnit(
            7600451,
            "Метка Pandora BT-760",
            "0760 4409 0451",
            null,
            listOf(ProductFirmware("Основной", "1.08", date)),
        ),
        ProductUnit(
            1200917,
            "Модуль GPS/ГЛОНАСС Pandora NAV-12",
            "0012 4410 0917",
            null,
            listOf(ProductFirmware("Основной", "1.02", date), ProductFirmware("Навигация", "4.3", date)),
        ),
        ProductUnit(null, "Сирена Pandora PS-331", "0331 4407 1182", null, emptyList()),
        ProductUnit(null, "Датчик температуры двигателя", null, null, emptyList()),
    )
    private val kit =
        ProductKit("Pandora DX-4GS · комплект с брелоком D-030", "K-2410-118342", LocalDate(2024, 10, 21), components)

    private fun render(name: String, state: ProductInfoState) = StateGallery.render(outDir, name) {
        ProductInfoScreen(state = state, actions = actions, snackbar = CmpSnackbarHostState())
    }

    @Test
    fun empty() = render("01-empty", ProductInfoState())

    @Test
    fun inputError() =
        render("02-input-error", ProductInfoState(query = "12ab", queryError = ProductQueryError.NOT_DIGITS))

    @Test
    fun loading() = render("03-loading", ProductInfoState(query = "107131", result = LCEState.Loading))

    @Test
    fun contentWithKit() = render(
        "04-content-kit",
        ProductInfoState(
            query = "107131",
            result = LCEState.Content(ProductInfo(product, kit)),
            connectedId = 107131,
            expanded = persistentSetOf(2),
        ),
    )

    @Test
    fun contentWithoutKit() = render(
        "05-content-no-kit",
        ProductInfoState(query = "107131", result = LCEState.Content(ProductInfo(product, null))),
    )

    @Test
    fun notFound() = render(
        "06-not-found",
        ProductInfoState(query = "1234567", result = LCEState.Error(ProductInfoError.NotFound(1234567))),
    )

    @Test
    fun offline() = render(
        "07-offline",
        ProductInfoState(query = "107131", result = LCEState.Error(ProductInfoError.Common(ApiError.NoConnection))),
    )

    @Test
    fun serverError() = render(
        "08-server",
        ProductInfoState(query = "107131", result = LCEState.Error(ProductInfoError.Unknown)),
    )
}
