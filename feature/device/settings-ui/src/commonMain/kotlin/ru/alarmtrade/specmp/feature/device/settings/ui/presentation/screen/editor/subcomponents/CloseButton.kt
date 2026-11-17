package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_close
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags

@Composable
internal fun CloseButton(onClose: () -> Unit) {
    CmpIconButton(
        onClick = onClose,
        icon = CmpIcons.Close,
        contentDescription = stringResource(Strings.string.settings_common_a11y_close),
        tint = cmpThemeColors().onSurfaceVariant,
        modifier = Modifier.testTag(SettingsTestTags.DIALOG_CLOSE),
    )
}

@Composable
internal fun DialogTitle(text: String, onClose: () -> Unit) {
    CmpRow(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(text = text, style = CmpTextStyle.Paragraph2, modifier = Modifier.weight(1f))
        CloseButton(onClose)
    }
}
