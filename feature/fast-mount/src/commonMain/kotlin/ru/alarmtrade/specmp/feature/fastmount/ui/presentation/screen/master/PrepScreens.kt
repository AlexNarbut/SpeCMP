package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_btn_advanced
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_btn_firmware
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_btn_other_device
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_error_failed
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_format_outdated
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_label_can
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_label_id
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_label_model
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_label_transport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_loading_device
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_factory
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_no_access
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_outdated
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_ready
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_alarms
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_database
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_msg_unsupported_microbot
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_card
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_factory
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_no_access
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_ready
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_title_unsupported
import ru.alarmtrade.specmp.core.localization.resources.fastmount_check_value_factory
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_next
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_value_ble
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_value_usb
import ru.alarmtrade.specmp.core.localization.resources.fastmount_rail_status_optional
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_btn_start
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_error_load
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_format_steps
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_label_author
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_label_can
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_label_car
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_msg_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_msg_reboots
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_msg_replace
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_msg_usb
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_car
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_configuration
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_plan
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_replace
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_screen
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_unit
import ru.alarmtrade.specmp.core.localization.resources.fastmount_summary_title_usb
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Banner
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.ContentColumn
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Dock
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Hero
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.InfoCard
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.KeyValue
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RatingLabel
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RoundArt
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardNav
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardScaffold
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.title
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

internal const val FAST_MOUNT_CHECK_NEXT_TAG = "fastmount_check_next"
internal const val FAST_MOUNT_UNSUPPORTED_TAG = "fastmount_check_unsupported"
internal const val FAST_MOUNT_SUMMARY_START_TAG = "fastmount_summary_start"

/** Действия подготовки. */
internal class PrepActions(
    val onBack: () -> Unit = {},
    val onRetryCheck: () -> Unit = {},
    val onCheckNext: () -> Unit = {},
    val onOpenFirmwareUpdate: () -> Unit = {},
    val onChooseAnother: () -> Unit = {},
    val onOpenAdvancedMount: () -> Unit = {},
    val onRetrySummary: () -> Unit = {},
    val onStart: () -> Unit = {},
)

/** «Проверка блока» (F3) и «Блок не поддерживает быстрый монтаж» (F2). */
@Composable
internal fun UnitCheckScreen(state: FastMountState, actions: PrepActions) {
    val fixed = state.fixed
    when (val check = state.check) {
        is UnitCheck.Unsupported -> WizardScaffold(
            title = stringResource(Strings.string.fastmount_common_title_screen),
            subtitle = fixed?.title,
            nav = WizardNav.BACK,
            onBack = actions.onChooseAnother,
            centered = true,
            modifier = Modifier.testTag(FAST_MOUNT_UNSUPPORTED_TAG),
            dock = {
                Dock(
                    buttons = listOf(
                        {
                            MainButton(
                                text = stringResource(Strings.string.fastmount_check_btn_other_device),
                                icon = CmpIcons.Search,
                                onClick = actions.onChooseAnother,
                            )
                        },
                        {
                            MainButton(
                                text = stringResource(Strings.string.fastmount_check_btn_advanced),
                                outlined = true,
                                onClick = actions.onOpenAdvancedMount,
                            )
                        },
                    ),
                )
            },
        ) {
            Hero(
                title = stringResource(Strings.string.fastmount_check_title_unsupported),
                text = stringResource(check.reason.message()),
                art = { RoundArt(icon = CmpIcons.Block, tone = BannerTone.WARN) },
            )
        }

        else -> CheckScaffold(state = state, check = check, actions = actions)
    }
}

