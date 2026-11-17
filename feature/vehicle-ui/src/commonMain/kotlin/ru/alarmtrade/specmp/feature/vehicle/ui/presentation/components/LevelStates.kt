package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_btn_filter_reset
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_error
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_filter_empty
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_error
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_filter_empty
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_btn_retry
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError

@Composable
internal fun LoadFailed(
    error: CatalogError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val offline = error == CatalogError.Common(ApiError.NoConnection)
    CmpStateBox(
        icon = if (offline) CmpIcons.WifiOff else CmpIcons.CloudOff,
        title = when (error) {
            is CatalogError.Common -> error.error.message()
            CatalogError.LoadFailed -> stringResource(Strings.string.vehicle_catalog_title_error)
        },
        text = stringResource(
            if (offline) Strings.string.vehicle_catalog_msg_offline else Strings.string.vehicle_catalog_msg_error,
        ),
        tone = CmpStateTone.Error,
        compact = compact,
        modifier = modifier,
    ) {
        StateAction(text = stringResource(Strings.string.vehicle_common_btn_retry), onClick = onRetry, primary = true)
    }
}

@Composable
internal fun FilteredOut(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    text: String? = null,
) {
    CmpStateBox(
        icon = CmpIcons.FilterAltOff,
        title = stringResource(Strings.string.vehicle_catalog_title_filter_empty),
        text = text ?: stringResource(Strings.string.vehicle_catalog_msg_filter_empty),
        compact = compact,
        modifier = modifier,
    ) {
        StateAction(text = stringResource(Strings.string.vehicle_catalog_btn_filter_reset), onClick = onReset)
    }
}

@Composable
internal fun StateAction(text: String, onClick: () -> Unit, primary: Boolean = false) {
    CmpButton(
        text = text,
        onClick = onClick,
        type = if (primary) CmpButtonType.Primary else CmpButtonType.Secondary,
        modifier = Modifier.width(StateActionWidth).let { if (primary) it.testTag(VehicleTestTags.RETRY) else it },
    )
}

private val StateActionWidth = 220.dp
private val SkeletonTitle = 180.dp
private val SkeletonSubtitle = 110.dp
private val SkeletonLine = 18.dp
private val SkeletonSubLine = 14.dp

@Composable
internal fun SkeletonRows(count: Int, modifier: Modifier = Modifier, thumb: Boolean = false) {
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        repeat(count) {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = RowMinHeight)
                    .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpSkeleton(
                    Modifier.size(if (thumb) PhotoThumbSize else ThumbSize),
                    shape = RoundedCornerShape(ThumbRadius),
                )
                CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                    CmpSkeleton(Modifier.width(SkeletonTitle).height(SkeletonLine))
                    if (thumb) CmpSkeleton(Modifier.width(SkeletonSubtitle).height(SkeletonSubLine))
                }
            }
        }
    }
}
