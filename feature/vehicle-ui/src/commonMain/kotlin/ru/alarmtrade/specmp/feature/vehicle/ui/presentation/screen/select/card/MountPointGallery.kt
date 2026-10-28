package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.vehicle_photo_a11y_full
import ru.alarmtrade.specmp.core.localization.resources.vehicle_photo_a11y_next
import ru.alarmtrade.specmp.core.localization.resources.vehicle_photo_a11y_prev
import ru.alarmtrade.specmp.core.localization.resources.vehicle_photo_label_counter
import ru.alarmtrade.specmp.core.localization.resources.vehicle_points_btn_back
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpZoomableImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPoint
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CarPhoto
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PHOTO_RATIO

/** Точка подключения: «← Все точки подключения», тип и название, галерея фото, описание. */
@Composable
internal fun MountPointDetail(
    point: MountPoint,
    photo: Int,
    onBack: () -> Unit,
    onPhotoChange: (Int) -> Unit,
    onFullScreen: () -> Unit
) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CmpTextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            CmpIcon(icon = CmpIcons.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
            CmpText(
                text = stringResource(Strings.string.vehicle_points_btn_back),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TileIcon(CmpIcons.Cable, colors.secondaryContainer, colors.onSecondaryContainer)
            CmpColumn {
                CmpText(
                    text = point.type.uppercase(),
                    style = CmpTextStyle.Caption,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
                CmpText(text = point.title, style = CmpTextStyle.Title, color = colors.onSurface)
            }
        }
        if (point.photos.isEmpty()) {
            CarPhoto(url = null, radius = 14.dp, modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO))
        } else {
            Gallery(point = point, photo = photo, onPhotoChange = onPhotoChange, onFullScreen = onFullScreen)
        }
        point.description?.let { CmpText(text = it, style = CmpTextStyle.BodyLarge) }
    }
}

/** Галерея: пейджер (свайп), стрелки, «3 из 7», миниатюры; тап по фото — полноэкранный просмотр. */
@Composable
private fun Gallery(point: MountPoint, photo: Int, onPhotoChange: (Int) -> Unit, onFullScreen: () -> Unit) {
    val count = point.photos.size
    val pager = rememberPagerState(initialPage = photo.coerceIn(0, count - 1)) { count }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect(onPhotoChange) }
    LaunchedEffect(photo) { if (photo != pager.currentPage) pager.animateScrollToPage(photo) }
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpBox(modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO).clip(RoundedCornerShape(14.dp))) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                CarPhoto(
                    url = point.photos[page].url,
                    radius = 0.dp,
                    contentDescription = point.photos[page].name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            onClickLabel = stringResource(Strings.string.vehicle_photo_a11y_full),
                            onClick = onFullScreen
                        )
                        .testTag(VehicleTestTags.PHOTO_FULL)
                )
            }
            Counter(
                index = pager.currentPage,
                count = count,
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
            )
        }
        if (count > 1) {
            CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    icon = CmpIcons.ChevronLeft,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_prev),
                    enabled = pager.currentPage > 0
                )
                // Лениво: у точки бывают десятки фото — миниатюры за краем не композируются и не грузятся.
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(point.photos) { index, item ->
                        val selected = index == pager.currentPage
                        CarPhoto(
                            url = item.url,
                            radius = 8.dp,
                            modifier = Modifier
                                .size(width = 64.dp, height = 48.dp)
                                .border(
                                    if (selected) 2.dp else 0.dp,
                                    if (selected) colors.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { scope.launch { pager.animateScrollToPage(index) } }
                        )
                    }
                }
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    icon = CmpIcons.ChevronRight,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_next),
                    enabled = pager.currentPage < count - 1
                )
            }
        }
    }
}

@Composable
private fun Counter(index: Int, count: Int, modifier: Modifier = Modifier) {
    CmpText(
        text = stringResource(Strings.string.vehicle_photo_label_counter, index + 1, count),
        style = CmpTextStyle.Body,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Полноэкранный просмотр фото точки: зум, листание стрелками и свайпом, «3 из 7», подпись, «✕». */
@Composable
internal fun PhotoViewer(point: MountPoint, photo: Int, onPhotoChange: (Int) -> Unit, onClose: () -> Unit) {
    val count = point.photos.size
    if (count == 0) return
    val pager = rememberPagerState(initialPage = photo.coerceIn(0, count - 1)) { count }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect(onPhotoChange) }
    CmpFullScreenDialog(onDismissRequest = onClose) {
        CmpBox(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = true) { page ->
                CmpZoomableImage(
                    model = point.photos[page].url,
                    contentDescription = point.photos[page].name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            CmpRow(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpText(
                    text = point.type,
                    style = CmpTextStyle.BodyLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                CmpIconButton(
                    onClick = onClose,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.vehicle_common_btn_close),
                    tint = Color.White
                )
            }
            CmpRow(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    icon = CmpIcons.ChevronLeft,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_prev),
                    enabled = pager.currentPage > 0,
                    tint = Color.White
                )
                Counter(index = pager.currentPage, count = count)
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    icon = CmpIcons.ChevronRight,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_next),
                    enabled = pager.currentPage < count - 1,
                    tint = Color.White
                )
            }
        }
    }
}
