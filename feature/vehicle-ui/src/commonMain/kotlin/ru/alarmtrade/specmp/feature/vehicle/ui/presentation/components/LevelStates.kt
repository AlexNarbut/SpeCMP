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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_btn_filter_reset
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_error
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_filter_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_offline
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_error
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_filter_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_offline
import specmp.feature.vehicle_ui.generated.resources.vehicle_common_btn_retry

/** Ошибка загрузки уровня: «нет интернета» или «сервер не ответил», с «Повторить». */
@Composable
internal fun LoadFailed(
    offline: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    CmpStateBox(
        icon = if (offline) CmpIcons.WifiOff else CmpIcons.CloudOff,
        title = stringResource(
            if (offline) Res.string.vehicle_catalog_title_offline else Res.string.vehicle_catalog_title_error
        ),
        text = stringResource(
            if (offline) Res.string.vehicle_catalog_msg_offline else Res.string.vehicle_catalog_msg_error
        ),
        tone = CmpStateTone.Error,
        compact = compact,
        modifier = modifier
    ) {
        StateAction(text = stringResource(Res.string.vehicle_common_btn_retry), onClick = onRetry, primary = true)
    }
}

/** Фильтр функций отсеял всё на уровне — «Сбросить фильтр». */
@Composable
internal fun FilteredOut(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    text: String? = null
) {
    CmpStateBox(
        icon = CmpIcons.FilterAltOff,
        title = stringResource(Res.string.vehicle_catalog_title_filter_empty),
        text = text ?: stringResource(Res.string.vehicle_catalog_msg_filter_empty),
        compact = compact,
        modifier = modifier
    ) {
        StateAction(text = stringResource(Res.string.vehicle_catalog_btn_filter_reset), onClick = onReset)
    }
}

/** Кнопка действия в блоке состояния (52 dp, от 200 dp). */
@Composable
internal fun StateAction(text: String, onClick: () -> Unit, primary: Boolean = false) {
    val modifier = Modifier.heightIn(min = 52.dp).width(220.dp).let {
        if (primary) it.testTag(VehicleTestTags.RETRY) else it
    }
    if (primary) {
        CmpButton(text = text, onClick = onClick, modifier = modifier)
    } else {
        CmpOutlinedButton(text = text, onClick = onClick, modifier = modifier)
    }
}

/** Скелетон списка: [count] строк, слева квадрат [lead] или миниатюра фото ([thumb]). */
@Composable
internal fun SkeletonRows(count: Int, modifier: Modifier = Modifier, thumb: Boolean = false, lead: Dp = 40.dp) {
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (thumb) {
                    CmpSkeleton(Modifier.size(width = 84.dp, height = 56.dp), shape = RoundedCornerShape(10.dp))
                } else {
                    CmpSkeleton(Modifier.size(lead), shape = RoundedCornerShape(12.dp))
                }
                CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CmpSkeleton(Modifier.width(180.dp).height(18.dp))
                    if (thumb) CmpSkeleton(Modifier.width(110.dp).height(14.dp))
                }
            }
        }
    }
}
