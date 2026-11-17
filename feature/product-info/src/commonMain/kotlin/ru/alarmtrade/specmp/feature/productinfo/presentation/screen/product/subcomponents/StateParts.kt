package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags

/** Начальное состояние: где взять номер. */
@Composable
internal fun EmptyHint(wide: Boolean, onScan: (() -> Unit)?, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = if (wide) 40.dp else 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(112.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.ManageSearch,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = colors.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CmpText(
                text = stringResource(Strings.string.productinfo_empty_title),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Strings.string.productinfo_empty_msg),
                color = colors.onSurfaceVariant,
                style = CmpTextStyle.Paragraph1,
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
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                tips(Modifier.weight(1f))
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tips(Modifier.fillMaxWidth())
            }
        }
        if (onScan != null) ScanButton(onScan, Modifier.padding(top = 4.dp).testTag(ProductInfoTags.SCAN_EMPTY))
    }
}

@Composable
internal fun ScanButton(onScan: () -> Unit, modifier: Modifier = Modifier) {
    val primary = cmpThemeColors().primary
    CmpOutlinedButton(onClick = onScan, modifier = modifier.heightIn(min = 56.dp)) {
        CmpIcon(
            icon = CmpIcons.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = primary,
        )
        CmpText(
            text = stringResource(Strings.string.productinfo_empty_btn_scan),
            modifier = Modifier.padding(start = 8.dp),
            color = primary,
            style = CmpTextStyle.Paragraph1,
        )
    }
}

@Composable
private fun Tip(icon: CmpIcons, title: StringResource, hint: StringResource, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    InfoCard(modifier = modifier, spacing = 0.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(colors.secondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(icon = icon, contentDescription = null, tint = colors.onSecondaryContainer)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(text = stringResource(title), style = CmpTextStyle.Paragraph1)
                CmpText(text = stringResource(hint), color = colors.onSurfaceVariant, style = CmpTextStyle.Paragraph2)
            }
        }
    }
}

@Composable
internal fun SkeletonCard(wide: Boolean, lines: Int, modifier: Modifier = Modifier) {
    InfoCard(modifier = modifier, padding = if (wide) 24.dp else 16.dp, spacing = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpSkeleton(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp))
            CmpSkeleton(modifier = Modifier.size(width = 96.dp, height = 20.dp))
        }
        CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(28.dp))
        repeat(lines) { index ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpSkeleton(modifier = Modifier.size(width = 110.dp, height = 14.dp))
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_START - index * SKELETON_STEP).height(20.dp))
            }
        }
    }
}

private const val SKELETON_TITLE = 0.72f
private const val SKELETON_START = 0.6f
private const val SKELETON_STEP = 0.08f