@Composable
private fun CheckScaffold(state: FastMountState, check: UnitCheck, actions: PrepActions) {
    val verdict = (check as? UnitCheck.Done)?.verdict
    val blocking = verdict?.blocking == true
    WizardScaffold(
        title = stringResource(Strings.string.fastmount_check_title_screen),
        subtitle = state.fixed?.title,
        nav = WizardNav.BACK,
        onBack = actions.onBack,
        centered = check is UnitCheck.Checking || check is UnitCheck.Failed,
        dock = {
            Dock(
                note = if (blocking) stringResource(Strings.string.fastmount_check_msg_locked) else null,
                buttons = buildList {
                    add {
                        MainButton(
                            text = stringResource(Strings.string.fastmount_common_btn_next),
                            icon = CmpIcons.ArrowForward,
                            enabled = verdict != null && !blocking,
                            busy = check is UnitCheck.Checking,
                            onClick = actions.onCheckNext,
                            modifier = Modifier.testTag(FAST_MOUNT_CHECK_NEXT_TAG),
                        )
                    }
                    if (verdict is UnitVerdict.NoAccess) {
                        add {
                            MainButton(
                                text = stringResource(Strings.string.fastmount_check_btn_other_device),
                                outlined = true,
                                onClick = actions.onChooseAnother,
                            )
                        }
                    }
                },
            )
        },
    ) {
        when (check) {
            UnitCheck.Checking -> Hero(
                title = stringResource(Strings.string.fastmount_check_loading_device),
                art = { CmpCircularProgressIndicator(modifier = Modifier.size(56.dp)) },
            )

            is UnitCheck.Failed -> Hero(
                title = stringResource(Strings.string.fastmount_check_error_failed),
                text = check.error.text(),
                art = { RoundArt(icon = CmpIcons.CloudOff, tone = BannerTone.ERROR) },
            ) {
                MainButton(
                    text = stringResource(Strings.string.fastmount_common_btn_retry),
                    icon = CmpIcons.Refresh,
                    outlined = true,
                    onClick = actions.onRetryCheck,
                )
            }

            is UnitCheck.Done -> ContentColumn {
                VerdictBanner(check.verdict, actions.onOpenFirmwareUpdate)
                UnitCard(state, check.verdict)
            }

            is UnitCheck.Unsupported -> Unit
        }
    }
}

@Composable
private fun VerdictBanner(verdict: UnitVerdict, onOpenFirmwareUpdate: () -> Unit) {
    when (verdict) {
        UnitVerdict.Ready -> Banner(
            tone = BannerTone.SUCCESS,
            title = stringResource(Strings.string.fastmount_check_title_ready),
            text = stringResource(Strings.string.fastmount_check_msg_ready),
        )

        is UnitVerdict.Outdated -> Banner(
            tone = BannerTone.WARN,
            icon = CmpIcons.SystemUpdate,
            title = stringResource(Strings.string.fastmount_check_format_outdated, verdict.latest),
            text = stringResource(Strings.string.fastmount_check_msg_outdated),
            action = stringResource(Strings.string.fastmount_check_btn_firmware),
            onAction = onOpenFirmwareUpdate,
        )

        UnitVerdict.FactoryCan -> Banner(
            tone = BannerTone.ERROR,
            title = stringResource(Strings.string.fastmount_check_title_factory),
            text = stringResource(Strings.string.fastmount_check_msg_factory),
            action = stringResource(Strings.string.fastmount_check_btn_firmware),
            onAction = onOpenFirmwareUpdate,
        )

        is UnitVerdict.NoAccess -> Banner(
            tone = BannerTone.ERROR,
            icon = CmpIcons.Lock,
            title = stringResource(Strings.string.fastmount_check_title_no_access),
            text = verdict.message.ifBlank { stringResource(Strings.string.fastmount_check_msg_no_access) },
        )
    }
}

@Composable
private fun UnitCard(state: FastMountState, verdict: UnitVerdict) {
    val colors = cmpThemeColors()
    val fixed = state.fixed ?: return
    val unit = state.unit
    InfoCard(title = stringResource(Strings.string.fastmount_check_title_card)) {
        KeyValue(stringResource(Strings.string.fastmount_check_label_model), fixed.systemName)
        KeyValue(stringResource(Strings.string.fastmount_check_label_id), fixed.systemId.toString())
        if (unit != null) {
            KeyValue(
                label = stringResource(Strings.string.fastmount_check_label_firmware),
                value = unit.firmware,
                tone = if (verdict is UnitVerdict.Outdated) colors.tertiary else null,
            )
            unit.canCode?.let { code ->
                KeyValue(
                    label = stringResource(Strings.string.fastmount_check_label_can),
                    value = if (unit.canFactory) {
                        stringResource(
                            Strings.string.fastmount_check_value_factory,
                            code,
                        )
                    } else {
                        code
                    },
                    tone = if (unit.canFactory) colors.error else null,
                )
            }
        }
        KeyValue(stringResource(Strings.string.fastmount_check_label_transport), transportLabel(state.usb))
    }
}

