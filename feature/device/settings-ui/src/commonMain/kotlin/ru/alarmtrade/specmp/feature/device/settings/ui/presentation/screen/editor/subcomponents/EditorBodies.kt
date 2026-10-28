package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_hours_down
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_hours_up
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_less
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_minutes_down
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_minutes_up
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_more
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_a11y_type_value
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_error_invalid
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_error_range
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_label_counter
import ru.alarmtrade.specmp.core.localization.resources.settings_edit_label_value
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpStepButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpSlider
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextInputType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.decimalNumber
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.decimalText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.edgeText
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.matchesPattern
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.withUnits

/** Больше делений — ползунок без отметок (значение всё равно округляется к шагу). */
private const val MAX_SLIDER_STEPS = 20
private const val HOURS_IN_DAY = 24
private const val MINUTES_IN_HOUR = 60
private const val TIME_DIGITS = 2
private const val OPTIONS_SCROLL_CONTEXT = 2
private val BigNumberSize = 48.sp
private val UnitsSize = 22.sp
private val OptionsMaxHeight = 360.dp

@Composable
internal fun IntegerEditor(setting: IntegerSetting, value: Int, onChange: (Int) -> Unit) {
    IntRangeEditor(
        value = value,
        range = setting.range,
        units = setting.units,
        minValueText = setting.minValueText,
        maxValueText = setting.maxValueText,
        onChange = onChange
    )
}

/** Целое число в диапазоне: −/+ с автоповтором, ввод с клавиатуры, ползунок, подписи краёв. */
@Composable
internal fun IntRangeEditor(
    value: Int,
    range: IntRange,
    units: String?,
    onChange: (Int) -> Unit,
    minValueText: String? = null,
    maxValueText: String? = null
) {
    val edgeLabel = edgeText(value, range.first, range.last, minValueText, maxValueText)
    NumberEditor(
        bigText = edgeLabel ?: value.toString(),
        units = units.takeIf { edgeLabel == null },
        typedInitial = value.toString(),
        parse = { text -> text.trim().toIntOrNull()?.takeIf { it in range } },
        onType = onChange,
        rangeError = stringResource(Strings.string.settings_edit_error_range, range.first, range.last),
        onStep = { delta -> onChange((value + delta).coerceIn(range)) },
        slider = SliderSpec(
            value = value.toFloat(),
            range = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).takeIf { it in 0..MAX_SLIDER_STEPS } ?: 0,
            onChange = { onChange(it.roundToInt().coerceIn(range)) }
        ),
        minLabel = minValueText ?: withUnits(range.first.toString(), units),
        maxLabel = maxValueText ?: withUnits(range.last.toString(), units)
    )
}

@Composable
internal fun DecimalEditor(setting: DecimalSetting, value: Float, onChange: (Float) -> Unit) {
    val snap = { raw: Double ->
        ((raw / setting.step).roundToLong() * setting.step.toDouble()).toFloat().coerceIn(
            setting.minValue,
            setting.maxValue
        )
    }
    val edgeLabel = edgeText(value, setting.minValue, setting.maxValue, setting.minValueText, setting.maxValueText)
    val stepsCount = ((setting.maxValue - setting.minValue) / setting.step).roundToInt() - 1
    NumberEditor(
        bigText = edgeLabel ?: decimalNumber(setting, value),
        units = setting.units.takeIf { edgeLabel == null },
        typedInitial = decimalNumber(setting, value),
        parse = { text ->
            text.trim().replace(',', '.').replace('−', '-').toFloatOrNull()
                ?.takeIf { it in setting.minValue..setting.maxValue }
        },
        onType = onChange,
        rangeError = stringResource(
            Strings.string.settings_edit_error_range,
            decimalNumber(setting, setting.minValue),
            decimalNumber(setting, setting.maxValue)
        ),
        onStep = { delta -> onChange(snap(value.toDouble() + delta * setting.step)) },
        slider = SliderSpec(
            value = value,
            range = setting.minValue..setting.maxValue,
            steps = stepsCount.takeIf { it in 0..MAX_SLIDER_STEPS } ?: 0,
            onChange = { onChange(snap(it.toDouble())) }
        ),
        minLabel = decimalText(setting, setting.minValue),
        maxLabel = decimalText(setting, setting.maxValue)
    )
}

private class SliderSpec(
    val value: Float,
    val range: ClosedFloatingPointRange<Float>,
    val steps: Int,
    val onChange: (Float) -> Unit
)

