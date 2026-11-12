package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_archive
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_reflash
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_error_failed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_error_link_lost
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_bypass
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_clone
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_clone_attempts
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_doors
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_engine
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_hood
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_ignition
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_module_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_module_main
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_security
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_temperature
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_trunk
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_label_voltage
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_status_all_good
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_status_up_to_date
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_status_update
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_status_warnings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_title_autostart
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_title_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_title_state
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_armed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_can_factory
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_clone_done
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_clone_failed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_clone_not_needed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_clone_required
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_closed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_disarmed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_learned
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_not_learned
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_off
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_on
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_open
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_running
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_stopped
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_temperature
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_voltage
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.settingValueText

internal fun resultTag(entryId: Long): String = "mountassistant_card_$entryId"

internal fun archiveTag(moduleId: Long): String = "mountassistant_archive_$moduleId"

internal fun flashTag(moduleId: Long): String = "mountassistant_flash_$moduleId"

/** Карточка проверки прошивки, состояния или статуса обхода: загрузка, данные, ошибка с «Повторить». */
@Composable
internal fun DeviceResultCard(
    entryId: Long,
    kind: DeviceResultKind,
    content: ResultContent,
    onModuleArchive: (moduleId: Long) -> Unit = {},
    onModuleFlash: (moduleId: Long) -> Unit = {},
    onRetry: () -> Unit,
) {
    CardShell(modifier = Modifier.testTag(resultTag(entryId))) {
        val error = content as? ResultContent.Error
        CardHeader(icon = kind.icon, title = kind.title(), error = error != null) {
            if (content is ResultContent.Data) ResultBadge(content)
        }
        when (content) {
            ResultContent.Loading -> SkeletonRows(rows = if (kind == DeviceResultKind.STATE) 4 else 3)
            is ResultContent.Error -> ResultError(content.failure, onRetry)
            is ResultContent.FirmwareData -> FirmwareBody(content.check, content.can, onModuleArchive, onModuleFlash)
            is ResultContent.StateData -> StateBody(content.summary)
            is ResultContent.AutostartData -> AutostartBody(content.status, content.variant)
            is ResultContent.SettingsData -> Unit
        }
    }
}

internal enum class DeviceResultKind(val icon: CmpIcons) {
    FIRMWARE(CmpIcons.SystemUpdate),
    STATE(CmpIcons.Speed),
    AUTOSTART(CmpIcons.Key),
}

@Composable
private fun DeviceResultKind.title(): String = stringResource(
    when (this) {
        DeviceResultKind.FIRMWARE -> Strings.string.mountassistant_card_title_firmware
        DeviceResultKind.STATE -> Strings.string.mountassistant_card_title_state
        DeviceResultKind.AUTOSTART -> Strings.string.mountassistant_card_title_autostart
    },
)

@Composable
private fun ResultBadge(content: ResultContent.Data) {
    when (content) {
        is ResultContent.FirmwareData -> if (content.check.modules.any { it.updateAvailable }) {
            AssistantPill(text = stringResource(Strings.string.mountassistant_card_status_update), tone = PillTone.WARN)
        } else {
            AssistantPill(
                text = stringResource(Strings.string.mountassistant_card_status_up_to_date),
                tone = PillTone.OK,
                icon = CmpIcons.CheckCircle,
            )
        }

        is ResultContent.StateData -> if (content.summary.warnings > 0) {
            AssistantPill(
                text = stringResource(Strings.string.mountassistant_card_status_warnings, content.summary.warnings),
                tone = PillTone.WARN,
                icon = CmpIcons.Warning,
            )
        } else {
            AssistantPill(text = stringResource(Strings.string.mountassistant_card_status_all_good), tone = PillTone.OK)
        }

        is ResultContent.AutostartData, is ResultContent.SettingsData -> Unit
    }
}

