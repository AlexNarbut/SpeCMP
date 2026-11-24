package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_a11y_locked
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold

private val BodyMaxWidth = 480.dp
private val CompactHero = 128.dp
private val WideHero = 220.dp

@Composable
internal fun FirmwareScreenFrame(
    title: String,
    subtitle: String?,
    locked: Boolean,
    onBack: () -> Unit,
    bodyAlignment: Alignment.Horizontal,
    hero: @Composable (size: Dp) -> Unit,
    body: @Composable ColumnScope.() -> Unit,
) {
    // Раскладка меняется с шириной окна: состояние героя и тела (прокрутка, анимации) переезжает вместе с ними.
    val currentHero by rememberUpdatedState(hero)
    val currentBody by rememberUpdatedState(body)
    val heroContent = remember { movableContentOf { size: Dp -> currentHero(size) } }
    val bodyContent = remember { movableContentWithReceiverOf<ColumnScope> { currentBody() } }
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = title,
                subtitle = subtitle,
                navigationIcon = {
                    if (locked) {
                        LockedBackIcon()
                    } else {
                        CmpIconButton(
                            onClick = onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.firmware_common_a11y_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (firmwareLayoutOf(maxWidth) != FirmwareLayout.Compact) {
                CmpRow(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    CmpBox(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        heroContent(WideHero)
                    }
                    CmpBox(
                        modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center,
                    ) {
                        FrameBody(
                            bodyAlignment,
                            Modifier.padding(
                                start = CmpSpacing.space8,
                                end = CmpSpacing.screenPaddingHMedium,
                                top = CmpSpacing.space24,
                                bottom = CmpSpacing.space24,
                            ),
                            bodyContent,
                        )
                    }
                }
            } else {
                CmpColumn(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CmpBox(modifier = Modifier.padding(top = CmpSpacing.space20, bottom = CmpSpacing.space4)) {
                        heroContent(CompactHero)
                    }
                    FrameBody(
                        bodyAlignment,
                        Modifier.padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space12),
                        bodyContent,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameBody(
    alignment: Alignment.Horizontal,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpColumn(
        modifier = modifier.widthIn(max = BodyMaxWidth).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        horizontalAlignment = alignment,
        content = content,
    )
}

@Composable
private fun LockedBackIcon() {
    CmpBox(modifier = Modifier.size(CmpSize.touchMin), contentAlignment = Alignment.Center) {
        CmpIcon(
            icon = CmpIcons.Lock,
            contentDescription = stringResource(Strings.string.firmware_update_a11y_locked),
            tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
}
