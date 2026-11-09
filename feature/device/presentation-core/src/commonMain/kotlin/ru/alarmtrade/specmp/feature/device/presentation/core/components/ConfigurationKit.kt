package ru.alarmtrade.specmp.feature.device.presentation.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration

@Composable
fun ModelBadge(deviceName: String, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpText(
        text = deviceName,
        style = CmpTextStyle.Body,
        fontWeight = FontWeight.SemiBold,
        color = colors.onSurface,
        maxLines = 1,
        modifier = modifier
            .background(colors.surfaceContainerHighest, BadgeShape)
            .border(BorderStroke(1.dp, colors.outlineVariant), BadgeShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun ReadOnlyBadge(modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier
            .border(BorderStroke(1.dp, colors.outline), CircleShape)
            .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpIcon(
            icon = CmpIcons.Visibility,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        CmpText(
            text = stringResource(Strings.string.settings_common_value_source_read_only),
            style = CmpTextStyle.Body,
            color = colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
fun SourceTag(icon: CmpIcons, text: String, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier
            .background(colors.secondaryContainer, BadgeShape)
            .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(18.dp),
        )
        CmpText(text = text, style = CmpTextStyle.Body, color = colors.onSecondaryContainer, maxLines = 1)
    }
}

@Composable
fun ConfigurationOptionRow(
    icon: CmpIcons,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(24.dp))
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = title, style = CmpTextStyle.BodyLarge, color = colors.onSurface)
            subtitle?.let {
                CmpText(text = it, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ConfigurationOptionsPanel(
    title: String,
    wide: Boolean,
    onDismiss: () -> Unit,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = cmpThemeColors()
    val body: @Composable ColumnScope.() -> Unit = {
        content()
        footer?.let {
            CmpText(
                text = it,
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
    if (wide) {
        CmpDialog(
            onDismissRequest = onDismiss,
            title = { CmpText(text = title) },
            text = { CmpColumn(modifier = Modifier.fillMaxWidth(), content = body) },
            confirmButton = {
                CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
            },
        )
    } else {
        CmpBottomSheet(onDismissRequest = onDismiss) {
            CmpColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                CmpText(
                    text = title,
                    style = CmpTextStyle.Title,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
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
    val colors = cmpThemeColors()
    val nameBlank = name.isBlank()
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = title) },
        text = {
            CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                deviceName?.let {
                    CmpText(
                        text = stringResource(Strings.string.settings_form_label_model, it),
                        style = CmpTextStyle.Body,
                        color = colors.onSurfaceVariant,
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
                            style = CmpTextStyle.Caption,
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
                            style = CmpTextStyle.Caption,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                        .testTag(ConfigurationTestTags.FORM_DESCRIPTION),
                )
            }
        },
        confirmButton = {
            CmpButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = !nameBlank,
                modifier = Modifier.testTag(ConfigurationTestTags.FORM_SAVE),
            )
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Strings.string.settings_common_btn_cancel), onClick = onDismiss)
        },
    )
}

object ConfigurationTestTags {
    const val FORM_NAME = "configuration_form_name"
    const val FORM_DESCRIPTION = "configuration_form_description"
    const val FORM_SAVE = "configuration_form_save"
}

private val BadgeShape = RoundedCornerShape(8.dp)
private const val DESCRIPTION_LINES = 5
