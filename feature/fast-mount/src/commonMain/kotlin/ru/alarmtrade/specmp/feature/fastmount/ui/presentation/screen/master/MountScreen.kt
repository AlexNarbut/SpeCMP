package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_a11y_next_photo
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_a11y_prev_photo
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_btn_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_btn_open_map
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_format_photos
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_format_size
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_msg_downloading
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_msg_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_msg_map_error
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_msg_opens_in
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_title_description
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_title_lead
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_title_map
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_title_map_error
import ru.alarmtrade.specmp.core.localization.resources.fastmount_mount_title_photos
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.image.CmpZoomableImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountImage
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.ContentColumn
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Dock
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.InfoCard
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.LocalWizardWide
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardNav
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardScaffold
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.title
import specmp.feature.fast_mount.generated.resources.Res
import specmp.feature.fast_mount.generated.resources.fastmount_slave_learn

internal const val FAST_MOUNT_MOUNT_DONE_TAG = "fastmount_mount_done"
internal const val FAST_MOUNT_MAP_TAG = "fastmount_mount_map"

private const val BYTES_IN_MB = 1024f * 1024f
private const val SLAVE_FRAME_MILLIS = 200L
private const val SLAVE_LAST_FRAME_MILLIS = 600L
private const val SLAVE_RATIO = 800f / 550f
private const val SLAVE_FRAMES = 18
private const val SLAVE_COLUMNS = 3
private const val VIEWER_BUTTON_ALPHA = 0.16f
private val MapIconSize = 56.dp
private val MapIconHeight = 68.dp
private val MapPreviewHeight = 200.dp
private val MapPreviewIconWidth = 96.dp
private val MapPreviewIconHeight = 116.dp
private val MapRetryMaxWidth = 160.dp
private val MapButtonMaxWidth = 280.dp
private val PhotoHeight = 120.dp
private val PhotoLeadHeight = 220.dp
private val PhotoSmallHeight = 116.dp
private val SlaveMaxWidth = 520.dp
private val DescriptionRowHeight = 64.dp

@Composable
internal fun MountScreen(state: FastMountState, actions: MasterActions) {
    val wide = LocalWizardWide.current
    var photo by rememberSaveable { mutableStateOf<Int?>(null) }
    val images = state.configuration?.images.orEmpty()
    WizardScaffold(
        title = stringResource(RailKey.MOUNT.title()),
        subtitle = state.fixed?.title,
        nav = WizardNav.BACK,
        onBack = actions.onBack,
        dock = {
            Dock(
                buttons = listOf {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_mount_btn_done),
                        icon = CmpIcons.Check,
                        busy = state.master.busy,
                        onClick = actions.onMountDone,
                        modifier = Modifier.testTag(FAST_MOUNT_MOUNT_DONE_TAG),
                    )
                },
            )
        },
    ) {
        if (wide) {
            WideMount(
                state,
                actions,
                onPhoto = { photo = it },
            )
        } else {
            PhoneMount(
                state,
                actions,
                onPhoto = { photo = it },
            )
        }
    }
    photo?.let { index ->
        PhotoViewer(images = images, start = index, onClose = { photo = null })
    }
}

