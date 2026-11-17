package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
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

@Composable
internal fun MountPointDetail(
    point: MountPoint,
    photo: Int,
    onBack: () -> Unit,
    onPhotoChange: (Int) -> Unit,
    onFullScreen: () -> Unit,
) {
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpButton(
            text = stringResource(Strings.string.vehicle_points_btn_back),
            onClick = onBack,
            type = CmpButtonType.Tertiary,
            size = CmpButtonSize.S,
            icon = CmpIcons.ArrowBack,
        )
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TileIcon(CmpIcons.Cable, colors.backgroundSurfaceHighlight, colors.foregroundOnDarkPrimaryDefault)
            CmpColumn {
                CmpText(
                    text = point.type.uppercase(),
                    style = CmpTextStyle.Additional0,
                    color = colors.brandPrimaryDefault,
                )
                CmpText(
                    text = point.title,
                    style = CmpTextStyle.Headline1,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
            }
        }
        if (point.photos.isEmpty()) {
            CarPhoto(url = null, modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO))
        } else {
            Gallery(point = point, photo = photo, onPhotoChange = onPhotoChange, onFullScreen = onFullScreen)
        }
        point.description?.let { CmpText(text = it, style = CmpTextStyle.Paragraph2) }
    }
}

@Composable
private fun Gallery(point: MountPoint, photo: Int, onPhotoChange: (Int) -> Unit, onFullScreen: () -> Unit) {
    val count = point.photos.size
    val pager = rememberPagerState(initialPage = photo.coerceIn(0, count - 1)) { count }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect(onPhotoChange) }
    LaunchedEffect(photo) { if (photo != pager.currentPage) pager.animateScrollToPage(photo) }
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpBox(
            modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO).clip(RoundedCornerShape(CmpRadius.cardSmall)),
        ) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                CarPhoto(
                    url = point.photos[page].url,
                    radius = 0.dp,
                    contentDescription = point.photos[page].name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            onClickLabel = stringResource(Strings.string.vehicle_photo_a11y_full),
                            onClick = onFullScreen,
                        )
                        .testTag(VehicleTestTags.PHOTO_FULL),
                )
            }
            Counter(
                index = pager.currentPage,
                count = count,
                modifier = Modifier.align(Alignment.BottomEnd).padding(CmpSpacing.space12),
            )
        }
        if (count > 1) {
            CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    icon = CmpIcons.ChevronLeft,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_prev),
                    enabled = pager.currentPage > 0,
                )
                // Лениво: у точки бывают десятки фото — миниатюры за краем не композируются и не грузятся.
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    itemsIndexed(point.photos) { index, item ->
                        val selected = index == pager.currentPage
                        CarPhoto(
                            url = item.url,
                            radius = CmpRadius.tags,
                            modifier = Modifier
                                .size(width = ThumbWidth, height = CmpSize.touchMin)
                                .border(
                                    if (selected) ThumbSelectedBorder else 0.dp,
                                    if (selected) colors.brandPrimaryDefault else Color.Transparent,
                                    RoundedCornerShape(CmpRadius.tags),
                                )
                                .clickable { scope.launch { pager.animateScrollToPage(index) } },
                        )
                    }
                }
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    icon = CmpIcons.ChevronRight,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_next),
                    enabled = pager.currentPage < count - 1,
                )
            }
        }
    }
}

@Composable
private fun Counter(index: Int, count: Int, modifier: Modifier = Modifier) {
    CmpText(
        text = stringResource(Strings.string.vehicle_photo_label_counter, index + 1, count),
        style = CmpTextStyle.Headline3,
        color = cmpDomainColors().onCamera,
        modifier = modifier
            .background(cmpDomainColors().cameraScrim, RoundedCornerShape(CmpRadius.round))
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
    )
}

@Composable
internal fun PhotoViewer(point: MountPoint, photo: Int, onPhotoChange: (Int) -> Unit, onClose: () -> Unit) {
    val count = point.photos.size
    if (count == 0) return
    val pager = rememberPagerState(initialPage = photo.coerceIn(0, count - 1)) { count }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect(onPhotoChange) }
    val camera = cmpDomainColors()
    CmpFullScreenDialog(onDismissRequest = onClose) {
        CmpBox(modifier = Modifier.fillMaxSize().background(camera.camera)) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = true) { page ->
                CmpZoomableImage(
                    model = point.photos[page].url,
                    contentDescription = point.photos[page].name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            CmpRow(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(
                    text = point.type,
                    style = CmpTextStyle.Headline2,
                    color = camera.onCamera,
                    modifier = Modifier.weight(1f).padding(start = CmpSpacing.space8),
                )
                CmpIconButton(
                    onClick = onClose,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.vehicle_common_btn_close),
                    tint = camera.onCamera,
                )
            }
            CmpRow(
                modifier = Modifier.align(Alignment.BottomCenter).padding(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                    icon = CmpIcons.ChevronLeft,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_prev),
                    enabled = pager.currentPage > 0,
                    tint = camera.onCamera,
                )
                Counter(index = pager.currentPage, count = count)
                CmpIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    icon = CmpIcons.ChevronRight,
                    contentDescription = stringResource(Strings.string.vehicle_photo_a11y_next),
                    enabled = pager.currentPage < count - 1,
                    tint = camera.onCamera,
                )
            }
        }
    }
}

private val ThumbWidth = 64.dp
private val ThumbSelectedBorder = 2.dp
