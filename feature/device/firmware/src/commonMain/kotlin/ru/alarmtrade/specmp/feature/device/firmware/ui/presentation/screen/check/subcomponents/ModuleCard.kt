package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.titleRes
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CardStatusUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CurrentVersionUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.ModuleCardUi

private val ButtonHeight = 48.dp

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
    val colors = cmpThemeColors()
    val highlighted = module.status is CardStatusUi.UpdateAvailable
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = if (highlighted) {
            BorderStroke(2.dp, colors.primary)
        } else {
            BorderStroke(1.dp, colors.outlineVariant)
        },
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CardHeader(module, onRefresh)
            CurrentVersionRow(module.currentVersion)
            StatusBlock(module, onDescription, onUpdate, onRefresh)
        }
    }
}

@Composable
private fun CardHeader(module: ModuleCardUi, onRefresh: () -> Unit) {
    val colors = cmpThemeColors()
    val canRefresh = module.status !is CardStatusUi.NotSupported &&
        module.status !is CardStatusUi.Searching &&
        module.currentVersion !is CurrentVersionUi.Reading
    CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        CmpSurface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = colors.primaryContainer,
        ) {
            CmpBox(contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = CmpIcons.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = colors.onPrimaryContainer,
                )
            }
        }
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(
                text = stringResource(module.kind.titleRes()).uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
            CmpText(text = module.title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
        if (module.status !is CardStatusUi.NotSupported) {
            CmpIconButton(
                onClick = onRefresh,
                icon = CmpIcons.Refresh,
                contentDescription = stringResource(Strings.string.firmware_check_a11y_refresh),
                modifier = Modifier.size(ButtonHeight).testTag(moduleRefreshTag(module.id)),
                enabled = canRefresh,
            )
        }
    }
}

@Composable
private fun CurrentVersionRow(currentVersion: CurrentVersionUi) {
    val colors = cmpThemeColors()
    if (currentVersion is CurrentVersionUi.Absent) return
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.firmware_common_label_version),
            color = colors.onSurfaceVariant,
            style = CmpTextStyle.Body,
        )
        when (currentVersion) {
            CurrentVersionUi.Reading -> CmpRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = stringResource(Strings.string.firmware_check_loading_version),
                    color = colors.onSurfaceVariant,
                    style = CmpTextStyle.Body,
                )
                CmpCircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.5.dp)
            }

            is CurrentVersionUi.Known -> VersionText(currentVersion.version)

            CurrentVersionUi.Absent -> Unit
        }
    }
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
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.error,
            )
            CmpText(
                text = stringResource(Strings.string.firmware_check_label_unsupported),
                fontWeight = FontWeight.Medium,
                color = colors.error,
            )
        }
        CmpText(
            text = stringResource(Strings.string.firmware_check_msg_unsupported_hint),
            color = colors.onSurfaceVariant,
            style = CmpTextStyle.Body,
        )
    }
}

@Composable
private fun SearchingBlock() {
    CmpRow(
        modifier = Modifier.heightIn(min = ButtonHeight),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
        CmpText(
            text = stringResource(Strings.string.firmware_check_loading_search),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.Body,
        )
    }
}

@Composable
private fun UpdateAvailableBlock(
    moduleId: Long,
    status: CardStatusUi.UpdateAvailable,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        CmpColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpIcon(icon = CmpIcons.ArrowCircleUp, contentDescription = null, modifier = Modifier.size(24.dp))
                    CmpText(
                        text = stringResource(Strings.string.firmware_check_label_update_available),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                VersionText(status.version, fontSize = 18.sp, color = colors.onPrimaryContainer)
            }
            CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpOutlinedButton(
                    onClick = onDescription,
                    modifier = Modifier.weight(1f).height(ButtonHeight),
                ) { OneLine(stringResource(Strings.string.firmware_check_btn_description)) }
                CmpButton(
                    onClick = onUpdate,
                    modifier = Modifier.weight(
                        UPDATE_BUTTON_WEIGHT,
                    ).height(ButtonHeight).testTag(moduleUpdateTag(moduleId)),
                ) { OneLine(stringResource(Strings.string.firmware_check_btn_update)) }
            }
        }
    }
}

@Composable
private fun ActualBlock(moduleId: Long, status: CardStatusUi.Actual, onUpdate: () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(
                icon = CmpIcons.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = cmpThemeColors().primary,
            )
            CmpText(text = stringResource(Strings.string.firmware_check_label_actual), style = CmpTextStyle.Body)
            VersionText(status.version, fontSize = 15.sp)
        }
        CmpTextButton(
            text = stringResource(Strings.string.firmware_check_btn_reinstall),
            onClick = onUpdate,
            modifier = Modifier.height(ButtonHeight).testTag(moduleUpdateTag(moduleId)),
        )
    }
}