@Composable
private fun MountLead(withIcon: Boolean) {
    val colors = CmpTheme.colors
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (withIcon) {
            CmpBox(
                modifier = Modifier
                    .size(CmpSize.touchMin)
                    .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall)),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    CmpIcons.Build,
                    contentDescription = null,
                    tint = colors.foregroundOnDarkPrimaryDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
        }
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = stringResource(Strings.string.fastmount_mount_title_lead), style = CmpTextStyle.Headline1)
            CmpText(
                text = stringResource(Strings.string.fastmount_mount_msg_lead),
                style = CmpTextStyle.Paragraph1,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun PhoneMount(state: FastMountState, actions: MasterActions, onPhoto: (Int) -> Unit) {
    var descriptionOpen by rememberSaveable { mutableStateOf(false) }
    val configuration = state.configuration
    ContentColumn {
        MountLead(withIcon = false)
        if (state.master.mountMap != MountMapState.None) MapCard(state.master.mountMap, actions)
        configuration?.fullDescription?.takeIf { it.isNotBlank() }?.let { description ->
            DescriptionCard(description, open = descriptionOpen, onToggle = { descriptionOpen = !descriptionOpen })
        }
        PhotosHeader(configuration?.images.orEmpty().size)
        PhotoGrid(configuration?.images.orEmpty(), onPhoto)
    }
}

@Composable
private fun WideMount(state: FastMountState, actions: MasterActions, onPhoto: (Int) -> Unit) {
    val configuration = state.configuration
    val images = configuration?.images.orEmpty()
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val two = maxWidth >= 860.dp
        ContentColumn(maxWidth = 1120.dp) {
            MountLead(withIcon = true)
            val left: @Composable (Modifier) -> Unit = { modifier ->
                CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
                    if (state.master.mountMap != MountMapState.None) MapPreview(state.master.mountMap, actions)
                    configuration?.fullDescription?.takeIf { it.isNotBlank() }?.let { description ->
                        InfoCard(
                            title = stringResource(Strings.string.fastmount_mount_title_description),
                            icon = CmpIcons.Description,
                        ) {
                            CmpText(text = description.stripHtml(), style = CmpTextStyle.Paragraph1)
                        }
                    }
                }
            }
            val right: @Composable (Modifier) -> Unit = { modifier ->
                CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                    PhotosHeader(images.size)
                    PhotoMosaic(images, onPhoto)
                }
            }
            if (two) {
                CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24)) {
                    left(Modifier.weight(WIDE_LEFT_WEIGHT))
                    right(Modifier.weight(1f))
                }
            } else {
                left(Modifier)
                right(Modifier)
            }
        }
    }
}

private const val WIDE_LEFT_WEIGHT = 1.25f

@Composable
private fun DescriptionCard(description: String, open: Boolean, onToggle: () -> Unit) {
    val colors = CmpTheme.colors
    CmpCard(modifier = Modifier.fillMaxWidth()) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DescriptionRowHeight)
                .clickable(onClick = onToggle)
                .padding(start = CmpSpacing.space16, end = CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpIcon(
                CmpIcons.Description,
                contentDescription = null,
                tint = colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
            CmpText(
                text = stringResource(Strings.string.fastmount_mount_title_description),
                style = CmpTextStyle.Headline3,
                modifier = Modifier.weight(1f),
            )
            CmpIcon(
                CmpIcons.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.size(CmpSize.iconL).rotate(if (open) HALF_TURN else 0f),
            )
        }
        if (open) {
            CmpText(
                text = description.stripHtml(),
                style = CmpTextStyle.Paragraph1,
                modifier = Modifier.padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space16,
                    bottom = CmpSpacing.space16,
                ),
            )
        }
    }
}

private const val HALF_TURN = 180f

@Composable
private fun MapCard(map: MountMapState, actions: MasterActions) {
    val colors = CmpTheme.colors
    val error = map == MountMapState.Failed
    val ready = map is MountMapState.Ready
    CmpCard(
        modifier = Modifier.fillMaxWidth().testTag(FAST_MOUNT_MAP_TAG),
        containerColor = if (error) colors.errorHighlight else Color.Unspecified,
    ) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CmpSize.rowSection)
                .clickable(enabled = ready, onClick = actions.onMapOpen)
                .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            MapIcon(map, Modifier.size(width = MapIconSize, height = MapIconHeight))
            MapTexts(map, Modifier.weight(1f))
            when {
                ready -> CmpIcon(
                    CmpIcons.OpenInNew,
                    contentDescription = null,
                    tint = colors.brandPrimaryDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )

                error -> CmpButton(
                    text = stringResource(Strings.string.fastmount_common_btn_retry),
                    onClick = actions.onMapRetry,
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    modifier = Modifier.widthIn(max = MapRetryMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun MapTexts(map: MountMapState, modifier: Modifier = Modifier) {
    val error = map == MountMapState.Failed
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = stringResource(mapTitle(error)), style = CmpTextStyle.Headline3)
        CmpText(
            text = mapCaption(map),
            style = CmpTextStyle.Paragraph3,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun MapPreview(map: MountMapState, actions: MasterActions) {
    val colors = CmpTheme.colors
    val error = map == MountMapState.Failed
    CmpCard(modifier = Modifier.fillMaxWidth().testTag(FAST_MOUNT_MAP_TAG)) {
        CmpBox(
            modifier = Modifier.fillMaxWidth().height(MapPreviewHeight).background(colors.backgroundSurfaceHighlight),
            contentAlignment = Alignment.Center,
        ) {
            MapIcon(map, Modifier.size(width = MapPreviewIconWidth, height = MapPreviewIconHeight))
        }
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (error) colors.errorHighlight else Color.Transparent)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            MapTexts(map, Modifier.weight(1f))
            CmpBox(modifier = Modifier.widthIn(max = MapButtonMaxWidth).weight(1f)) {
                if (error) {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_common_btn_retry),
                        icon = CmpIcons.Refresh,
                        outlined = true,
                        onClick = actions.onMapRetry,
                    )
                } else {
                    MainButton(
                        text = stringResource(Strings.string.fastmount_mount_btn_open_map),
                        icon = CmpIcons.OpenInNew,
                        busy = map == MountMapState.Downloading,
                        onClick = actions.onMapOpen,
                    )
                }
            }
        }
    }
}