@Composable
internal fun ResultError(failure: ResultFailure, onRetry: () -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpText(text = failureText(failure), style = CmpTextStyle.BodyLarge)
        CmpOutlinedButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            CmpIcon(icon = CmpIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            CmpText(
                text = stringResource(Strings.string.mountassistant_card_btn_retry),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
internal fun failureText(failure: ResultFailure): String = when (failure) {
    ResultFailure.LinkLost -> stringResource(Strings.string.mountassistant_card_error_link_lost)
    ResultFailure.WrongPin, ResultFailure.Failed -> stringResource(Strings.string.mountassistant_card_error_failed)
    is ResultFailure.Common -> failure.error.message()
}

@Composable
private fun FirmwareBody(
    check: FirmwareCheck,
    can: CanFacts?,
    onModuleArchive: (moduleId: Long) -> Unit,
    onModuleFlash: (moduleId: Long) -> Unit,
) {
    CmpColumn {
        check.modules.forEachIndexed { index, module ->
            if (index > 0) CmpDivider()
            CmpColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                ModuleRow(module)
                ModuleActions(module, onModuleArchive, onModuleFlash)
            }
        }
        if (can != null) {
            CmpDivider()
            CmpColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                KeyValueRow(
                    label = stringResource(Strings.string.mountassistant_card_label_can),
                    value = if (can.factory) {
                        stringResource(Strings.string.mountassistant_card_value_can_factory)
                    } else {
                        listOfNotNull(can.version, can.code).joinToString(" · ")
                    },
                    tone = if (can.factory) PillTone.WARN else PillTone.OK,
                    icon = if (can.factory) CmpIcons.Warning else CmpIcons.CheckCircle,
                )
            }
        }
    }
}

/** Обновить до новой версии или перепрошить актуальную — обе ведут в загрузчик; архив — версии этого модуля. */
@Composable
private fun ModuleActions(
    module: FirmwareModuleCheck,
    onArchive: (moduleId: Long) -> Unit,
    onFlash: (moduleId: Long) -> Unit,
) {
    CmpFlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val latest = module.latest
        if (latest != null) {
            ModuleButton(
                icon = if (module.updateAvailable) CmpIcons.ArrowCircleUp else CmpIcons.Refresh,
                text = if (module.updateAvailable) {
                    stringResource(Strings.string.mountassistant_card_btn_update, latest)
                } else {
                    stringResource(Strings.string.mountassistant_card_btn_reflash)
                },
                tag = flashTag(module.id),
                onClick = { onFlash(module.id) },
            )
        }
        ModuleButton(
            icon = CmpIcons.Inventory2,
            text = stringResource(Strings.string.mountassistant_card_btn_archive),
            tag = archiveTag(module.id),
            onClick = { onArchive(module.id) },
        )
    }
}

@Composable
private fun ModuleButton(icon: CmpIcons, text: String, tag: String, onClick: () -> Unit) {
    CmpTextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp).testTag(tag)) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        CmpText(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

/** Версии — в метке справа, статус — подписью: так название модуля не переносится на узком экране. */
@Composable
private fun ModuleRow(module: FirmwareModuleCheck) {
    val label = when (module.role) {
        FirmwareModuleRole.MAIN -> stringResource(Strings.string.mountassistant_card_label_module_main)
        FirmwareModuleRole.BLUETOOTH -> stringResource(Strings.string.mountassistant_card_label_module_bluetooth)
        FirmwareModuleRole.OTHER -> module.title
    }
    val versions = when {
        module.installed == null -> module.latest
        module.updateAvailable && module.latest != null -> "${module.installed} → ${module.latest}"
        else -> module.installed
    }
    val status = stringResource(
        if (module.updateAvailable) {
            Strings.string.mountassistant_card_status_update
        } else {
            Strings.string.mountassistant_card_status_up_to_date
        },
    )
    KeyValueRow(
        label = label,
        sub = status.takeIf { versions != null },
        value = versions ?: status,
        tone = if (module.updateAvailable) PillTone.WARN else PillTone.OK,
        icon = if (module.updateAvailable) CmpIcons.ArrowCircleUp else CmpIcons.CheckCircle,
    )
}

private data class StateItem(val icon: CmpIcons, val label: String, val value: String, val warn: Boolean)

@Composable
private fun StateBody(summary: StateSummary) {
    val on = stringResource(Strings.string.mountassistant_card_value_on)
    val off = stringResource(Strings.string.mountassistant_card_value_off)
    val open = stringResource(Strings.string.mountassistant_card_value_open)
    val closed = stringResource(Strings.string.mountassistant_card_value_closed)
    fun zone(zone: StateZone) = if (zone in summary.openZones) open else closed
    val items = listOf(
        StateItem(
            CmpIcons.Security,
            stringResource(Strings.string.mountassistant_card_label_security),
            stringResource(
                if (summary.armed) {
                    Strings.string.mountassistant_card_value_armed
                } else {
                    Strings.string.mountassistant_card_value_disarmed
                },
            ),
            warn = false,
        ),
        StateItem(
            CmpIcons.SensorDoor,
            stringResource(Strings.string.mountassistant_card_label_doors),
            zone(StateZone.DOORS),
            StateZone.DOORS in summary.openZones,
        ),
        StateItem(
            CmpIcons.DirectionsCar,
            stringResource(Strings.string.mountassistant_card_label_hood),
            zone(StateZone.HOOD),
            StateZone.HOOD in summary.openZones,
        ),
        StateItem(
            CmpIcons.Luggage,
            stringResource(Strings.string.mountassistant_card_label_trunk),
            zone(StateZone.TRUNK),
            StateZone.TRUNK in summary.openZones,
        ),
        StateItem(
            CmpIcons.Key,
            stringResource(Strings.string.mountassistant_card_label_ignition),
            if (summary.ignitionOn) on else off,
            warn = false,
        ),
        StateItem(
            CmpIcons.PowerSettingsNew,
            stringResource(Strings.string.mountassistant_card_label_engine),
            stringResource(
                if (summary.engineRunning) {
                    Strings.string.mountassistant_card_value_running
                } else {
                    Strings.string.mountassistant_card_value_stopped
                },
            ),
            warn = false,
        ),
        StateItem(
            CmpIcons.Memory,
            stringResource(Strings.string.mountassistant_card_label_voltage),
            stringResource(Strings.string.mountassistant_card_value_voltage, summary.voltage.oneDecimal()),
            warn = false,
        ),
        StateItem(
            CmpIcons.Speed,
            stringResource(Strings.string.mountassistant_card_label_temperature),
            stringResource(Strings.string.mountassistant_card_value_temperature, summary.temperature),
            warn = false,
        ),
    )
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { StateTile(it, Modifier.weight(1f).fillMaxHeight()) }
            }
        }
    }
}

