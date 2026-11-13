package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.text

/** Пусто: без блока вместо «Новая заявка» — пояснение и «Подключить блок»; чат — всегда альтернатива. */
@Composable
internal fun TechRequestsEmpty(
    canCreate: Boolean,
    onCreate: () -> Unit,
    onConnectDevice: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier.size(112.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.SupportAgent,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colors.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpText(
                text = stringResource(Strings.string.techrequests_list_msg_empty_title),
                style = CmpTextStyle.Title,
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
                style = CmpTextStyle.BodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (canCreate) {
                CmpButton(
                    onClick = onCreate,
                    modifier = Modifier.heightIn(min = 56.dp).testTag("techrequests_create"),
                ) {
                    CmpIcon(icon = CmpIcons.Add, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.techrequests_list_btn_new),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else {
                CmpButton(onClick = onConnectDevice, modifier = Modifier.heightIn(min = 56.dp)) {
                    CmpIcon(icon = CmpIcons.Bluetooth, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.techrequests_list_btn_connect),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            CmpTextButton(onClick = onOpenChat, modifier = Modifier.heightIn(min = 48.dp)) {
                CmpIcon(icon = CmpIcons.Forum, contentDescription = null)
                CmpText(
                    text = stringResource(Strings.string.techrequests_list_btn_chat),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
internal fun TechRequestsLoadError(error: TechRequestsError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CmpStateBox(
        icon = CmpIcons.CloudOff,
        title = stringResource(Strings.string.techrequests_list_error_load_title),
        text = error.text(),
        tone = CmpStateTone.Error,
        modifier = modifier,
    ) {
        CmpButton(onClick = onRetry, modifier = Modifier.heightIn(min = 56.dp).testTag("techrequests_error_retry")) {
            CmpIcon(icon = CmpIcons.Refresh, contentDescription = null)
            CmpText(
                text = stringResource(Strings.string.techrequests_common_btn_retry),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Без блока создать заявку нельзя: баннер над списком (вход с главного экрана). */
@Composable
internal fun NoDeviceBanner(onConnectDevice: () -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CmpIcon(icon = CmpIcons.Bluetooth, contentDescription = null, tint = colors.onSecondaryContainer)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = stringResource(Strings.string.techrequests_list_title_no_device),
                    color = colors.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                CmpText(
                    text = stringResource(Strings.string.techrequests_list_msg_no_device),
                    color = colors.onSecondaryContainer,
                )
            }
        }
        CmpTextButton(
            onClick = onConnectDevice,
            modifier = Modifier.align(Alignment.End).heightIn(min = 48.dp).testTag("techrequests_connect"),
        ) {
            CmpText(
                text = stringResource(Strings.string.techrequests_list_btn_connect),
                color = colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
internal fun SectionHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    Row(
        modifier = modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpText(text = label, color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .widthIn(min = 24.dp)
                .background(colors.surfaceContainerHighest, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = count) { value ->
                CmpText(
                    text = value.toString(),
                    style = CmpTextStyle.Caption,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
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
    val colors = cmpThemeColors()
    when {
        loadingMore -> Row(
            modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            CmpText(
                text = stringResource(Strings.string.techrequests_list_loading_more),
                color = colors.onSurfaceVariant,
            )
        }

        moreError != null -> Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(colors.errorContainer, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = CmpIcons.ErrorOutline, contentDescription = null, tint = colors.onErrorContainer)
            CmpText(
                text = stringResource(Strings.string.techrequests_list_error_page),
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
            CmpTextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
                CmpText(
                    text = stringResource(Strings.string.techrequests_common_btn_retry),
                    color = colors.onErrorContainer,
                )
            }
        }
    }
}
