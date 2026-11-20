package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_reread
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_optional
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_reread_offline
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags

private val LineMinHeight = 44.dp

/** Данные, заполненные сами: карточка с пометкой источника («Считано с блока», «Из мастера монтажа»…). */
@Composable
internal fun AutoCard(source: String?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space4)) {
            if (source != null) {
                Row(
                    modifier = Modifier.padding(top = CmpSpacing.space8, bottom = CmpSpacing.space4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpIcon(
                        icon = CmpIcons.Lock,
                        contentDescription = null,
                        tint = muted,
                        modifier = Modifier.size(CmpSize.iconS),
                    )
                    CmpText(
                        text = source,
                        style = CmpTextStyle.Additional1,
                        color = muted,
                        modifier = Modifier.padding(start = CmpSpacing.space4),
                    )
                }
            }
            content()
        }
    }
}

/** «Подпись над значением» внутри [AutoCard]. */
@Composable
internal fun KeyValue(label: String, value: String, last: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = CmpSpacing.space12)) {
        CmpText(
            text = label,
            style = CmpTextStyle.Additional0,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        CmpText(text = value, style = CmpTextStyle.Headline3, modifier = Modifier.padding(top = CmpSpacing.space4))
    }
    if (!last) CmpDivider()
}

/** Компактная строка «метка — значение» для длинных однотипных списков (оборудование). */
@Composable
internal fun Line(label: String, value: String, last: Boolean = false, missing: Boolean = false) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = LineMinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = label,
            style = CmpTextStyle.Paragraph2,
            color = muted,
            modifier = Modifier.padding(end = CmpSpacing.space12),
        )
        CmpText(
            text = value,
            style = if (missing) CmpTextStyle.Paragraph2 else CmpTextStyle.Headline3,
            color = if (missing) muted else CmpTheme.colors.foregroundOnDarkPrimaryDefault,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    if (!last) CmpDivider()
}

/** Ручное поле: пустое — не упрёк, подпись «Необязательно»; ошибка формата — вместо подписи. */
@Composable
internal fun OptionalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    hint: String? = null,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val support = error ?: hint ?: stringResource(Strings.string.mountpassport_editor_hint_optional)
    CmpTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = { CmpText(text = support, style = CmpTextStyle.Additional1) },
        modifier = modifier.fillMaxWidth(),
    )
}

/** «Не удалось прочитать …»: «Перечитать» — при подключённом том же блоке, иначе подсказка. */
@Composable
internal fun RereadRow(
    text: String,
    stage: PassportReadStage,
    rereadVisible: Boolean,
    onReread: (PassportReadStage) -> Unit,
    modifier: Modifier = Modifier,
    rereading: PassportReadStage? = null,
) {
    val colors = CmpTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (rereadVisible) colors.warningHighlight else colors.backgroundSurfaceHighlight,
                RoundedCornerShape(CmpRadius.cardSmall),
            )
            .padding(
                start = CmpSpacing.space12,
                end = CmpSpacing.space4,
                top = CmpSpacing.space8,
                bottom = CmpSpacing.space8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(
            icon = if (rereadVisible) CmpIcons.Warning else CmpIcons.BluetoothDisabled,
            contentDescription = null,
            tint = if (rereadVisible) colors.warningDefault else colors.foregroundOnDarkSecondaryDefault,
        )
        Column(modifier = Modifier.weight(1f)) {
            CmpText(text = text, style = CmpTextStyle.Paragraph2)
            if (!rereadVisible) {
                CmpText(
                    text = stringResource(Strings.string.mountpassport_editor_hint_reread_offline),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        if (rereadVisible) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_common_btn_reread),
                onClick = { onReread(stage) },
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                icon = CmpIcons.Refresh,
                uppercase = false,
                enabled = rereading == null,
                loading = rereading == stage,
                modifier = Modifier.testTag(MountPassportTags.reread(stage.name)),
            )
        }
    }
}

@Composable
internal fun Note(icon: CmpIcons, text: String, modifier: Modifier = Modifier) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        CmpIcon(icon = icon, contentDescription = null, tint = muted, modifier = Modifier.size(CmpSize.iconS))
        CmpText(
            text = text,
            style = CmpTextStyle.Paragraph3,
            color = muted,
            modifier = Modifier.weight(1f).padding(start = CmpSpacing.space8),
        )
    }
}
