package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_a11y_refresh
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_refresh
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_reinstall
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_update
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_error_server
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_actual
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_dialog_full_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_unsupported
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_update_available
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_loading_search
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_loading_version
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_dialog_no_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_unsupported_hint
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_label_version
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.titleRes
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CardStatusUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CurrentVersionUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.ModuleCardUi

private val HighlightBorder = 2.dp
private val SpinnerStroke = 2.5.dp

// Уже этой ширины «Описание» и «Обновить» в одну строку не помещаются (телефон 412 и уже) — кнопки стопкой.
private val StackedButtonsBelow = 320.dp

private const val UPDATE_BUTTON_WEIGHT = 1.4f
private const val WIDE_UPDATE_WEIGHT = 1.25f
private const val DESCRIPTION_PREVIEW_LINES = 3
private const val TONE_BORDER_ALPHA = 0.3f

internal fun moduleUpdateTag(moduleId: Long): String = "firmware_module_update_$moduleId"

internal fun moduleRefreshTag(moduleId: Long): String = "firmware_module_refresh_$moduleId"

@Composable
internal fun ModuleCard(
    module: ModuleCardUi,
    onRefresh: () -> Unit,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    if (wide) {
        ModuleCardWide(module, onRefresh, onDescription, onUpdate, modifier)
        return
    }
    CmpCard(
        modifier = modifier.fillMaxWidth(),
        border = cardBorder(highlighted = module.status is CardStatusUi.UpdateAvailable),
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CardHeader(module, onRefresh)
            CurrentVersionRow(module.currentVersion)
            StatusBlock(module, onDescription, onUpdate, onRefresh)
        }
    }
}

@Composable
private fun cardBorder(highlighted: Boolean): BorderStroke = if (highlighted) {
    BorderStroke(HighlightBorder, CmpTheme.colors.brandPrimaryDefault)
} else {
    BorderStroke(CmpBorderWidth.listItem, CmpTheme.colors.borderHighlight)
}

@Composable
private fun ModuleIcon(size: Dp, radius: Dp, iconSize: Dp) {
    val colors = CmpTheme.colors
    CmpBox(
        modifier = Modifier.size(size).background(colors.brandPrimaryHighlight, RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            icon = CmpIcons.Memory,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = colors.brandPrimaryDefault,
        )
    }
}

@Composable
private fun CardHeader(module: ModuleCardUi, onRefresh: () -> Unit) {
    val canRefresh = module.status !is CardStatusUi.NotSupported &&
        module.status !is CardStatusUi.Searching &&
        module.currentVersion !is CurrentVersionUi.Reading
    CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12), verticalAlignment = Alignment.Top) {
        ModuleIcon(size = CmpSize.touchMin, radius = CmpRadius.cardSmall, iconSize = CmpSize.iconL)
        ModuleTitle(module, titleStyle = CmpTextStyle.Headline2, modifier = Modifier.weight(1f))
        if (module.status !is CardStatusUi.NotSupported) {
            CmpIconButton(
                onClick = onRefresh,
                icon = CmpIcons.Refresh,
                contentDescription = stringResource(Strings.string.firmware_check_a11y_refresh),
                modifier = Modifier.testTag(moduleRefreshTag(module.id)),
                enabled = canRefresh,
            )
        }
    }
}

@Composable
private fun ModuleTitle(module: ModuleCardUi, titleStyle: CmpTextStyle, modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(module.kind.titleRes()),
            style = CmpTextStyle.Additional1,
            color = CmpTheme.colors.brandPrimaryDefault,
            uppercase = true,
        )
        CmpText(text = module.title, style = titleStyle)
    }
}

@Composable
private fun CurrentVersionRow(currentVersion: CurrentVersionUi, wide: Boolean = false) {
    val colors = CmpTheme.colors
    if (currentVersion is CurrentVersionUi.Absent) return
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.firmware_common_label_version),
            color = colors.foregroundOnDarkSecondaryDefault,
            style = if (wide) CmpTextStyle.Paragraph1 else CmpTextStyle.Paragraph2,
            modifier = Modifier.weight(1f),
        )
        when (currentVersion) {
            CurrentVersionUi.Reading -> CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.firmware_check_loading_version),
                    color = colors.foregroundOnDarkSecondaryDefault,
                    style = CmpTextStyle.Paragraph2,
                )
                Spinner()
            }

            is CurrentVersionUi.Known -> VersionText(
                currentVersion.version,
                style = if (wide) CmpTextStyle.Headline2 else CmpTextStyle.Headline3,
            )

            CurrentVersionUi.Absent -> Unit
        }
    }
}

