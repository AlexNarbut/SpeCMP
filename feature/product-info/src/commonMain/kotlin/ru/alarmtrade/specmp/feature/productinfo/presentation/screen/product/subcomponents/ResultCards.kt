package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_firmwares
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_id
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_label_serial
import ru.alarmtrade.specmp.core.localization.resources.productinfo_kit_label_components
import ru.alarmtrade.specmp.core.localization.resources.productinfo_kit_label_contents
import ru.alarmtrade.specmp.core.localization.resources.productinfo_kit_label_package_date
import ru.alarmtrade.specmp.core.localization.resources.productinfo_kit_title
import ru.alarmtrade.specmp.core.localization.resources.productinfo_product_label_factory_date
import ru.alarmtrade.specmp.core.localization.resources.productinfo_product_status_connected
import ru.alarmtrade.specmp.core.localization.resources.productinfo_product_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductKit
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

/** [solo] — широкое окно без комплекта: строки слева, прошивки справа. */
@Composable
internal fun ProductCard(
    product: ProductUnit,
    connected: Boolean,
    wide: Boolean,
    solo: Boolean,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(modifier = modifier.testTag(ProductInfoTags.PRODUCT), padding = cardPadding(wide)) {
        Kicker(icon = CmpIcons.Tag, text = stringResource(Strings.string.productinfo_product_title)) {
            if (connected) {
                CmpTag(
                    text = stringResource(Strings.string.productinfo_product_status_connected),
                    tone = CmpTagTone.Brand,
                    icon = CmpIcons.Link,
                    modifier = Modifier.testTag(ProductInfoTags.CONNECTED),
                )
            }
        }
        CmpText(text = product.name, style = CmpTextStyle.Headline1)
        val lines = productLines(product)
        if (solo && product.firmwares.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space32), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) { InfoLines(lines, onCopy) }
                Column(modifier = Modifier.weight(1f)) { ProductFirmwares(product) }
            }
        } else {
            InfoLines(lines, onCopy)
            ProductFirmwares(product)
        }
    }
}

@Composable
private fun productLines(product: ProductUnit): List<InfoLine> = listOfNotNull(
    product.id?.let { InfoLine(stringResource(Strings.string.productinfo_common_label_id), it.toString(), true, true) },
    product.serial?.let { InfoLine(stringResource(Strings.string.productinfo_common_label_serial), it, true, true) },
    product.factoryDate?.let {
        InfoLine(stringResource(Strings.string.productinfo_product_label_factory_date), it.formatted(), false, false)
    },
)

@Composable
private fun ProductFirmwares(product: ProductUnit) {
    if (product.firmwares.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(Strings.string.productinfo_common_label_firmwares),
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Additional0,
        )
        FirmwareRows(product.firmwares)
    }
}

@Composable
internal fun KitCard(kit: ProductKit, wide: Boolean, onCopy: (String) -> Unit, modifier: Modifier = Modifier) {
    InfoCard(modifier = modifier.testTag(ProductInfoTags.KIT), padding = cardPadding(wide)) {
        Kicker(icon = CmpIcons.Inventory2, text = stringResource(Strings.string.productinfo_kit_title))
        kit.name?.let { CmpText(text = it, style = CmpTextStyle.Headline1) }
        val lines = listOfNotNull(
            kit.serial?.let {
                InfoLine(stringResource(Strings.string.productinfo_common_label_serial), it, true, true)
            },
            kit.packageDate?.let {
                InfoLine(
                    stringResource(Strings.string.productinfo_kit_label_package_date),
                    it.formatted(),
                    false,
                    false,
                )
            },
            InfoLine(
                label = stringResource(Strings.string.productinfo_kit_label_contents),
                value = pluralStringResource(
                    Strings.plurals.productinfo_kit_label_components,
                    kit.components.size,
                    kit.components.size,
                ),
                monospace = false,
                copyable = false,
            ),
        )
        InfoLines(lines, onCopy)
    }
}

private fun cardPadding(wide: Boolean) = if (wide) CmpSpacing.space24 else CmpSpacing.space16