@Composable
private fun MapIcon(map: MountMapState, modifier: Modifier) {
    val colors = CmpTheme.colors
    val error = map == MountMapState.Failed
    CmpBox(
        modifier = modifier.background(
            if (error) colors.errorHighlight else colors.brandPrimaryHighlight,
            RoundedCornerShape(CmpRadius.cardSmall),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (map == MountMapState.Downloading) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))
        } else {
            CmpIcon(
                icon = if (error) CmpIcons.ErrorOutline else CmpIcons.PictureAsPdf,
                contentDescription = null,
                tint = if (error) colors.errorDefault else colors.brandPrimaryDefault,
                modifier = Modifier.size(CmpSize.iconL),
            )
        }
    }
}

@Composable
private fun mapCaption(map: MountMapState): String = when (map) {
    MountMapState.Downloading -> stringResource(Strings.string.fastmount_mount_msg_downloading)

    MountMapState.Failed -> stringResource(Strings.string.fastmount_mount_msg_map_error)

    is MountMapState.Ready -> stringResource(
        Strings.string.fastmount_mount_msg_opens_in,
        stringResource(Strings.string.fastmount_mount_format_size, formatMegabytes(map.sizeBytes)),
    )

    MountMapState.None -> ""
}

private fun formatMegabytes(bytes: Int): String {
    val tenths = (bytes / BYTES_IN_MB * TENTHS).toInt().coerceAtLeast(1)
    return "${tenths / TENTHS},${tenths % TENTHS}"
}

private const val TENTHS = 10