@Composable
internal fun transportLabel(usb: Boolean): String = stringResource(
    if (usb) Strings.string.fastmount_common_value_usb else Strings.string.fastmount_common_value_ble,
)

/** Сводка перед стартом (F5): блок, автомобиль, конфигурация, план шагов, предупреждения. */
@Composable
internal fun SummaryScreen(state: FastMountState, actions: PrepActions) {
    WizardScaffold(
        title = stringResource(Strings.string.fastmount_summary_title_screen),
        subtitle = state.fixed?.title,
        nav = WizardNav.BACK,
        onBack = actions.onBack,
        centered = state.summary !is SummaryLoad.Ready,
        dock = {
            Dock(
                buttons = listOf {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_summary_btn_start),
                        icon = CmpIcons.PlayArrow,
                        enabled = state.summary is SummaryLoad.Ready,
                        onClick = actions.onStart,
                        modifier = Modifier.testTag(FAST_MOUNT_SUMMARY_START_TAG),
                    )
                },
            )
        },
    ) {
        val configuration = state.configuration
        when (val summary = state.summary) {
            SummaryLoad.Loading -> CmpCircularProgressIndicator(modifier = Modifier.size(56.dp))

            is SummaryLoad.Failed -> Hero(
                title = stringResource(Strings.string.fastmount_summary_error_load),
                text = summary.error.text(),
                art = { RoundArt(icon = CmpIcons.CloudOff, tone = BannerTone.ERROR) },
            ) {
                MainButton(
                    text = stringResource(Strings.string.fastmount_common_btn_retry),
                    icon = CmpIcons.Refresh,
                    outlined = true,
                    onClick = actions.onRetrySummary,
                )
            }

            SummaryLoad.Ready -> if (configuration != null) SummaryBody(state, configuration)
        }
    }
}

@Composable
private fun SummaryBody(state: FastMountState, configuration: FastMountConfiguration) {
    val colors = cmpThemeColors()
    ContentColumn(maxWidth = 1040.dp) {
        CmpColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CmpText(text = stringResource(Strings.string.fastmount_summary_title_lead), fontSize = 24.sp)
            CmpText(
                text = stringResource(Strings.string.fastmount_summary_msg_lead),
                style = CmpTextStyle.BodyLarge,
                color = colors.onSurfaceVariant,
            )
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val threeColumns = maxWidth >= 900.dp
            val cards: List<@Composable (Modifier) -> Unit> = listOf(
                { modifier -> SummaryUnitCard(state, modifier) },
                { modifier -> SummaryCarCard(state.car, modifier) },
                { modifier -> SummaryConfigurationCard(configuration, modifier) },
            )
            if (threeColumns) {
                CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards.forEach { card -> card(Modifier.weight(1f)) }
                }
            } else {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) { cards.forEach { it(Modifier) } }
            }
        }
        state.plan?.let { PlanCard(it) }
        if (state.usb) {
            Banner(
                tone = BannerTone.INFO,
                icon = CmpIcons.Usb,
                title = stringResource(Strings.string.fastmount_summary_title_usb),
                text = stringResource(Strings.string.fastmount_summary_msg_usb),
            )
        }
        Banner(
            tone = BannerTone.WARN,
            title = stringResource(Strings.string.fastmount_summary_title_replace),
            text = stringResource(Strings.string.fastmount_summary_msg_replace),
        )
    }
}

