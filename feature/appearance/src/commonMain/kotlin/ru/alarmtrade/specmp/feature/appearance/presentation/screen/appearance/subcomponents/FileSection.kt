package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_btn_export
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_btn_import
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_hint
import ru.alarmtrade.specmp.core.localization.resources.appearance_file_title
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

@Composable
internal fun FileSection(onExport: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    SectionCard(title = stringResource(Strings.string.appearance_file_title), modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpButton(
                text = stringResource(Strings.string.appearance_file_btn_export),
                onClick = onExport,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Download,
                modifier = Modifier.weight(1f).testTag("appearance_export"),
            )
            CmpButton(
                text = stringResource(Strings.string.appearance_file_btn_import),
                onClick = onImport,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Upload,
                modifier = Modifier.weight(1f).testTag("appearance_import"),
            )
        }
        Hint(stringResource(Strings.string.appearance_file_hint))
    }
}
