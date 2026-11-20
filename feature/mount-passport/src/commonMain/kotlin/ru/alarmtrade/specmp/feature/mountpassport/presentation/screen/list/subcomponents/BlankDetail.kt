package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_label_no_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_hint_connect_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_blank
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_select
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_select
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary

private val BlankCardMaxWidth = 460.dp

/** Правая часть широкого окна для заготовки: что осталось сделать и «Дооформить» при подключённом блоке. */
@Composable
internal fun BlankDetail(
    item: MountPassportSummary,
    canFinish: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Box(modifier = modifier.fillMaxSize().padding(CmpSpacing.space24), contentAlignment = Alignment.Center) {
        CmpCard(modifier = Modifier.widthIn(max = BlankCardMaxWidth)) {
            Column(
                modifier = Modifier.padding(CmpSpacing.space24),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                StatusTag(item.status)
                val car = item.carTitle?.takeIf { it.isNotBlank() }
                    ?: stringResource(Strings.string.mountpassport_common_label_no_car)
                CmpText(
                    text = stringResource(Strings.string.mountpassport_common_format_unit, car, item.systemId),
                    style = CmpTextStyle.Headline1,
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_list_msg_blank),
                    style = CmpTextStyle.Paragraph2,
                    color = muted,
                )
                if (canFinish) {
                    CmpButton(
                        text = stringResource(Strings.string.mountpassport_list_btn_finish),
                        onClick = onFinish,
                        icon = CmpIcons.Description,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CmpIcon(
                            icon = CmpIcons.Bluetooth,
                            contentDescription = null,
                            tint = muted,
                            modifier = Modifier.size(CmpSize.iconS),
                        )
                        CmpText(
                            text = stringResource(
                                Strings.string.mountpassport_list_hint_connect_unit,
                                item.unitModel,
                                item.systemId,
                            ),
                            style = CmpTextStyle.Paragraph3,
                            color = muted,
                            modifier = Modifier.padding(start = CmpSpacing.space8),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectHint(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CmpStateBox(
            icon = CmpIcons.Description,
            title = stringResource(Strings.string.mountpassport_list_title_select),
            text = stringResource(Strings.string.mountpassport_list_msg_select),
        )
    }
}
