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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
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

private const val DISABLED_ALPHA = 0.38f

@Composable
internal fun ScheduleEditor(value: AutoStartSchedule, onChange: (AutoStartSchedule) -> Unit) {
    var openSlot by rememberSaveable { mutableStateOf<Int?>(null) }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    val colors = cmpThemeColors()
    CmpSurface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 8.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpBox(modifier = Modifier.size(width = 64.dp, height = 48.dp), contentAlignment = Alignment.Center) {
                    CmpSwitch(checked = enabled, onCheckedChange = onEnabledChange)
                }
                CmpText(text = label, style = CmpTextStyle.Headline1, modifier = Modifier.weight(1f))
                CmpSurface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (open) colors.secondaryContainer else colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.outline),
                    modifier = Modifier
                        .widthIn(min = 96.dp)
                        .heightIn(min = 48.dp)
                        .clickable(enabled = enabled, role = Role.Button, onClick = onToggleOpen),
                ) {
                    CmpBox(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        CmpText(
                            text = time.text(),
                            style = CmpTextStyle.Headline2,
                            textAlign = TextAlign.Center,
                            color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = DISABLED_ALPHA),
                            modifier = Modifier.padding(vertical = 10.dp),
                        )
                    }
                }
            }
            if (open && enabled) {
                CmpDivider(color = colors.outlineVariant)
                TimeEditor(time, onTimeChange)
            }
        }
    }
}

@Composable
internal fun SirenEditor(setting: SirenSetting, value: SirenTone, onChange: (SirenTone) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            color = cmpThemeColors().onSurfaceVariant,
        )
        IntRangeEditor(
            value = value.number,
            range = setting.range,
            units = setting.units,
            onChange = { onChange(value.copy(number = it)) },
        )
    }
}
