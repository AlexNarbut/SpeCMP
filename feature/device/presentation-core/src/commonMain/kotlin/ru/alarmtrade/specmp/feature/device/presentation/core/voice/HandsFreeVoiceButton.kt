package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_a11y_novoice
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_a11y_off
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_a11y_on
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_a11y_speaking
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_btn_install
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_btn_later
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_btn_tip_ok
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_label_macos
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_label_windows
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_novoice_android
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_novoice_desktop
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_novoice_ios
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_path_ios
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_path_macos
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_path_windows
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_msg_tip
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_title_novoice
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_voice_title_tip
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

const val HANDS_FREE_VOICE_TAG = "handsfree_voice"
const val HANDS_FREE_TIP_OK_TAG = "handsfree_voice_tip_ok"
const val HANDS_FREE_NO_VOICE_TAG = "handsfree_novoice"

private val txt = Strings.string

/** Состояние кнопки «Озвучка» общей VM записи навигации (в мастере — одна на все шаги). */
@Composable
fun rememberHandsFreeVoice(): HandsFreeVoice {
    val viewModel = koinViewModel<HandsFreeVoiceViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        HandsFreeVoiceActions(
            onToggle = viewModel::onToggle,
            onTipDismiss = viewModel::onTipDismiss,
            onNoVoiceDismiss = viewModel::onNoVoiceDismiss,
            onInstallVoice = viewModel::onInstallVoice,
        )
    }
    return HandsFreeVoice(state, actions)
}

/** Кнопка «Озвучка» для `actions` шапки; синтеза речи нет — ничего не рисует. */
@Composable
fun HandsFreeVoiceButton(
    voice: HandsFreeVoice,
    modifier: Modifier = Modifier,
    help: VoiceSettingsHelp = platformVoiceSettingsHelp,
) {
    val state = voice.state
    val mode = state.mode ?: return
    CmpBox(modifier = modifier) {
        VoiceCircle(mode, voice.actions.onToggle)
        if (state.tip) VoiceTip(voice.actions.onTipDismiss)
    }
    if (state.noVoiceHelp) {
        NoVoiceHelp(
            help = help,
            canInstall = state.canInstallVoice,
            onInstall = voice.actions.onInstallVoice,
            onDismiss = voice.actions.onNoVoiceDismiss,
        )
    }
}

@Composable
private fun VoiceCircle(mode: VoiceButtonMode, onClick: () -> Unit) {
    val colors = CmpTheme.colors
    val on = mode == VoiceButtonMode.ON || mode == VoiceButtonMode.SPEAKING
    val surface = if (on) {
        Modifier.background(colors.brandPrimaryDefault, CircleShape)
    } else {
        Modifier.border(OutlineWidth, colors.borderTertiary, CircleShape)
    }
    val tint = when {
        on -> colors.onBrand
        mode == VoiceButtonMode.NO_VOICE -> colors.foregroundOnDarkSecondaryDefault
        else -> colors.foregroundOnDarkPrimaryDefault
    }
    val icon = when (mode) {
        VoiceButtonMode.ON -> CmpIcons.VolumeUp
        VoiceButtonMode.SPEAKING -> CmpIcons.GraphicEq
        VoiceButtonMode.OFF, VoiceButtonMode.NO_VOICE -> CmpIcons.VolumeOff
    }
    val pulse = if (mode == VoiceButtonMode.SPEAKING) Modifier.speakingPulse(colors.brandPrimaryDefault) else Modifier
    CmpBox(
        modifier = Modifier
            .testTag(HANDS_FREE_VOICE_TAG)
            .then(pulse)
            .size(CmpSize.touchMin)
            .cmpPressable(onClick = onClick, shape = CircleShape)
            .then(surface),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(icon = icon, contentDescription = stringResource(mode.a11y()), tint = tint)
        if (mode == VoiceButtonMode.NO_VOICE) {
            CmpBox(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = DotInset, end = DotInset)
                    .size(DotSize)
                    .border(DotRing, colors.backgroundPagePrimary, CircleShape)
                    .padding(DotRing)
                    .background(colors.errorDefault, CircleShape),
            )
        }
    }
}

private fun VoiceButtonMode.a11y(): StringResource = when (this) {
    VoiceButtonMode.OFF -> txt.deviceinteraction_voice_a11y_off
    VoiceButtonMode.ON -> txt.deviceinteraction_voice_a11y_on
    VoiceButtonMode.SPEAKING -> txt.deviceinteraction_voice_a11y_speaking
    VoiceButtonMode.NO_VOICE -> txt.deviceinteraction_voice_a11y_novoice
}

// Кольцо расходится от кнопки и гаснет за 70% цикла, остаток — пауза (как hfPulse в макете). «Убрать анимации»
// в системе Compose учитывает сам (масштаб длительности).
@Composable
private fun Modifier.speakingPulse(color: Color): Modifier {
    val progress by rememberInfiniteTransition(label = "voicePulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "voicePulseProgress",
    )
    return drawBehind {
        val phase = (progress / PULSE_ACTIVE).coerceAtMost(1f)
        if (phase < 1f) {
            drawCircle(
                color = color.copy(alpha = PULSE_ALPHA * (1f - phase)),
                radius = size.minDimension / 2 + PulseSpread.toPx() * phase,
            )
        }
    }
}