@Composable
private fun SummaryUnitCard(state: FastMountState, modifier: Modifier) {
    val fixed = state.fixed ?: return
    InfoCard(
        title = stringResource(Strings.string.fastmount_summary_title_unit),
        icon = CmpIcons.Sensors,
        modifier = modifier,
    ) {
        KeyValue(stringResource(Strings.string.fastmount_check_label_model), fixed.systemName)
        KeyValue(stringResource(Strings.string.fastmount_check_label_id), fixed.systemId.toString())
        state.unit?.let { KeyValue(stringResource(Strings.string.fastmount_check_label_firmware), it.firmware) }
        KeyValue(stringResource(Strings.string.fastmount_check_label_transport), transportLabel(state.usb))
    }
}

@Composable
private fun SummaryCarCard(car: MountCar?, modifier: Modifier) {
    car ?: return
    InfoCard(
        title = stringResource(Strings.string.fastmount_summary_title_car),
        icon = CmpIcons.DirectionsCar,
        modifier = modifier,
    ) {
        KeyValue(stringResource(Strings.string.fastmount_summary_label_car), car.title)
        if (car.canCode.isNotBlank()) KeyValue(stringResource(Strings.string.fastmount_summary_label_can), car.canCode)
    }
}

@Composable
private fun SummaryConfigurationCard(configuration: FastMountConfiguration, modifier: Modifier) {
    val colors = cmpThemeColors()
    InfoCard(
        title = stringResource(Strings.string.fastmount_summary_title_configuration),
        icon = CmpIcons.CloudDone,
        modifier = modifier,
    ) {
        CmpText(text = configuration.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = stringResource(Strings.string.fastmount_summary_label_author, configuration.author),
                style = CmpTextStyle.BodyLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (configuration.rating > 0) RatingLabel(configuration.rating)
        }
    }
}

@Composable
private fun PlanCard(plan: FastMountPlan) {
    val colors = cmpThemeColors()
    val numbered = plan.steps.count { !it.conditional }
    InfoCard(
        title = stringResource(Strings.string.fastmount_summary_title_plan),
        aside = {
            CmpText(
                text = stringResource(Strings.string.fastmount_summary_format_steps, numbered),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
        },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val twoColumns = maxWidth >= 600.dp
            var number = 0
            val rows = plan.steps.map { planned ->
                if (!planned.conditional) number += 1
                Triple(planned, number, planned.conditional)
            }
            val half = (rows.size + 1) / 2
            val columns = if (twoColumns) listOf(rows.take(half), rows.drop(half)) else listOf(rows)
            CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                columns.forEach { column ->
                    CmpColumn(modifier = Modifier.weight(1f)) {
                        column.forEach { (planned, n, optional) -> PlanRow(planned.step.railKey(), n, optional) }
                    }
                }
            }
        }
        CmpRow(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CmpIcon(
                CmpIcons.Autorenew,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            CmpText(
                text = stringResource(Strings.string.fastmount_summary_msg_reboots),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlanRow(key: RailKey?, number: Int, optional: Boolean) {
    key ?: return
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpBox(
            modifier = Modifier.size(36.dp).border(2.dp, colors.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (optional) {
                CmpIcon(
                    CmpIcons.Add,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                CmpText(text = number.toString(), fontWeight = FontWeight.Bold, color = colors.onSurfaceVariant)
            }
        }
        CmpColumn {
            CmpText(
                text = stringResource(key.title()),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (optional) colors.onSurfaceVariant else colors.onSurface,
            )
            if (optional) {
                CmpText(
                    text = stringResource(Strings.string.fastmount_rail_status_optional),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun UnsupportedReason.message() = when (this) {
    UnsupportedReason.NOT_ALARM -> Strings.string.fastmount_check_msg_unsupported_alarms
    UnsupportedReason.MICROBOT -> Strings.string.fastmount_check_msg_unsupported_microbot
    UnsupportedReason.NOT_IN_DATABASE -> Strings.string.fastmount_check_msg_unsupported_database
}

/** Текст ошибки для плашки/экрана; предметные ошибки шагов — свои строки. */
@Composable
internal fun FastMountError.text(): String? = when (this) {
    is FastMountError.Common -> error.message()
    is FastMountError.Step -> stringResource(failure.message())
    FastMountError.Unknown -> null
}