@Composable
private fun PhotosHeader(count: Int) {
    if (count == 0) return
    CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CmpText(
            text = stringResource(Strings.string.fastmount_mount_title_photos),
            style = CmpTextStyle.Headline1,
            modifier = Modifier.weight(1f),
        )
        CmpText(
            text = stringResource(Strings.string.fastmount_mount_format_photos, count),
            style = CmpTextStyle.Paragraph3,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun PhotoGrid(images: List<MountImage>, onPhoto: (Int) -> Unit) {
    images.indices.chunked(2).forEach { pair ->
        CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            pair.forEach { index ->
                PhotoTile(images[index], PhotoHeight, onClick = { onPhoto(index) }, modifier = Modifier.weight(1f))
            }
            if (pair.size == 1) CmpBox(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PhotoMosaic(images: List<MountImage>, onPhoto: (Int) -> Unit) {
    val first = images.firstOrNull() ?: return
    PhotoTile(first, PhotoLeadHeight, onClick = { onPhoto(0) }, modifier = Modifier.fillMaxWidth())
    images.indices.drop(1).chunked(2).forEach { pair ->
        CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            pair.forEach { index ->
                PhotoTile(images[index], PhotoSmallHeight, onClick = { onPhoto(index) }, modifier = Modifier.weight(1f))
            }
            if (pair.size == 1) CmpBox(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PhotoTile(image: MountImage, height: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpColumn(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpAsyncImage(
            model = image.previewUrl ?: image.url,
            contentDescription = image.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(CmpRadius.cardMedium))
                .background(CmpTheme.colors.backgroundSurfaceHighlight),
        )
        if (image.title.isNotBlank()) CmpText(text = image.title, style = CmpTextStyle.Paragraph3)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoViewer(images: List<MountImage>, start: Int, onClose: () -> Unit) {
    if (images.isEmpty()) return
    val pager = rememberPagerState(initialPage = start.coerceIn(images.indices)) { images.size }
    val scope = rememberCoroutineScope()
    val white = CmpTheme.colors.staticWhite
    CmpFullScreenDialog(onDismissRequest = onClose) {
        CmpColumn(modifier = Modifier.fillMaxSize().background(CmpTheme.colors.staticBlack)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(
                    min = CmpSize.topbar,
                ).padding(horizontal = CmpSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIconButton(
                    onClick = onClose,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.fastmount_mount_a11y_close),
                    tint = white,
                    modifier = Modifier.size(CmpSize.iconXxl),
                )
                CmpText(
                    text = stringResource(Strings.string.fastmount_mount_title_photos),
                    style = CmpTextStyle.Headline2,
                    color = white,
                    modifier = Modifier.weight(1f),
                )
                CmpText(
                    text = "${pager.currentPage + 1} / ${images.size}",
                    style = CmpTextStyle.Headline3,
                    color = white,
                    modifier = Modifier.padding(horizontal = CmpSpacing.space12),
                )
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                CmpZoomableImage(
                    model = images[page].url,
                    contentDescription = images[page].title,
                    modifier = Modifier.fillMaxSize().padding(CmpSpacing.space12),
                )
            }
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space16,
                    top = CmpSpacing.space12,
                    bottom = CmpSpacing.space20,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                ViewerArrow(
                    CmpIcons.ChevronLeft,
                    Strings.string.fastmount_mount_a11y_prev_photo,
                    pager.currentPage > 0,
                ) {
                    scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
                }
                val image = images[pager.currentPage]
                CmpText(
                    text = image.title.ifBlank { image.description },
                    style = CmpTextStyle.Paragraph2,
                    color = white,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                ViewerArrow(
                    CmpIcons.ChevronRight,
                    Strings.string.fastmount_mount_a11y_next_photo,
                    pager.currentPage < images.lastIndex,
                ) {
                    scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }
            }
        }
    }
}

@Composable
private fun ViewerArrow(
    icon: CmpIcons,
    label: org.jetbrains.compose.resources.StringResource,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val white = CmpTheme.colors.staticWhite
    CmpBox(
        modifier = Modifier
            .size(CmpSize.iconXxl)
            .background(
                white.copy(alpha = if (enabled) VIEWER_BUTTON_ALPHA else VIEWER_BUTTON_ALPHA / 2),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CmpIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = stringResource(label),
            enabled = enabled,
            tint = white.copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
        )
    }
}

private const val DISABLED_ALPHA = 0.35f

/** Спрайт-лист из `slave_learn.gif` старого приложения: 18 кадров 800×550, [SLAVE_COLUMNS] колонки, 5 кадров/с. */
@Composable
internal fun SlaveAnimation(modifier: Modifier = Modifier) {
    val sheet = imageResource(Res.drawable.fastmount_slave_learn)
    val frame = remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(if (frame.intValue == SLAVE_FRAMES - 1) SLAVE_LAST_FRAME_MILLIS else SLAVE_FRAME_MILLIS)
            frame.intValue = (frame.intValue + 1) % SLAVE_FRAMES
        }
    }
    // Кадры на белом фоне (исходник старого приложения) — в рамке, чтобы в тёмной теме не «висели» в воздухе.
    // Кадр читается только при отрисовке — смена кадра не вызывает рекомпозицию.
    CmpBox(
        modifier = modifier
            .widthIn(max = SlaveMaxWidth)
            .aspectRatio(SLAVE_RATIO)
            .clip(RoundedCornerShape(CmpRadius.cardMedium))
            .background(CmpTheme.colors.staticWhite)
            .drawBehind {
                val cell = IntSize(sheet.width / SLAVE_COLUMNS, sheet.height / rowsOf(SLAVE_FRAMES))
                val index = frame.intValue
                drawImage(
                    image = sheet,
                    srcOffset = IntOffset(index % SLAVE_COLUMNS * cell.width, index / SLAVE_COLUMNS * cell.height),
                    srcSize = cell,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    filterQuality = FilterQuality.Medium,
                )
            },
    )
}

private fun rowsOf(frames: Int) = (frames + SLAVE_COLUMNS - 1) / SLAVE_COLUMNS

/** Описание конфигурации приходит HTML — для текста без разметки. */
internal fun String.stripHtml(): String = replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<[^>]+>"), "")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .trim()

private fun mapTitle(error: Boolean) =
    if (error) Strings.string.fastmount_mount_title_map_error else Strings.string.fastmount_mount_title_map
