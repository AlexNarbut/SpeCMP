package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_btn_scan
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_hint_barcode
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_hint_device_id
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_label_barcode
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_label_device_id
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_msg
import ru.alarmtrade.specmp.core.localization.resources.productinfo_empty_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

private val HeroSize = 112.dp
private val HintMaxWidth = 440.dp
private val TipsMaxWidth = 640.dp

/** Начальное состояние: где взять номер. */
@Composable
internal fun EmptyHint(wide: Boolean, onScan: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (wide) CmpSpacing.space32 else CmpSpacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        Box(
            modifier = Modifier.size(HeroSize).background(colors.brandPrimaryHighlight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.ManageSearch,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconXxl),
                tint = colors.brandPrimaryDefault,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = HintMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.productinfo_empty_title),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.productinfo_empty_msg),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
                textAlign = TextAlign.Center,
            )
        }
        val tips: @Composable (Modifier) -> Unit = { itemModifier ->
            Tip(
                CmpIcons.Memory,
                Strings.string.productinfo_empty_label_device_id,
                Strings.string.productinfo_empty_hint_device_id,
                itemModifier,
            )
            Tip(
                CmpIcons.Inventory2,
                Strings.string.productinfo_empty_label_barcode,
                Strings.string.productinfo_empty_hint_barcode,
                itemModifier,
            )
        }
        if (wide) {
            Row(
                modifier = Modifier.widthIn(max = TipsMaxWidth).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                tips(Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                tips(Modifier.fillMaxWidth())
            }
        }
        if (onScan != null) {
            ScanButton(onScan, Modifier.padding(top = CmpSpacing.space4).testTag(ProductInfoTags.SCAN_EMPTY))
        }
    }
}

@Composable
internal fun ScanButton(onScan: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(
        text = stringResource(Strings.string.productinfo_empty_btn_scan),
        onClick = onScan,
        modifier = modifier,
        type = CmpButtonType.Secondary,
        icon = CmpIcons.QrCodeScanner,
    )
}

@Composable
private fun Tip(icon: CmpIcons, title: StringResource, hint: StringResource, modifier: Modifier = Modifier) {
    CmpListItem(
        title = stringResource(title),
        subtitle = stringResource(hint),
        icon = icon,
        modifier = modifier,
    )
}

@Composable
internal fun SkeletonCard(wide: Boolean, lines: Int, modifier: Modifier = Modifier) {
    InfoCard(
        modifier = modifier,
        padding = if (wide) CmpSpacing.space24 else CmpSpacing.space16,
        spacing = CmpSpacing.space16,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpSkeleton(modifier = Modifier.size(IconTileSize), shape = RoundedCornerShape(CmpRadius.controls))
            CmpSkeleton(modifier = Modifier.size(width = 96.dp, height = 20.dp))
        }
        CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(28.dp))
        repeat(lines) { index ->
            Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpSkeleton(modifier = Modifier.size(width = 110.dp, height = 14.dp))
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_START - index * SKELETON_STEP).height(20.dp))
            }
        }
    }
}

private const val SKELETON_TITLE = 0.72f
private const val SKELETON_START = 0.6f
private const val SKELETON_STEP = 0.08f
