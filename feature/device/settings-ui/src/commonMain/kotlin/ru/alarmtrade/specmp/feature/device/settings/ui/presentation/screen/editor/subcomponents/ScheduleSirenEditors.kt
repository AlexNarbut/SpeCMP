package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_schedule_label_evening
import ru.alarmtrade.specmp.core.localization.resources.settings_schedule_label_morning
import ru.alarmtrade.specmp.core.localization.resources.settings_siren_label_melody
import ru.alarmtrade.specmp.core.localization.resources.settings_siren_label_number
import ru.alarmtrade.specmp.core.localization.resources.settings_siren_label_tone
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartSchedule
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenTone
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenToneType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.text

private val SlotMinHeight = 64.dp
private val SwitchBoxWidth = 64.dp
private val TimeChipMinWidth = 96.dp

@Composable
internal fun ScheduleEditor(value: AutoStartSchedule, onChange: (AutoStartSchedule) -> Unit) {
    var openSlot by rememberSaveable { mutableStateOf<Int?>(null) }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        ScheduleSlot(
            label = stringResource(Strings.string.settings_schedule_label_morning),
            enabled = value.isMorningStartEnabled,
            time = value.morningStartTime,
            open = openSlot == MORNING,
            onEnabledChange = { onChange(value.copy(isMorningStartEnabled = it)) },
            onToggleOpen = { openSlot = if (openSlot == MORNING) null else MORNING },
            onTimeChange = { onChange(value.copy(morningStartTime = it)) },
        )
        ScheduleSlot(
            label = stringResource(Strings.string.settings_schedule_label_evening),
            enabled = value.isEveningStartEnabled,
            time = value.eveningStartTime,
            open = openSlot == EVENING,
            onEnabledChange = { onChange(value.copy(isEveningStartEnabled = it)) },
            onToggleOpen = { openSlot = if (openSlot == EVENING) null else EVENING },
            onTimeChange = { onChange(value.copy(eveningStartTime = it)) },
        )
    }
}

private const val MORNING = 0
private const val EVENING = 1

@Suppress("LongParameterList")
@Composable
private fun ScheduleSlot(
    label: String,
    enabled: Boolean,
    time: TimeOfDay,
    open: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onToggleOpen: () -> Unit,
    onTimeChange: (TimeOfDay) -> Unit,
) {
    val colors = CmpTheme.colors
    CmpSurface(
        shape = RoundedCornerShape(CmpRadius.cardMedium),
        color = colors.backgroundSurfaceCard,
        border = BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight),
    ) {
        CmpColumn {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SlotMinHeight)
                    .padding(start = CmpSpacing.space8, end = CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpBox(
                    modifier = Modifier.size(width = SwitchBoxWidth, height = CmpSize.touchMin),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpSwitch(checked = enabled, onCheckedChange = onEnabledChange)
                }
                CmpText(text = label, style = CmpTextStyle.Headline2, modifier = Modifier.weight(1f))
                CmpSurface(
                    shape = RoundedCornerShape(CmpRadius.controls),
                    color = if (open) colors.backgroundSurfaceHighlight else colors.backgroundSurfaceUltraHighlight,
                    border = BorderStroke(CmpBorderWidth.inputs, colors.borderLight),
                    modifier = Modifier
                        .widthIn(min = TimeChipMinWidth)
                        .heightIn(min = CmpSize.touchMin)
                        .clickable(enabled = enabled, role = Role.Button, onClick = onToggleOpen),
                ) {
                    CmpBox(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = CmpSpacing.space12),
                    ) {
                        CmpText(
                            text = time.text(),
                            style = CmpTextStyle.Headline2,
                            textAlign = TextAlign.Center,
                            color = if (enabled) {
                                colors.foregroundOnDarkPrimaryDefault
                            } else {
                                colors.foregroundOnDarkPrimaryDisabled
                            },
                            modifier = Modifier.padding(vertical = CmpSpacing.space12),
                        )
                    }
                }
            }
            if (open && enabled) {
                CmpDivider()
                TimeEditor(time, onTimeChange)
            }
        }
    }
}

@Composable
internal fun SirenEditor(setting: SirenSetting, value: SirenTone, onChange: (SirenTone) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpSegmentedButton(
            options = listOf(
                stringResource(Strings.string.settings_siren_label_tone),
                stringResource(Strings.string.settings_siren_label_melody),
            ),
            selectedIndex = if (value.type == SirenToneType.TONE) 0 else 1,
            onSelect = { index ->
                val type = if (index == 0) SirenToneType.TONE else SirenToneType.MELODY
                if (type != value.type) onChange(SirenTone(type, setting.range.first))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        CmpText(
            text = stringResource(Strings.string.settings_siren_label_number),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        IntRangeEditor(
            value = value.number,
            range = setting.range,
            units = setting.units,
            onChange = { onChange(value.copy(number = it)) },
        )
    }
}
