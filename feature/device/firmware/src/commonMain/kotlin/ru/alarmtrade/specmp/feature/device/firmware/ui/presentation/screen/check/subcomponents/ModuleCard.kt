package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_a11y_refresh
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_description
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
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
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

private val WideUpdateMinWidth = 200.dp

private const val UPDATE_BUTTON_WEIGHT = 1.4f
private const val DESCRIPTION_PREVIEW_LINES = 2
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
private fun CardHeader(module: ModuleCardUi, onRefresh: () -> Unit, wide: Boolean = false) {
    val canRefresh = module.status !is CardStatusUi.NotSupported &&
        module.status !is CardStatusUi.Searching &&
        module.currentVersion !is CurrentVersionUi.Reading
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(if (wide) CmpSpacing.space16 else CmpSpacing.space12),
        verticalAlignment = if (wide) Alignment.CenterVertically else Alignment.Top,
    ) {
        if (wide) {
            ModuleIcon(size = CmpSize.iconXxl, radius = CmpRadius.cardMedium, iconSize = CmpSize.iconL)
        } else {
            ModuleIcon(size = CmpSize.touchMin, radius = CmpRadius.cardSmall, iconSize = CmpSize.iconL)
        }
        ModuleTitle(
            module,
            titleStyle = if (wide) CmpTextStyle.Headline1 else CmpTextStyle.Headline2,
            modifier = Modifier.weight(1f),
        )
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
private fun CurrentVersionRow(currentVersion: CurrentVersionUi) {
    if (currentVersion is CurrentVersionUi.Absent) return
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VersionLabel(CmpTextStyle.Paragraph2, Modifier.weight(1f))
        CurrentVersionValue(currentVersion, CmpTextStyle.Headline3)
    }
}

@Composable
private fun InlineCurrentVersion(currentVersion: CurrentVersionUi) {
    if (currentVersion is CurrentVersionUi.Absent) return
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (currentVersion is CurrentVersionUi.Known) VersionLabel(CmpTextStyle.Paragraph1)
        CurrentVersionValue(currentVersion, CmpTextStyle.Headline2)
    }
}

@Composable
private fun VersionLabel(style: CmpTextStyle, modifier: Modifier = Modifier) {
    CmpText(
        text = stringResource(Strings.string.firmware_common_label_version),
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        style = style,
        modifier = modifier,
    )
}

@Composable
private fun CurrentVersionValue(currentVersion: CurrentVersionUi, style: CmpTextStyle) {
    when (currentVersion) {
        CurrentVersionUi.Reading -> CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = stringResource(Strings.string.firmware_check_loading_version),
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
            )
            Spinner()
        }

        is CurrentVersionUi.Known -> VersionText(currentVersion.version, style = style)

        CurrentVersionUi.Absent -> Unit
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
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        UnsupportedTitle()
        UnsupportedHint()
    }
}

@Composable
private fun UnsupportedTitle() {
    val colors = CmpTheme.colors
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
}

@Composable
private fun UnsupportedHint() {
    CmpText(
        text = stringResource(Strings.string.firmware_check_msg_unsupported_hint),
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        style = CmpTextStyle.Paragraph2,
    )
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
private fun UpdateTonalBlock(radius: Dp, content: @Composable () -> Unit) {
    val colors = CmpTheme.colors
    CmpCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        containerColor = colors.brandPrimaryHighlight,
        border = BorderStroke(CmpBorderWidth.listItem, colors.brandPrimaryDefault.copy(alpha = TONE_BORDER_ALPHA)),
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            content()
        }
    }
}

@Composable
private fun UpdateAvailableBlock(
    moduleId: Long,
    status: CardStatusUi.UpdateAvailable,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
) {
    UpdateTonalBlock(radius = CmpRadius.cardSmall) {
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
private fun ReinstallButton(moduleId: Long, onUpdate: () -> Unit, type: CmpButtonType = CmpButtonType.Tertiary) {
    CmpButton(
        text = stringResource(Strings.string.firmware_check_btn_reinstall),
        onClick = onUpdate,
        type = type,
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
    CmpBanner(
        text = error.text(Strings.string.firmware_check_error_server),
        tone = CmpBannerTone.Error,
        icon = CmpIcons.CloudOff,
        actionLabel = stringResource(Strings.string.firmware_common_btn_retry),
        onAction = onRetry,
    )
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
    CmpCard(
        modifier = modifier.fillMaxWidth(),
        border = cardBorder(highlighted = status is CardStatusUi.UpdateAvailable),
    ) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space20),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CardHeader(module, onRefresh, wide = true)
            CmpDivider()
            when (status) {
                is CardStatusUi.UpdateAvailable -> WideUpdateBlock(module, status, onDescription, onUpdate)

                is CardStatusUi.Actual -> WideStatusRow {
                    ActualStatus(status.version, wide = true)
                    ReinstallButton(module.id, onUpdate, type = CmpButtonType.Secondary)
                }

                CardStatusUi.Searching -> WideStatusRow {
                    SearchingBlock()
                    InlineCurrentVersion(module.currentVersion)
                }

                CardStatusUi.NotSupported -> CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    WideStatusRow {
                        UnsupportedTitle()
                        InlineCurrentVersion(module.currentVersion)
                    }
                    UnsupportedHint()
                }

                is CardStatusUi.Failed -> CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                    InlineCurrentVersion(module.currentVersion)
                    FailedBlock(status.error, onRefresh)
                }
            }
        }
    }
}

@Composable
private fun WideStatusRow(content: @Composable () -> Unit) {
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
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
    val hasDescription = status.description.isNotBlank()
    UpdateTonalBlock(radius = CmpRadius.cardMedium) {
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
        if (hasDescription) {
            CmpText(
                text = status.description,
                style = CmpTextStyle.Paragraph2,
                maxLines = DESCRIPTION_PREVIEW_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            CmpText(
                text = stringResource(Strings.string.firmware_check_msg_dialog_no_description),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        CmpFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasDescription) {
                CmpButton(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    onClick = onDescription,
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                )
            }
            CmpButton(
                text = stringResource(Strings.string.firmware_check_btn_update),
                onClick = onUpdate,
                icon = CmpIcons.SystemUpdate,
                modifier = Modifier.widthIn(min = WideUpdateMinWidth).testTag(moduleUpdateTag(module.id)),
            )
        }
    }
}
