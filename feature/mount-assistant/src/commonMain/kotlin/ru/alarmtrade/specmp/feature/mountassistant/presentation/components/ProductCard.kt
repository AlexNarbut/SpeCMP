package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_action_title_product_info
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_btn_details
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_btn_other_number
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_label_components
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_label_factory_date
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_label_id
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_label_kit
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_label_serial
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_msg_not_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_status_not_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_title_not_found
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProduct
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProductKit
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.icon

internal fun productDetailsTag(entryId: Long): String = "mountassistant_product_details_$entryId"

/**
 * Товар подключённого блока: кратко и «Подробнее» на полный экран. Не найден — нейтрально (блок мог быть продан не
 * через систему), обрыв сети — тоже без красного: это не поломка блока.
 */
@Composable
internal fun ProductResultCard(
    entryId: Long,
    content: ResultContent,
    onOpen: (query: String?) -> Unit,
    onRetry: () -> Unit,
) {
    CardShell(modifier = Modifier.testTag(resultTag(entryId))) {
        val failure = (content as? ResultContent.Error)?.failure
        val network = (failure as? ResultFailure.Common)?.error is ApiError
        CardHeader(
            icon = AssistantAction.PRODUCT_INFO.icon,
            title = stringResource(Strings.string.mountassistant_action_title_product_info),
            error = failure != null && !network,
        ) {
            if (content is ResultContent.ProductNotFound) {
                AssistantPill(text = stringResource(Strings.string.mountassistant_product_status_not_found))
            }
        }
        when (content) {
            ResultContent.Loading -> SkeletonRows(rows = 4)

            is ResultContent.Error -> ResultError(content.failure, onRetry)

            is ResultContent.ProductData -> ProductBody(entryId, content.product, onOpen)

            is ResultContent.ProductNotFound -> NotFoundBody(content.id) { onOpen(null) }

            is ResultContent.FirmwareData,
            is ResultContent.StateData,
            is ResultContent.AutostartData,
            is ResultContent.SettingsData,
            -> Unit
        }
    }
}

@Composable
private fun ProductBody(entryId: Long, product: AssistantProduct, onOpen: (String?) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpText(text = product.name, style = CmpTextStyle.Title)
        CmpColumn {
            val rows = productRows(product)
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) CmpDivider()
                ValueRow(label, value)
            }
        }
        product.kit?.let { KitRow(it) }
        CmpButton(
            onClick = { onOpen(product.id?.toString()) },
            modifier = Modifier.heightIn(min = 48.dp).testTag(productDetailsTag(entryId)),
        ) {
            CmpIcon(icon = CmpIcons.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            CmpText(
                text = stringResource(Strings.string.mountassistant_product_btn_details),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun productRows(product: AssistantProduct): List<Pair<String, String>> = listOfNotNull(
    product.id?.let { stringResource(Strings.string.mountassistant_product_label_id) to it.toString() },
    product.serial?.let { stringResource(Strings.string.mountassistant_product_label_serial) to it },
    product.factoryDate?.let { stringResource(Strings.string.mountassistant_product_label_factory_date) to it.text() },
)

@Composable
private fun ValueRow(label: String, value: String) {
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = label,
            modifier = Modifier.padding(end = 12.dp),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.BodyLarge,
        )
        CmpText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)) {
                    append(value)
                }
            },
            style = CmpTextStyle.BodyLarge,
        )
    }
}

@Composable
private fun KitRow(kit: AssistantProductKit) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        CmpIcon(icon = CmpIcons.Inventory2, contentDescription = null, tint = colors.primary)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(
                text = stringResource(Strings.string.mountassistant_product_label_kit),
                color = colors.onSurfaceVariant,
                style = CmpTextStyle.Body,
            )
            kit.name?.let { CmpText(text = it, fontWeight = FontWeight.Medium, style = CmpTextStyle.BodyLarge) }
            AssistantPill(
                text = pluralStringResource(
                    Strings.plurals.mountassistant_product_label_components,
                    kit.components,
                    kit.components,
                ),
                tone = PillTone.INFO,
            )
        }
    }
}

@Composable
private fun NotFoundBody(id: Long, onOtherNumber: () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(
            text = stringResource(Strings.string.mountassistant_product_title_not_found, id.toString()),
            fontWeight = FontWeight.Medium,
            style = CmpTextStyle.BodyLarge,
        )
        CmpText(
            text = stringResource(Strings.string.mountassistant_product_msg_not_found),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.BodyLarge,
        )
        CmpOutlinedButton(onClick = onOtherNumber, modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp)) {
            CmpIcon(icon = CmpIcons.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            CmpText(
                text = stringResource(Strings.string.mountassistant_product_btn_other_number),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private const val DAY_MONTH_DIGITS = 2

private fun LocalDate.text(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
