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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpInfoRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
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
                CmpTag(text = stringResource(Strings.string.mountassistant_product_status_not_found))
            }
        }
        when (content) {
            ResultContent.Loading -> SkeletonRows(rows = 4)

            is ResultContent.Error ->
                ResultError(content.failure, onRetry, tone = if (network) CmpBannerTone.Info else CmpBannerTone.Error)

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
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpText(text = product.name, style = CmpTextStyle.Headline1)
        CmpColumn {
            val rows = productRows(product)
            rows.forEachIndexed { index, (label, value) ->
                CmpInfoRow(label = label, value = value, mono = true, compact = true, last = index == rows.lastIndex)
            }
        }
        product.kit?.let { KitRow(it) }
        CmpButton(
            text = stringResource(Strings.string.mountassistant_product_btn_details),
            onClick = { onOpen(product.id?.toString()) },
            modifier = Modifier.testTag(productDetailsTag(entryId)),
            size = CmpButtonSize.S,
            icon = CmpIcons.OpenInNew,
        )
    }
}

@Composable
private fun productRows(product: AssistantProduct): List<Pair<String, String>> = listOfNotNull(
    product.id?.let { stringResource(Strings.string.mountassistant_product_label_id) to it.toString() },
    product.serial?.let { stringResource(Strings.string.mountassistant_product_label_serial) to it },
    product.factoryDate?.let { stringResource(Strings.string.mountassistant_product_label_factory_date) to it.text() },
)

@Composable
private fun KitRow(kit: AssistantProductKit) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.touchMin + CmpSpacing.space8)
            .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall))
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.Top,
    ) {
        CmpIcon(
            icon = CmpIcons.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(CmpSize.iconM),
            tint = colors.brandPrimaryDefault,
        )
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(
                text = stringResource(Strings.string.mountassistant_product_label_kit),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Additional0,
            )
            kit.name?.let { CmpText(text = it, style = CmpTextStyle.Headline3) }
            CmpTag(
                text = pluralStringResource(
                    Strings.plurals.mountassistant_product_label_components,
                    kit.components,
                    kit.components,
                ),
                tone = CmpTagTone.Info,
            )
        }
    }
}

@Composable
private fun NotFoundBody(id: Long, onOtherNumber: () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(Strings.string.mountassistant_product_title_not_found, id.toString()),
            style = CmpTextStyle.Headline2,
        )
        CmpText(
            text = stringResource(Strings.string.mountassistant_product_msg_not_found),
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Paragraph1,
        )
        CmpButton(
            text = stringResource(Strings.string.mountassistant_product_btn_other_number),
            onClick = onOtherNumber,
            modifier = Modifier.padding(top = CmpSpacing.space4),
            type = CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            icon = CmpIcons.Search,
            maxLines = 2,
        )
    }
}

private const val DAY_MONTH_DIGITS = 2

private fun LocalDate.text(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
