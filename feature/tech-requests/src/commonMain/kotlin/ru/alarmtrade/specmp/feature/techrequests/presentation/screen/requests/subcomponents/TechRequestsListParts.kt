package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_chat
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_connect
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_new
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_error_load_title
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_error_page
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_loading_more
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_empty_text
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_empty_title
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_no_device
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_title_no_device
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsTags
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.text

private val EmptyBadgeSize = 112.dp
private val EmptyIconSize = 64.dp
private val FooterHeight = 64.dp

/** Пусто: без блока вместо «Новая заявка» — пояснение и «Подключить блок»; чат — всегда альтернатива. */
@Composable
internal fun TechRequestsEmpty(
    canCreate: Boolean,
    onCreate: () -> Unit,
    onConnectDevice: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    Column(
        modifier = modifier.padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
    ) {
        Box(
            modifier = Modifier.size(EmptyBadgeSize).background(colors.brandPrimaryHighlight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.SupportAgent,
                contentDescription = null,
                modifier = Modifier.size(EmptyIconSize),
                tint = colors.brandPrimaryDefault,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.techrequests_list_msg_empty_title),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(
                    if (canCreate) {
                        Strings.string.techrequests_list_msg_empty_text
                    } else {
                        Strings.string.techrequests_list_msg_no_device
                    },
                ),
                style = CmpTextStyle.Paragraph1,
                color = colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            if (canCreate) {
                CmpButton(
                    text = stringResource(Strings.string.techrequests_list_btn_new),
                    onClick = onCreate,
                    icon = CmpIcons.Add,
                    modifier = Modifier.testTag(TechRequestsTags.CREATE),
                )
            } else {
                CmpButton(
                    text = stringResource(Strings.string.techrequests_list_btn_connect),
                    onClick = onConnectDevice,
                    icon = CmpIcons.Bluetooth,
                )
            }
            CmpButton(
                text = stringResource(Strings.string.techrequests_list_btn_chat),
                onClick = onOpenChat,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                icon = CmpIcons.Forum,
            )
        }
    }
}

@Composable
internal fun TechRequestsLoadError(error: TechRequestsError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val offline = error is TechRequestsError.Common && error.error == ApiError.NoConnection
    CmpStateBox(
        icon = if (offline) CmpIcons.WifiOff else CmpIcons.CloudOff,
        title = stringResource(Strings.string.techrequests_list_error_load_title),
        text = error.text(),
        tone = if (offline) CmpStateTone.Neutral else CmpStateTone.Error,
        modifier = modifier,
    ) {
        CmpButton(
            text = stringResource(Strings.string.techrequests_common_btn_retry),
            onClick = onRetry,
            icon = CmpIcons.Refresh,
            modifier = Modifier.testTag(TechRequestsTags.RETRY),
        )
    }
}

/** Без блока создать заявку нельзя: баннер над списком (вход с главного экрана). */
@Composable
internal fun NoDeviceBanner(onConnectDevice: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpIcon(icon = CmpIcons.Bluetooth, contentDescription = null, tint = colors.brandPrimaryDefault)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = stringResource(Strings.string.techrequests_list_title_no_device),
                    style = CmpTextStyle.Headline3,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
                CmpText(
                    text = stringResource(Strings.string.techrequests_list_msg_no_device),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        CmpButton(
            text = stringResource(Strings.string.techrequests_list_btn_connect),
            onClick = onConnectDevice,
            type = CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            modifier = Modifier.align(Alignment.End).testTag(TechRequestsTags.CONNECT),
        )
    }
}

@Composable
internal fun SectionHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Row(
        modifier = modifier.padding(start = CmpSpacing.space4, top = CmpSpacing.space12, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(text = label, style = CmpTextStyle.Headline3, color = colors.foregroundOnDarkSecondaryDefault)
        Box(
            modifier = Modifier
                .heightIn(min = CmpSpacing.space24)
                .widthIn(min = CmpSpacing.space24)
                .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall))
                .padding(horizontal = CmpSpacing.space8),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = count) { value ->
                CmpText(
                    text = value.toString(),
                    style = CmpTextStyle.Additional0,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
internal fun ListFooter(
    loadingMore: Boolean,
    moreError: TechRequestsError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    when {
        loadingMore -> Row(
            modifier = modifier.fillMaxWidth().heightIn(min = FooterHeight),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), strokeWidth = 3.dp)
            CmpText(
                text = stringResource(Strings.string.techrequests_list_loading_more),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }

        moreError != null -> CmpBanner(
            text = stringResource(Strings.string.techrequests_list_error_page),
            modifier = modifier,
            tone = CmpBannerTone.Error,
            icon = CmpIcons.ErrorOutline,
            actionLabel = stringResource(Strings.string.techrequests_common_btn_retry),
            onAction = onRetry,
            compact = true,
        )
    }
}