@Composable
private fun Spinner() {
    CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), strokeWidth = SpinnerStroke)
}

@Composable
private fun StatusBlock(module: ModuleCardUi, onDescription: () -> Unit, onUpdate: () -> Unit, onRefresh: () -> Unit) {
    when (val status = module.status) {
        CardStatusUi.NotSupported -> NotSupportedBlock()
        CardStatusUi.Searching -> SearchingBlock()
        is CardStatusUi.UpdateAvailable -> UpdateAvailableBlock(module.id, status, onDescription, onUpdate)
        is CardStatusUi.Actual -> ActualBlock(module.id, status, onUpdate)
        is CardStatusUi.Failed -> FailedBlock(status.error, onRefresh)
    }
}

@Composable
private fun NotSupportedBlock() {
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconM),
                tint = colors.errorDefault,
            )
            CmpText(
                text = stringResource(Strings.string.firmware_check_label_unsupported),
                style = CmpTextStyle.Headline3,
                color = colors.errorDefault,
            )
        }
        CmpText(
            text = stringResource(Strings.string.firmware_check_msg_unsupported_hint),
            color = colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Paragraph2,
        )
    }
}

@Composable
private fun SearchingBlock() {
    CmpRow(
        modifier = Modifier.heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spinner()
        CmpText(
            text = stringResource(Strings.string.firmware_check_loading_search),
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Paragraph2,
        )
    }
}

@Composable
private fun UpdateTag() {
    CmpTag(
        text = stringResource(Strings.string.firmware_check_label_update_available),
        tone = CmpTagTone.Brand,
        icon = CmpIcons.ArrowCircleUp,
        maxLines = 2,
    )
}

@Composable
private fun TonalBlock(
    container: Color,
    border: Color,
    radius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(container, shape)
            .border(CmpBorderWidth.listItem, border.copy(alpha = TONE_BORDER_ALPHA), shape)
            .padding(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        content()
    }
}

@Composable
private fun UpdateAvailableBlock(
    moduleId: Long,
    status: CardStatusUi.UpdateAvailable,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
) {
    val colors = CmpTheme.colors
    TonalBlock(
        container = colors.brandPrimaryHighlight,
        border = colors.brandPrimaryDefault,
        radius = CmpRadius.cardSmall,
    ) {
        CmpFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateTag()
            VersionText(status.version, style = CmpTextStyle.Headline2)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val description: @Composable (Modifier) -> Unit = { mod ->
                CmpButton(
                    text = stringResource(Strings.string.firmware_check_btn_description),
                    onClick = onDescription,
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    modifier = mod,
                )
            }
            val update: @Composable (Modifier) -> Unit = { mod ->
                CmpButton(
                    text = stringResource(Strings.string.firmware_check_btn_update),
                    onClick = onUpdate,
                    size = CmpButtonSize.S,
                    modifier = mod.testTag(moduleUpdateTag(moduleId)),
                )
            }
            if (maxWidth < StackedButtonsBelow) {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    update(Modifier.fillMaxWidth())
                    description(Modifier.fillMaxWidth())
                }
            } else {
                CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    description(Modifier.weight(1f))
                    update(Modifier.weight(UPDATE_BUTTON_WEIGHT))
                }
            }
        }
    }
}

@Composable
private fun ActualStatus(version: String, wide: Boolean) {
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon = CmpIcons.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(CmpSize.iconM),
            tint = CmpTheme.colors.brandPrimaryDefault,
        )
        CmpText(
            text = stringResource(Strings.string.firmware_check_label_actual),
            style = if (wide) CmpTextStyle.Headline3 else CmpTextStyle.Paragraph2,
        )
        VersionText(version, style = CmpTextStyle.Headline3)
    }
}

@Composable
private fun ReinstallButton(moduleId: Long, onUpdate: () -> Unit) {
    CmpButton(
        text = stringResource(Strings.string.firmware_check_btn_reinstall),
        onClick = onUpdate,
        type = CmpButtonType.Tertiary,
        size = CmpButtonSize.S,
        modifier = Modifier.testTag(moduleUpdateTag(moduleId)),
    )
}

