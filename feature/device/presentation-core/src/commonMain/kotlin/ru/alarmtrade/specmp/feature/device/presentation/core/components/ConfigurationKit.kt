package ru.alarmtrade.specmp.feature.device.presentation.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_read_only
import ru.alarmtrade.specmp.core.localization.resources.settings_form_btn_save
import ru.alarmtrade.specmp.core.localization.resources.settings_form_error_name
import ru.alarmtrade.specmp.core.localization.resources.settings_form_format_counter
import ru.alarmtrade.specmp.core.localization.resources.settings_form_label_description
import ru.alarmtrade.specmp.core.localization.resources.settings_form_label_model
import ru.alarmtrade.specmp.core.localization.resources.settings_form_label_name
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration

@Composable
fun ModelBadge(deviceName: String, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.tags)
    CmpText(
        text = deviceName,
        style = CmpTextStyle.Headline3,
        color = colors.foregroundOnDarkPrimaryDefault,
        maxLines = 1,
        modifier = modifier
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.tags, colors.borderHighlight, shape)
            .padding(horizontal = BadgePaddingH, vertical = CmpSpacing.space4),
    )
}

@Composable
fun ReadOnlyBadge(modifier: Modifier = Modifier) {
    CmpTag(
        text = stringResource(Strings.string.settings_common_value_source_read_only),
        tone = CmpTagTone.Outline,
        icon = CmpIcons.Visibility,
        modifier = modifier,
    )
}

@Composable
fun SourceTag(icon: CmpIcons, text: String, modifier: Modifier = Modifier) {
    CmpTag(text = text, tone = CmpTagTone.Neutral, icon = icon, modifier = modifier)
}

@Composable
fun ConfigurationOptionRow(
    icon: CmpIcons,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpListItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        flat = true,
        trailing = {},
        modifier = modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space8),
    )
}

@Composable
fun ConfigurationOptionsPanel(
    title: String,
    wide: Boolean,
    onDismiss: () -> Unit,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    val body: @Composable ColumnScope.() -> Unit = {
        content()
        footer?.let {
            CmpText(
                text = it,
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space12),
            )
        }
    }
    if (wide) {
        CmpDialog(
            onDismissRequest = onDismiss,
            title = { CmpText(text = title, style = CmpTextStyle.Headline1) },
            text = { CmpColumn(modifier = Modifier.fillMaxWidth(), content = body) },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.settings_common_btn_cancel),
                    onClick = onDismiss,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            },
        )
    } else {
        CmpBottomSheet(onDismissRequest = onDismiss) {
            CmpColumn(modifier = Modifier.fillMaxWidth().padding(bottom = CmpSpacing.space16)) {
                CmpText(
                    text = title,
                    style = CmpTextStyle.Headline1,
                    color = colors.foregroundOnDarkPrimaryDefault,
                    modifier = Modifier.padding(
                        start = CmpSpacing.screenPaddingH,
                        end = CmpSpacing.screenPaddingH,
                        bottom = CmpSpacing.space8,
                    ),
                )
                body()
            }
        }
    }
}

@Composable
fun ConfigurationInfoDialog(
    title: String,
    name: String,
    description: String,
    deviceName: String?,
    onChange: (name: String, description: String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(Strings.string.settings_form_btn_save),
) {
    val colors = CmpTheme.colors
    val nameBlank = name.isBlank()
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = title, style = CmpTextStyle.Headline1) },
        text = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                deviceName?.let {
                    CmpText(
                        text = stringResource(Strings.string.settings_form_label_model, it),
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                CmpTextField(
                    value = name,
                    onValueChange = { onChange(it.take(LocalConfiguration.NAME_MAX_LENGTH), description) },
                    label = stringResource(Strings.string.settings_form_label_name),
                    singleLine = true,
                    isError = nameBlank,
                    supportingText = {
                        CmpText(
                            text = if (nameBlank) {
                                stringResource(Strings.string.settings_form_error_name)
                            } else {
                                stringResource(
                                    Strings.string.settings_form_format_counter,
                                    name.length,
                                    LocalConfiguration.NAME_MAX_LENGTH,
                                )
                            },
                            style = CmpTextStyle.Paragraph3,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag(ConfigurationTestTags.FORM_NAME),
                )
                CmpTextField(
                    value = description,
                    onValueChange = { onChange(name, it.take(LocalConfiguration.DESCRIPTION_MAX_LENGTH)) },
                    label = stringResource(Strings.string.settings_form_label_description),
                    maxLines = DESCRIPTION_LINES,
                    supportingText = {
                        CmpText(
                            text = stringResource(
                                Strings.string.settings_form_format_counter,
                                description.length,
                                LocalConfiguration.DESCRIPTION_MAX_LENGTH,
                            ),
                            style = CmpTextStyle.Paragraph3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = DescriptionMinHeight)
                        .testTag(ConfigurationTestTags.FORM_DESCRIPTION),
                )
            }
        },
        confirmButton = {
            CmpButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = !nameBlank,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(ConfigurationTestTags.FORM_SAVE),
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.settings_common_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
    )
}

object ConfigurationTestTags {
    const val FORM_NAME = "configuration_form_name"
    const val FORM_DESCRIPTION = "configuration_form_description"
    const val FORM_SAVE = "configuration_form_save"
}

private val BadgePaddingH = 10.dp
private val DescriptionMinHeight = 120.dp
private const val DESCRIPTION_LINES = 5