@Composable
private fun VoiceTip(onDismiss: () -> Unit) {
    val colors = CmpTheme.colors
    val ink = colors.foregroundOnLightPrimaryDefault
    val offset = with(LocalDensity.current) { IntOffset(0, (CmpSize.touchMin + TipGap).roundToPx()) }
    Popup(alignment = Alignment.TopEnd, offset = offset, properties = PopupProperties(focusable = false)) {
        CmpColumn(
            modifier = Modifier
                .width(TipWidth)
                .background(colors.backgroundSurfacePrimary, RoundedCornerShape(CmpRadius.cardMedium))
                .padding(start = CmpSpacing.space16, end = CmpSpacing.space12, top = CmpSpacing.space12)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpIcon(icon = CmpIcons.VolumeUp, contentDescription = null, tint = ink)
                CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                    CmpText(
                        text = stringResource(txt.deviceinteraction_voice_title_tip),
                        style = CmpTextStyle.Headline3,
                        color = ink,
                    )
                    CmpText(
                        text = stringResource(txt.deviceinteraction_voice_msg_tip),
                        style = CmpTextStyle.Paragraph2,
                        color = ink,
                    )
                }
            }
            CmpBox(
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = CmpSize.touchMin)
                    .testTag(HANDS_FREE_TIP_OK_TAG)
                    .cmpPressable(onClick = onDismiss, shape = RoundedCornerShape(CmpRadius.round))
                    .padding(horizontal = CmpSpacing.space16),
                contentAlignment = Alignment.Center,
            ) {
                CmpText(
                    text = stringResource(txt.deviceinteraction_voice_btn_tip_ok),
                    style = CmpTextStyle.Headline3,
                    color = ink,
                )
            }
        }
    }
}

/** Телефон — нижний лист, широкое окно (≥ Medium) — диалог. */
@Composable
private fun NoVoiceHelp(help: VoiceSettingsHelp, canInstall: Boolean, onInstall: () -> Unit, onDismiss: () -> Unit) {
    val window = LocalWindowInfo.current.containerSize.width
    val wide = with(LocalDensity.current) { window.toDp() } >= CmpBreakpoints.Medium
    val install = help == VoiceSettingsHelp.ANDROID && canInstall
    if (wide) {
        CmpDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag(HANDS_FREE_NO_VOICE_TAG),
            title = { NoVoiceTitle() },
            text = { CmpColumn(content = { NoVoiceBody(help) }) },
            confirmButton = {
                if (install) {
                    CmpButton(text = stringResource(txt.deviceinteraction_voice_btn_install), onClick = onInstall)
                } else {
                    CmpButton(text = stringResource(txt.deviceinteraction_voice_btn_ok), onClick = onDismiss)
                }
            },
            dismissButton = if (install) {
                {
                    CmpButton(
                        text = stringResource(txt.deviceinteraction_voice_btn_later),
                        onClick = onDismiss,
                        type = CmpButtonType.Tertiary,
                    )
                }
            } else {
                null
            },
        )
    } else {
        CmpBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag(HANDS_FREE_NO_VOICE_TAG)) {
            CmpColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = CmpSpacing.space20, end = CmpSpacing.space20, bottom = CmpSpacing.space20),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                NoVoiceTitle()
                NoVoiceBody(help)
                if (install) {
                    CmpButton(
                        text = stringResource(txt.deviceinteraction_voice_btn_install),
                        onClick = onInstall,
                        icon = CmpIcons.Settings,
                        fullWidth = true,
                    )
                    CmpButton(
                        text = stringResource(txt.deviceinteraction_voice_btn_later),
                        onClick = onDismiss,
                        type = CmpButtonType.Tertiary,
                        fullWidth = true,
                    )
                } else {
                    CmpButton(
                        text = stringResource(txt.deviceinteraction_voice_btn_ok),
                        onClick = onDismiss,
                        fullWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun NoVoiceTitle() {
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpBox(
            modifier = Modifier.size(CmpSize.iconXxl).background(colors.warningHighlight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = CmpIcons.VoiceOverOff, contentDescription = null, tint = colors.warningDefault)
        }
        CmpText(
            text = stringResource(txt.deviceinteraction_voice_title_novoice),
            style = CmpTextStyle.Headline1,
            color = colors.foregroundOnDarkPrimaryDefault,
        )
    }
}

@Composable
private fun ColumnScope.NoVoiceBody(help: VoiceSettingsHelp) {
    val colors = CmpTheme.colors
    val text = when (help) {
        VoiceSettingsHelp.ANDROID -> txt.deviceinteraction_voice_msg_novoice_android
        VoiceSettingsHelp.IOS -> txt.deviceinteraction_voice_msg_novoice_ios
        VoiceSettingsHelp.DESKTOP -> txt.deviceinteraction_voice_msg_novoice_desktop
    }
    CmpText(
        text = stringResource(text),
        style = CmpTextStyle.Paragraph1,
        color = colors.foregroundOnDarkSecondaryDefault,
    )
    when (help) {
        VoiceSettingsHelp.ANDROID -> Unit

        VoiceSettingsHelp.IOS -> SettingsPath(label = null, path = txt.deviceinteraction_voice_msg_path_ios)

        VoiceSettingsHelp.DESKTOP -> {
            SettingsPath(txt.deviceinteraction_voice_label_windows, txt.deviceinteraction_voice_msg_path_windows)
            SettingsPath(txt.deviceinteraction_voice_label_macos, txt.deviceinteraction_voice_msg_path_macos)
        }
    }
}

@Composable
private fun SettingsPath(label: StringResource?, path: StringResource) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = CmpSpacing.space8)
            .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall))
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        label?.let {
            CmpText(
                text = stringResource(it),
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        CmpText(
            text = stringResource(path),
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkPrimaryDefault,
        )
    }
}

private val OutlineWidth = 2.dp
private val DotSize = 12.dp
private val DotRing = 2.dp
private val DotInset = 4.dp
private val TipWidth = 288.dp
private val TipGap = 10.dp
private val PulseSpread = 10.dp
private const val PULSE_MS = 1600
private const val PULSE_ACTIVE = 0.7f
private const val PULSE_ALPHA = 0.5f