@Composable
private fun FailedBlock(error: FirmwareError, onRetry: () -> Unit) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer,
    ) {
        CmpColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpIcon(icon = CmpIcons.CloudOff, contentDescription = null, modifier = Modifier.size(24.dp))
                CmpText(text = error.text(Strings.string.firmware_check_error_server), style = CmpTextStyle.Body)
            }
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_common_btn_retry),
                onClick = onRetry,
                modifier = Modifier.height(ButtonHeight),
            )
        }
    }
}

private const val UPDATE_BUTTON_WEIGHT = 1.4f

/** Подписи кнопок карточек не переносятся. */
@Composable
private fun OneLine(text: String) {
    CmpText(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/**
 * Широкая карточка (раскладной, планшет-портрет, один модуль на широком окне): слева модуль, версия и «Проверить
 * заново», справа статус; у обновления — переход версий, начало описания и крупная «Обновить».
 */
@Composable
private fun ModuleCardWide(
    module: ModuleCardUi,
    onRefresh: () -> Unit,
    onDescription: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val status = module.status
    val update = status is CardStatusUi.UpdateAvailable
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = if (update) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                WideHeader(module)
                CurrentVersionRow(module.currentVersion)
                if (status !is CardStatusUi.NotSupported) {
                    CmpTextButton(
                        onClick = onRefresh,
                        enabled =
                            status !is CardStatusUi.Searching && module.currentVersion !is CurrentVersionUi.Reading,
                        modifier = Modifier.height(ButtonHeight).testTag(moduleRefreshTag(module.id)),
                    ) {
                        CmpIcon(icon = CmpIcons.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        CmpBox(modifier = Modifier.padding(start = 8.dp)) {
                            OneLine(stringResource(Strings.string.firmware_check_btn_refresh))
                        }
                    }
                }
            }
            CmpColumn(
                modifier = Modifier.weight(if (update) WIDE_UPDATE_WEIGHT else 1f),
            ) {
                when (status) {
                    is CardStatusUi.UpdateAvailable ->
                        WideUpdateBlock(module, status, onDescription, onUpdate)

                    else -> StatusBlock(module, onDescription, onUpdate, onRefresh)
                }
            }
        }
    }
}

@Composable
private fun WideHeader(module: ModuleCardUi) {
    val colors = cmpThemeColors()
    CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        CmpSurface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.primaryContainer,
        ) {
            CmpBox(contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = CmpIcons.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colors.onPrimaryContainer,
                )
            }
        }
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(
                text = stringResource(module.kind.titleRes()).uppercase(),
                style = CmpTextStyle.Caption,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
            )
            CmpText(text = module.title, style = CmpTextStyle.Title)
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
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpIcon(icon = CmpIcons.ArrowCircleUp, contentDescription = null, modifier = Modifier.size(26.dp))
                    CmpText(
                        text = stringResource(Strings.string.firmware_check_label_update_available),
                        style = CmpTextStyle.BodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    (module.currentVersion as? CurrentVersionUi.Known)?.let { current ->
                        VersionText(current.version, color = colors.onPrimaryContainer)
                        CmpIcon(
                            icon = CmpIcons.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    VersionText(status.version, color = colors.onPrimaryContainer)
                }
            }
            if (status.description.isBlank()) {
                CmpText(
                    text = stringResource(Strings.string.firmware_check_msg_dialog_no_description),
                    style = CmpTextStyle.Body,
                )
            } else {
                CmpText(
                    text = status.description,
                    style = CmpTextStyle.Body,
                    maxLines = DESCRIPTION_PREVIEW_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                CmpTextButton(onClick = onDescription, modifier = Modifier.height(ButtonHeight)) {
                    OneLine(stringResource(Strings.string.firmware_check_label_dialog_full_description))
                }
            }
            CmpButton(
                onClick = onUpdate,
                modifier = Modifier.fillMaxWidth().height(WideButtonHeight).testTag(moduleUpdateTag(module.id)),
            ) { OneLine(stringResource(Strings.string.firmware_check_btn_update)) }
        }
    }
}

private const val WIDE_UPDATE_WEIGHT = 1.25f
private const val DESCRIPTION_PREVIEW_LINES = 3
private val WideButtonHeight = 52.dp