private const val TENTHS = 10

private fun Float.oneDecimal(): String {
    val tenths = kotlin.math.round(this * TENTHS).toInt()
    return "${tenths / TENTHS}.${kotlin.math.abs(tenths % TENTHS)}"
}

@Composable
private fun StateTile(item: StateItem, modifier: Modifier) {
    val colors = cmpThemeColors()
    val content = if (item.warn) colors.onTertiaryContainer else colors.onSurface
    CmpRow(
        modifier = modifier.heightIn(min = 56.dp)
            .background(if (item.warn) colors.tertiaryContainer else colors.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(icon = item.icon, contentDescription = null, tint = if (item.warn) content else colors.primary)
        CmpColumn {
            CmpText(
                text = item.label,
                style = CmpTextStyle.Body,
                fontWeight = FontWeight.Medium,
                color = if (item.warn) content else colors.onSurfaceVariant,
            )
            CmpText(text = item.value, style = CmpTextStyle.BodyLarge, fontWeight = FontWeight.Medium, color = content)
        }
    }
}

@Composable
private fun AutostartBody(status: AutostartStatus, variant: FoundSetting?) {
    CmpColumn {
        val variantValue = variant?.value
        if (variant != null && variantValue != null) {
            KeyValueRow(label = variant.name, value = settingValueText(variant, variantValue), tone = PillTone.NEUTRAL)
            CmpDivider()
        }
        KeyValueRow(
            label = stringResource(Strings.string.mountassistant_card_label_bypass),
            value = stringResource(
                if (status.bypassLearned) {
                    Strings.string.mountassistant_card_value_learned
                } else {
                    Strings.string.mountassistant_card_value_not_learned
                },
            ),
            tone = if (status.bypassLearned) PillTone.OK else PillTone.NEUTRAL,
            icon = CmpIcons.CheckCircle.takeIf { status.bypassLearned },
        )
        CmpDivider()
        KeyValueRow(
            label = stringResource(Strings.string.mountassistant_card_label_clone),
            value = cloneText(status.clone),
            tone = when (status.clone) {
                CloneState.DONE -> PillTone.OK
                CloneState.FAILED -> PillTone.ERROR
                CloneState.REQUIRED -> PillTone.WARN
                CloneState.NOT_NEEDED -> PillTone.NEUTRAL
            },
            icon = CmpIcons.CheckCircle.takeIf { status.clone == CloneState.DONE },
        )
        val attempts = status.cloneAttempts
        if (attempts != null) {
            CmpDivider()
            KeyValueRow(
                label = stringResource(Strings.string.mountassistant_card_label_clone_attempts),
                value = attempts.toString(),
                tone = if (attempts > 1) PillTone.NEUTRAL else PillTone.WARN,
            )
        }
    }
}

@Composable
internal fun cloneText(clone: CloneState): String = stringResource(
    when (clone) {
        CloneState.DONE -> Strings.string.mountassistant_card_value_clone_done
        CloneState.REQUIRED -> Strings.string.mountassistant_card_value_clone_required
        CloneState.NOT_NEEDED -> Strings.string.mountassistant_card_value_clone_not_needed
        CloneState.FAILED -> Strings.string.mountassistant_card_value_clone_failed
    },
)

@Composable
internal fun bypassText(learned: Boolean): String = stringResource(
    if (learned) {
        Strings.string.mountassistant_card_value_learned
    } else {
        Strings.string.mountassistant_card_value_not_learned
    },
)
