package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.RollbackUi
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_btn_install
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_btn_refresh
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_btn_reinstall
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_empty_title
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_error_more
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_error_msg
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_error_title
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_loading
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_loading_more
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_msg_older
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_msg_same
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_rollback_msg
import specmp.feature.device.firmware_ui.generated.resources.firmware_archive_rollback_title
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_label_full_description
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_msg_no_description
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_btn_cancel
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_btn_retry

private val CenterMaxWidth = 440.dp
private val ButtonHeight = 48.dp

internal const val ARCHIVE_INSTALL_TAG = "firmware_archive_install"
internal const val ARCHIVE_ROLLBACK_CONFIRM_TAG = "firmware_archive_rollback_confirm"
internal const val ARCHIVE_REFRESH_TAG = "firmware_archive_refresh"

@Composable
internal fun ArchiveLoading() {
    CenteredBody {
        CmpCircularProgressIndicator()
        CmpText(
            text = stringResource(Res.string.firmware_archive_loading),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ArchiveEmpty(onRefresh: () -> Unit) {
    CenteredBody {
        FirmwareHero(icon = CmpIcons.Inventory2, tone = HeroTone.Neutral, size = 88.dp)
        CmpText(
            text = stringResource(Res.string.firmware_archive_empty_title),
            style = CmpTextStyle.Title,
            textAlign = TextAlign.Center
        )
        CmpOutlinedButton(
            text = stringResource(Res.string.firmware_archive_btn_refresh),
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}

@Composable
internal fun ArchiveError(onRetry: () -> Unit) {
    CenteredBody {
        FirmwareHero(icon = CmpIcons.CloudOff, tone = HeroTone.Neutral, size = 88.dp)
        CmpText(
            text = stringResource(Res.string.firmware_archive_error_title),
            style = CmpTextStyle.Title,
            textAlign = TextAlign.Center
        )
        CmpText(
            text = stringResource(Res.string.firmware_archive_error_msg),
            color = cmpThemeColors().onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        CmpButton(
            text = stringResource(Res.string.firmware_common_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}

@Composable
private fun CenteredBody(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(
                max = CenterMaxWidth
            ).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

/** Подвал списка: индикатор догрузки или ошибка догрузки (список выше остаётся). */
@Composable
internal fun ArchiveFooter(loadingMore: Boolean, moreError: Boolean, onRetryMore: () -> Unit) {
    when {
        loadingMore -> CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            CmpText(
                text = stringResource(Res.string.firmware_archive_loading_more),
                color = cmpThemeColors().onSurfaceVariant,
                style = CmpTextStyle.Body
            )
        }

        moreError -> InfoBanner(
            text = stringResource(Res.string.firmware_archive_error_more),
            tone = BannerTone.Error,
            icon = CmpIcons.CloudOff,
            action = {
                CmpTextButton(
                    text = stringResource(Res.string.firmware_common_btn_retry),
                    onClick = onRetryMore,
                    modifier = Modifier.height(ButtonHeight)
                )
            }
        )
    }
}

/** Панель справа на широком окне: версия, метка, дата, полное описание, предупреждения и «Установить». */
@Composable
internal fun ArchiveDetail(
    row: ArchiveRowUi,
    installedVersion: String?,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpColumn(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VersionText(row.version, fontSize = 34.sp)
                    VersionBadge(row.kind)
                }
                CmpText(text = formatArchiveDate(row.createdOn), color = colors.onSurfaceVariant)
            }
            CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(
                    text = stringResource(Res.string.firmware_check_dialog_label_full_description),
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant
                )
                CmpText(
                    text = row.description.ifBlank {
                        stringResource(Res.string.firmware_check_dialog_msg_no_description)
                    }
                )
            }
            when (row.kind) {
                ArchiveRowKind.Older -> installedVersion?.let {
                    InfoBanner(text = stringResource(Res.string.firmware_archive_msg_older, it))
                }

                ArchiveRowKind.Installed -> InfoBanner(
                    text = stringResource(Res.string.firmware_archive_msg_same),
                    tone = BannerTone.Ok,
                    icon = CmpIcons.Info
                )

                ArchiveRowKind.New -> Unit
            }
            CmpButton(
                text = stringResource(
                    if (row.kind == ArchiveRowKind.Installed) {
                        Res.string.firmware_archive_btn_reinstall
                    } else {
                        Res.string.firmware_archive_btn_install
                    }
                ),
                onClick = onInstall,
                modifier = Modifier.widthIn(min = 240.dp).height(52.dp).testTag(ARCHIVE_INSTALL_TAG)
            )
        }
    }
}

/** «Установить более старую версию?»: версия старее установленной, возможно без новых функций и исправлений. */
@Composable
internal fun RollbackDialog(rollback: RollbackUi, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
        icon = CmpIcons.Warning.imageVector(),
        title = { CmpText(text = stringResource(Res.string.firmware_archive_rollback_title)) },
        text = {
            CmpText(
                text = stringResource(
                    Res.string.firmware_archive_rollback_msg,
                    rollback.version,
                    rollback.installedVersion
                ),
                style = CmpTextStyle.Body
            )
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight)
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Res.string.firmware_archive_btn_install),
                onClick = onConfirm,
                modifier = Modifier.height(ButtonHeight).testTag(ARCHIVE_ROLLBACK_CONFIRM_TAG)
            )
        }
    )
}