@Composable
private fun ActualBlock(moduleId: Long, status: CardStatusUi.Actual, onUpdate: () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        ActualStatus(status.version, wide = false)
        ReinstallButton(moduleId, onUpdate)
    }
}

@Composable
private fun FailedBlock(error: FirmwareError, onRetry: () -> Unit) {
    val colors = CmpTheme.colors
    TonalBlock(container = colors.errorHighlight, border = colors.errorDefault, radius = CmpRadius.cardSmall) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpIcon(
                icon = CmpIcons.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconM),
                tint = colors.errorDefault,
            )
            CmpText(text = error.text(Strings.string.firmware_check_error_server), style = CmpTextStyle.Paragraph2)
        }
        CmpButton(
            text = stringResource(Strings.string.firmware_common_btn_retry),
            onClick = onRetry,
            type = CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            icon = CmpIcons.Refresh,
        )
    }
}

@Composable
private fun ModuleCardWide(
    module: ModuleCardUi,
    onRefresh: () -> Unit,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = module.status
    val update = status is CardStatusUi.UpdateAvailable
    CmpCard(modifier = modifier.fillMaxWidth(), border = cardBorder(highlighted = update)) {
        CmpRow(
            modifier = Modifier.padding(CmpSpacing.space16),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
        ) {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                    verticalAlignment = Alignment.Top,
                ) {
                    ModuleIcon(size = CmpSize.iconXxl, radius = CmpRadius.cardMedium, iconSize = CmpSize.iconL)
                    ModuleTitle(module, titleStyle = CmpTextStyle.Headline1, modifier = Modifier.weight(1f))
                }
                if (module.currentVersion !is CurrentVersionUi.Absent) {
                    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                        CmpDivider()
                        CurrentVersionRow(module.currentVersion, wide = true)
                    }
                }
                if (status !is CardStatusUi.NotSupported) {
                    CmpButton(
                        text = stringResource(Strings.string.firmware_check_btn_refresh),
                        onClick = onRefresh,
                        type = CmpButtonType.Tertiary,
                        size = CmpButtonSize.S,
                        icon = CmpIcons.Refresh,
                        enabled = status !is CardStatusUi.Searching &&
                            module.currentVersion !is CurrentVersionUi.Reading,
                        modifier = Modifier.testTag(moduleRefreshTag(module.id)),
                    )
                }
            }
            CmpColumn(
                modifier = Modifier
                    .weight(if (update) WIDE_UPDATE_WEIGHT else 1f)
                    .align(if (update) Alignment.Top else Alignment.CenterVertically),
            ) {
                when (status) {
                    is CardStatusUi.UpdateAvailable -> WideUpdateBlock(module, status, onDescription, onUpdate)

                    is CardStatusUi.Actual -> CmpFlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActualStatus(status.version, wide = true)
                        ReinstallButton(module.id, onUpdate)
                    }

                    else -> StatusBlock(module, onDescription, onUpdate, onRefresh)
                }
            }
        }
    }
}

@Composable
private fun WideUpdateBlock(
    module: ModuleCardUi,
    status: CardStatusUi.UpdateAvailable,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
) {
    val colors = CmpTheme.colors
    TonalBlock(
        container = colors.brandPrimaryHighlight,
        border = colors.brandPrimaryDefault,
        radius = CmpRadius.cardMedium,
    ) {
        CmpFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateTag()
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                (module.currentVersion as? CurrentVersionUi.Known)?.let { current ->
                    VersionText(
                        current.version,
                        style = CmpTextStyle.Headline3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                    CmpIcon(
                        icon = CmpIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconS),
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                VersionText(status.version, style = CmpTextStyle.Headline2)
            }
        }
        if (status.description.isBlank()) {
            CmpText(
                text = stringResource(Strings.string.firmware_check_msg_dialog_no_description),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        } else {
            CmpText(
                text = status.description,
                style = CmpTextStyle.Paragraph2,
                maxLines = DESCRIPTION_PREVIEW_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            CmpButton(
                text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                onClick = onDescription,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        }
        CmpButton(
            text = stringResource(Strings.string.firmware_check_btn_update),
            onClick = onUpdate,
            icon = CmpIcons.SystemUpdate,
            fullWidth = true,
            modifier = Modifier.testTag(moduleUpdateTag(module.id)),
        )
    }
}
