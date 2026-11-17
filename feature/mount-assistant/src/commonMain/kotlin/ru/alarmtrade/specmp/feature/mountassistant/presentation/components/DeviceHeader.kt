package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_block_msg_factory_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_a11y_collapse
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_a11y_expand
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_btn_can_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_ble
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_can_code
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_can_factory
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_offline
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_serial
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_unknown_model
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_header_value_usb
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts

internal const val HEADER_TAG = "mountassistant_header"

/** [wide] — в колонке пульта на Expanded: без фона и свёртки. [onToggle] `null` — не сворачивается. */
@Composable
internal fun DeviceHeader(
    facts: DeviceFacts,
    collapsed: Boolean,
    wide: Boolean,
    onToggle: (() -> Unit)?,
    onCanUpdate: () -> Unit,
) {
    if (collapsed && onToggle != null) {
        CollapsedHeader(facts, onToggle)
        return
    }
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = Modifier.fillMaxWidth().testTag(HEADER_TAG)
            .then(if (wide) Modifier else Modifier.background(colors.surfaceContainerLow))
            .padding(if (wide) 0.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpRow(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpText(text = facts.title, modifier = Modifier.weight(1f), style = CmpTextStyle.Headline1)
            if (onToggle != null) {
                CmpIconButton(
                    onClick = onToggle,
                    icon = CmpIcons.KeyboardArrowUp,
                    contentDescription = stringResource(Strings.string.mountassistant_header_a11y_collapse),
                )
            }
        }
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistantPill(text = transportText(facts.transport), icon = transportIcon(facts.transport))
            AssistantPill(
                text = stringResource(Strings.string.mountassistant_header_value_firmware, facts.firmwareVersion),
                icon = CmpIcons.Memory,
            )
            CanPill(facts)
            if (!facts.online) {
                AssistantPill(
                    text = stringResource(Strings.string.mountassistant_header_value_offline),
                    icon = CmpIcons.CloudOff,
                )
            }
        }
        if (facts.factoryCan) FactoryCanBanner(onCanUpdate)
    }
    if (!wide) CmpDivider()
}

@Composable
private fun CanPill(facts: DeviceFacts) {
    val can = facts.can
    when {
        !facts.inDatabase -> AssistantPill(
            text = stringResource(Strings.string.mountassistant_header_value_unknown_model),
            tone = PillTone.WARN,
            icon = CmpIcons.HelpOutline,
        )

        can == null -> Unit

        can.factory -> AssistantPill(
            text = stringResource(Strings.string.mountassistant_header_value_can_factory),
            tone = PillTone.WARN,
            icon = CmpIcons.Warning,
        )

        else -> AssistantPill(
            text = if (can.code != null && can.version != null) {
                stringResource(Strings.string.mountassistant_header_value_can_code, can.version, can.code)
            } else {
                stringResource(Strings.string.mountassistant_header_value_can, can.version ?: can.code.orEmpty())
            },
            tone = PillTone.OK,
            icon = CmpIcons.CheckCircle,
        )
    }
}

@Composable
private fun FactoryCanBanner(onCanUpdate: () -> Unit) {
    val colors = cmpThemeColors()
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth()
            .background(colors.tertiaryContainer, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(icon = CmpIcons.Lock, contentDescription = null, tint = colors.onTertiaryContainer)
        CmpText(
            text = stringResource(Strings.string.mountassistant_block_msg_factory_can),
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.Paragraph1,
            color = colors.onTertiaryContainer,
        )
        CmpButton(text = stringResource(Strings.string.mountassistant_header_btn_can_update), onClick = onCanUpdate)
    }
}

@Composable
private fun CollapsedHeader(facts: DeviceFacts, onToggle: () -> Unit) {
    val colors = cmpThemeColors()
    val description = stringResource(Strings.string.mountassistant_header_a11y_expand)
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(HEADER_TAG)
            .background(colors.surfaceContainerLow)
            .clickable(onClickLabel = description, onClick = onToggle)
            .padding(start = 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(icon = transportIcon(facts.transport), contentDescription = null, tint = colors.primary)
        CmpRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpText(
                text = facts.title,
                modifier = Modifier.weight(1f, fill = false),
                style = CmpTextStyle.Paragraph1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CmpText(
                text = facts.firmwareVersion + facts.can?.version?.let { " · CAN $it" }.orEmpty(),
                style = CmpTextStyle.Paragraph2,
                color = colors.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (facts.factoryCan) CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.tertiary)
        if (!facts.online) CmpIcon(icon = CmpIcons.CloudOff, contentDescription = null, tint = colors.onSurfaceVariant)
        CmpIcon(
            icon = CmpIcons.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = colors.onSurfaceVariant,
        )
    }
    CmpDivider()
}

@Composable
private fun transportText(transport: AssistantTransport): String = stringResource(
    when (transport) {
        AssistantTransport.BLE -> Strings.string.mountassistant_header_value_ble
        AssistantTransport.USB -> Strings.string.mountassistant_header_value_usb
        AssistantTransport.SERIAL -> Strings.string.mountassistant_header_value_serial
    },
)

private fun transportIcon(transport: AssistantTransport): CmpIcons = when (transport) {
    AssistantTransport.BLE -> CmpIcons.Bluetooth
    AssistantTransport.USB, AssistantTransport.SERIAL -> CmpIcons.Usb
}
