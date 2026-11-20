package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_btn_issue
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_btn_open
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_format_issued
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_msg_offer
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_title_issued
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_passport_title_offer
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry

private val PassportIconBox = 40.dp
private val OfferBorder = 2.dp

/** Карточка журнала «Монтаж закончен? Оформите паспорт»; оформлен — «Паспорт оформлен · №» и «Открыть». */
@Composable
internal fun PassportCard(entry: JournalEntry.Passport, onAction: (AssistantAction) -> Unit) {
    val colors = CmpTheme.colors
    val passport = entry.passport?.takeIf { it.issued }
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    val frame = if (passport == null) Modifier.border(OfferBorder, colors.brandPrimaryDefault, shape) else Modifier
    CmpCard(modifier = Modifier.fillMaxWidth().then(frame).testTag(resultTag(entry.id))) {
        CmpColumn(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpBox(
                    modifier = Modifier
                        .size(PassportIconBox)
                        .background(
                            if (passport != null) colors.successHighlight else colors.backgroundSurfaceHighlight,
                            shape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        icon = if (passport != null) CmpIcons.TaskAlt else CmpIcons.Description,
                        contentDescription = null,
                        tint = if (passport != null) colors.successDefault else colors.brandPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
                CmpText(
                    text = stringResource(
                        if (passport != null) {
                            Strings.string.mountassistant_passport_title_issued
                        } else {
                            Strings.string.mountassistant_passport_title_offer
                        },
                    ),
                    style = CmpTextStyle.Headline2,
                    modifier = Modifier.weight(1f),
                )
            }
            CmpText(
                text = if (passport != null) {
                    stringResource(Strings.string.mountassistant_passport_format_issued, passport.number)
                } else {
                    stringResource(Strings.string.mountassistant_passport_msg_offer)
                },
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
            CmpButton(
                text = stringResource(
                    if (passport != null) {
                        Strings.string.mountassistant_passport_btn_open
                    } else {
                        Strings.string.mountassistant_passport_btn_issue
                    },
                ),
                onClick = { onAction(AssistantAction.MOUNT_PASSPORT) },
                type = if (passport != null) CmpButtonType.Secondary else CmpButtonType.Primary,
                icon = if (passport != null) CmpIcons.OpenInNew else CmpIcons.Description,
            )
        }
    }
}
