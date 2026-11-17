package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductKit
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

private val WideCardPadding = 24.dp

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
    InfoCard(modifier = modifier.testTag(ProductInfoTags.PRODUCT), padding = if (wide) WideCardPadding else 16.dp) {
        Kicker(icon = CmpIcons.Tag, text = stringResource(Strings.string.productinfo_product_title)) {
            if (connected) {
                Badge(
                    icon = CmpIcons.Link,
                    text = stringResource(Strings.string.productinfo_product_status_connected),
                    modifier = Modifier.testTag(ProductInfoTags.CONNECTED),
                )
            }
        }
        CmpText(text = product.name, style = CmpTextStyle.Headline1)
        if (solo && product.firmwares.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) { ProductRows(product, onCopy) }
                Column(modifier = Modifier.weight(1f)) { ProductFirmwares(product) }
            }
        } else {
            Column { ProductRows(product, onCopy) }
            ProductFirmwares(product)
        }
    }
}

@Composable
private fun ProductRows(product: ProductUnit, onCopy: (String) -> Unit) {
    var first = true
    product.id?.let { id ->
        InfoRow(stringResource(Strings.string.productinfo_common_label_id), id.toString(), true, !first, onCopy)
        first = false
    }
    product.serial?.let { serial ->
        InfoRow(stringResource(Strings.string.productinfo_common_label_serial), serial, true, !first, onCopy)
        first = false
    }
    product.factoryDate?.let { date ->
        InfoRow(
            stringResource(Strings.string.productinfo_product_label_factory_date),
            date.formatted(),
            false,
            !first,
            null,
        )
    }
}

@Composable
private fun ProductFirmwares(product: ProductUnit) {
    if (product.firmwares.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CmpText(
            text = stringResource(Strings.string.productinfo_common_label_firmwares),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.Paragraph2,
        )
        FirmwareRows(product.firmwares)
    }
}

@Composable
internal fun KitCard(kit: ProductKit, wide: Boolean, onCopy: (String) -> Unit, modifier: Modifier = Modifier) {
    InfoCard(modifier = modifier.testTag(ProductInfoTags.KIT), padding = if (wide) WideCardPadding else 16.dp) {
        Kicker(icon = CmpIcons.Inventory2, text = stringResource(Strings.string.productinfo_kit_title))
        kit.name?.let { CmpText(text = it, style = CmpTextStyle.Headline1) }
        Column {
            var first = true
            kit.serial?.let { serial ->
                InfoRow(stringResource(Strings.string.productinfo_common_label_serial), serial, true, false, onCopy)
                first = false
            }
            kit.packageDate?.let { date ->
                InfoRow(
                    stringResource(Strings.string.productinfo_kit_label_package_date),
                    date.formatted(),
                    false,
                    !first,
                    null,
                )
                first = false
            }
            InfoRow(
                label = stringResource(Strings.string.productinfo_kit_label_contents),
                value = pluralStringResource(
                    Strings.plurals.productinfo_kit_label_components,
                    kit.components.size,
                    kit.components.size,
                ),
                monospace = false,
                divider = !first,
                onCopy = null,
            )
        }
    }
}
