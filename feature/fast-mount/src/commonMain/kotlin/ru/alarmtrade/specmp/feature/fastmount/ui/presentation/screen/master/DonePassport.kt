package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_btn_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_btn_passport_open
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_btn_passport_share
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_format_passport_number
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_msg_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_passport
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_title_passport_issued
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton

internal const val FAST_MOUNT_PASSPORT_TAG = "fastmount_passport"

private val PassportIconBox = 48.dp
private val ActionMinWidth = 180.dp

/** Блок «Паспорт монтажа» на финале: второе действие, «На главную» остаётся главной кнопкой. */
@Composable
internal fun DonePassportBlock(
    passport: FastMountPassport?,
    canShare: Boolean,
    actions: MasterActions,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val issuedNumber = passport?.takeIf { it.issued }?.number
    val issued = issuedNumber != null
    CmpCard(modifier = modifier.fillMaxWidth().testTag(FAST_MOUNT_PASSPORT_TAG)) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                CmpBox(
                    modifier = Modifier
                        .size(PassportIconBox)
                        .background(
                            if (issued) colors.successHighlight else colors.backgroundSurfaceHighlight,
                            RoundedCornerShape(CmpRadius.cardMedium),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        icon = if (issued) CmpIcons.TaskAlt else CmpIcons.Description,
                        contentDescription = null,
                        tint = if (issued) colors.successDefault else colors.foregroundOnDarkPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
                CmpColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                ) {
                    CmpText(
                        text = stringResource(
                            if (issued) {
                                Strings.string.fastmount_done_title_passport_issued
                            } else {
                                Strings.string.fastmount_done_title_passport
                            },
                        ),
                        style = CmpTextStyle.Headline2,
                    )
                    CmpText(
                        text = if (issuedNumber != null) {
                            stringResource(Strings.string.fastmount_done_format_passport_number, issuedNumber)
                        } else {
                            stringResource(Strings.string.fastmount_done_msg_passport)
                        },
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
            if (issued) {
                IssuedActions(canShare = canShare, actions = actions)
            } else {
                MainButton(
                    text = stringResource(Strings.string.fastmount_done_btn_passport),
                    icon = CmpIcons.Description,
                    outlined = true,
                    onClick = actions.onPassportStart,
                )
            }
        }
    }
}

/** Как в макете: кнопки в ряд от 180 dp каждая, уже — столбиком. */
@Composable
private fun IssuedActions(canShare: Boolean, actions: MasterActions) {
    val share: @Composable (Modifier) -> Unit = { modifier ->
        MainButton(
            text = stringResource(Strings.string.fastmount_done_btn_passport_share),
            icon = CmpIcons.Share,
            outlined = true,
            onClick = actions.onPassportShare,
            modifier = modifier,
        )
    }
    val open: @Composable (Modifier) -> Unit = { modifier ->
        MainButton(
            text = stringResource(Strings.string.fastmount_done_btn_passport_open),
            icon = CmpIcons.OpenInNew,
            outlined = true,
            onClick = actions.onPassportOpen,
            modifier = modifier,
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val row = canShare && maxWidth >= ActionMinWidth * 2 + CmpSpacing.space8
        if (row) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                share(Modifier.weight(1f))
                open(Modifier.weight(1f))
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                if (canShare) share(Modifier)
                open(Modifier)
            }
        }
    }
}
