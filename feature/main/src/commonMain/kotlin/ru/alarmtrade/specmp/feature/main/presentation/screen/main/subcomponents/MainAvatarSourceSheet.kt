package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.files.ImageSource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_avatar_camera
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_btn_avatar_gallery
import ru.alarmtrade.specmp.core.localization.resources.main_drawer_title_avatar_source
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun MainAvatarSourceSheet(onSourceSelect: (ImageSource) -> Unit, onDismiss: () -> Unit) {
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.screenPaddingH,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(
                text = stringResource(Strings.string.main_drawer_title_avatar_source),
                style = CmpTextStyle.Headline1,
                modifier = Modifier.padding(bottom = CmpSpacing.space8),
            )
            CmpListItem(
                flat = true,
                icon = CmpIcons.PhotoCamera,
                title = stringResource(Strings.string.main_drawer_btn_avatar_camera),
                onClick = { onSourceSelect(ImageSource.Camera) },
                trailing = {},
                modifier = Modifier.testTag("main_avatar_source_camera"),
            )
            CmpListItem(
                flat = true,
                icon = CmpIcons.PhotoLibrary,
                title = stringResource(Strings.string.main_drawer_btn_avatar_gallery),
                onClick = { onSourceSelect(ImageSource.Gallery) },
                trailing = {},
                modifier = Modifier.testTag("main_avatar_source_gallery"),
            )
        }
    }
}