/** Крупное значение между кнопками −/+ (автоповтор), ввод с клавиатуры по нажатию на число, ползунок, края. */
@Suppress("LongParameterList")
@Composable
private fun <T> NumberEditor(
    bigText: String,
    units: String?,
    typedInitial: String,
    parse: (String) -> T?,
    onType: (T) -> Unit,
    rangeError: String,
    onStep: (delta: Int) -> Unit,
    slider: SliderSpec,
    minLabel: String,
    maxLabel: String
) {
    val colors = cmpThemeColors()
    var typing by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    var typedInvalid by remember { mutableStateOf(false) }

    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CmpStepButton(
                icon = CmpIcons.Remove,
                contentDescription = stringResource(Strings.string.settings_edit_a11y_less),
                onStep = {
                    typing = false
                    onStep(-1)
                }
            )
            CmpColumn(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (typing) {
                    CmpTextField(
                        value = typed,
                        onValueChange = { text ->
                            typed = text
                            val parsed = parse(text)
                            typedInvalid = parsed == null
                            if (parsed != null) onType(parsed)
                        },
                        singleLine = true,
                        isError = typedInvalid,
                        supportingText = if (typedInvalid) ({ CmpText(text = rangeError) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                } else {
                    val typeLabel = stringResource(Strings.string.settings_edit_a11y_type_value)
                    CmpRow(
                        modifier = Modifier
                            .heightIn(min = 64.dp)
                            .clickable(role = Role.Button, onClickLabel = typeLabel) {
                                typed = typedInitial
                                typedInvalid = false
                                typing = true
                            }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CmpText(
                            text = bigText,
                            fontSize = BigNumberSize,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        if (units != null) {
                            CmpText(
                                text = " $units",
                                fontSize = UnitsSize,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            CmpStepButton(
                icon = CmpIcons.Add,
                contentDescription = stringResource(Strings.string.settings_edit_a11y_more),
                onStep = {
                    typing = false
                    onStep(1)
                }
            )
        }
        CmpSlider(
            value = slider.value.coerceIn(slider.range),
            onValueChange = {
                typing = false
                slider.onChange(it)
            },
            valueRange = slider.range,
            steps = slider.steps
        )
        CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CmpText(text = minLabel, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
            CmpText(text = maxLabel, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
        }
    }
}

/** Список вариантов с радиокнопками; длинные названия переносятся, текущий вариант виден сразу. */
@Composable
internal fun OptionEditor(setting: OptionSetting, selected: Int, onSelect: (Int) -> Unit) {
    val colors = cmpThemeColors()
    val listState =
        rememberLazyListState(initialFirstVisibleItemIndex = (selected - OPTIONS_SCROLL_CONTEXT).coerceAtLeast(0))
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().heightIn(max = OptionsMaxHeight)) {
        itemsIndexed(setting.options) { index, option ->
            val isSelected = index == selected
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(
                        if (isSelected) colors.secondaryContainer else Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(role = Role.RadioButton) { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CmpRadioButton(selected = isSelected, onClick = null)
                CmpText(
                    text = option,
                    style = CmpTextStyle.BodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else null,
                    color = if (isSelected) colors.onSecondaryContainer else colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Поле ввода с клавиатурой по типу устройства, счётчиком символов и проверкой шаблоном устройства. */
@Composable
internal fun TextEditor(setting: TextSetting, value: String, onChange: (String) -> Unit) {
    val valid = setting.matchesPattern(value)
    CmpTextField(
        value = value,
        onValueChange = { onChange(it.take(setting.maxLength)) },
        label = stringResource(Strings.string.settings_edit_label_value),
        singleLine = true,
        isError = !valid,
        supportingText = {
            CmpText(
                text = if (valid) {
                    stringResource(Strings.string.settings_edit_label_counter, value.length, setting.maxLength)
                } else {
                    setting.validationErrorText ?: stringResource(Strings.string.settings_edit_error_invalid)
                }
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = when (setting.inputType) {
                TextInputType.TEXT -> KeyboardType.Text
                TextInputType.NUMBER -> KeyboardType.Number
                TextInputType.PHONE -> KeyboardType.Phone
            }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Часы и минуты крупными кнопками ▲/▼ (по кругу, с автоповтором). */
@Composable
internal fun TimeEditor(value: TimeOfDay, onChange: (TimeOfDay) -> Unit) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeUnit(
            value = value.hours,
            upLabel = stringResource(Strings.string.settings_edit_a11y_hours_up),
            downLabel = stringResource(Strings.string.settings_edit_a11y_hours_down),
            onStep = { delta -> onChange(value.copy(hours = (value.hours + delta).mod(HOURS_IN_DAY))) }
        )
        CmpText(
            text = ":",
            fontSize = BigNumberSize,
            fontWeight = FontWeight.SemiBold,
            color = cmpThemeColors().onSurface
        )
        TimeUnit(
            value = value.minutes,
            upLabel = stringResource(Strings.string.settings_edit_a11y_minutes_up),
            downLabel = stringResource(Strings.string.settings_edit_a11y_minutes_down),
            onStep = { delta -> onChange(value.copy(minutes = (value.minutes + delta).mod(MINUTES_IN_HOUR))) }
        )
    }
}

@Composable
private fun TimeUnit(value: Int, upLabel: String, downLabel: String, onStep: (Int) -> Unit) {
    CmpColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpStepButton(icon = CmpIcons.KeyboardArrowUp, contentDescription = upLabel, onStep = {
            onStep(1)
        }, size = 56.dp)
        CmpText(
            text = value.toString().padStart(TIME_DIGITS, '0'),
            fontSize = BigNumberSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = cmpThemeColors().onSurface,
            modifier = Modifier.widthIn(min = 84.dp)
        )
        CmpStepButton(
            icon = CmpIcons.KeyboardArrowDown,
            contentDescription = downLabel,
            onStep = { onStep(-1) },
            size = 56.dp
        )
    }
}
