package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_a11y_photo
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_add_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_add_photo
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_continue_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_remove_missing
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_photos_count
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_photos_empty
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_photos_empty_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_hint_photos_full
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_loading_photo
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_missing_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_photo_unavailable
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_missing_photos
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_a11y_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_btn_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_hint_caption
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_item_camera
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_item_gallery
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_label_caption
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_label_position
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_msg_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_msg_denied
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_placeholder_caption
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_title_add
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_title_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_photo_title_denied
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.image.CmpZoomableImage
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.presentation.component.DeleteConfirmDialog
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.photoText
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportPhotoActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PhotoSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PhotoSource

private const val GRID_COLUMNS = 3

/** Сетка фото 3 в ряд, плитка «Добавить» до лимита; ошибка (нет места, лимит) — под сеткой. */
@Composable
internal fun PhotosBody(passport: MountPassport, state: PassportEditorState, actions: PassportPhotoActions) {
    val photos = passport.photos
    val tiles: List<PassportPhoto?> = buildList {
        addAll(photos)
        if (state.addingPhoto || photos.size < MountPassport.MAX_PHOTOS) add(null)
    }
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        tiles.chunked(GRID_COLUMNS).forEachIndexed { row, chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                chunk.forEachIndexed { column, photo ->
                    val index = row * GRID_COLUMNS + column
                    val tile = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(CmpRadius.cardSmall))
                    when {
                        photo != null && photo.id in state.unavailablePhotos ->
                            UnavailableTile(index, onDelete = { actions.onDeleteRequest(photo.id) }, modifier = tile)

                        photo != null -> PhotoTile(photo, index, onOpen = { actions.onOpen(index) }, modifier = tile)

                        state.addingPhoto -> LoadingTile(tile)

                        else -> AddTile(state.canTakePhoto, actions.onAdd, tile)
                    }
                }
                repeat(GRID_COLUMNS - chunk.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        state.photoError?.let { error ->
            CmpBanner(
                text = error.photoText(),
                tone = CmpBannerTone.Error,
                icon = CmpIcons.ErrorOutline,
                onDismiss = actions.onErrorDismiss,
            )
        }
        CmpText(
            text = photosHint(photos.size, state.canTakePhoto),
            style = CmpTextStyle.Additional0,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun photosHint(count: Int, canTakePhoto: Boolean): String = when {
    count >= MountPassport.MAX_PHOTOS ->
        stringResource(Strings.string.mountpassport_editor_hint_photos_full, MountPassport.MAX_PHOTOS)

    count > 0 -> stringResource(Strings.string.mountpassport_editor_hint_photos_count, count, MountPassport.MAX_PHOTOS)

    canTakePhoto -> stringResource(Strings.string.mountpassport_editor_hint_photos_empty)

    else -> stringResource(Strings.string.mountpassport_editor_hint_photos_empty_file)
}

@Composable
private fun PhotoTile(photo: PassportPhoto, index: Int, onOpen: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(CmpTheme.colors.backgroundSurfaceHighlight)
            .cmpPressable(onClick = onOpen)
            .testTag(MountPassportTags.photo(index)),
    ) {
        CmpAsyncImage(
            model = photo.thumbnailPath,
            contentDescription = stringResource(Strings.string.mountpassport_editor_a11y_photo, index + 1),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        photo.caption?.let { caption ->
            CmpText(
                text = caption,
                style = CmpTextStyle.Additional1,
                maxLines = 1,
                color = CmpTheme.colors.foregroundOnLightPrimaryDefault,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(CmpTheme.colors.backgroundOverlay)
                    .padding(horizontal = CmpSpacing.space4),
            )
        }
    }
}

/** Файла фото нет на устройстве: вместо картинки — пометка и удаление (в документ такое фото не попадёт). */
@Composable
private fun UnavailableTile(index: Int, onDelete: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(CmpTheme.colors.backgroundSurfaceHighlight)
            .testTag(MountPassportTags.photo(index)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(CmpSpacing.space4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CmpIcon(icon = CmpIcons.NoPhotography, contentDescription = null, tint = CmpTheme.colors.warningDefault)
            CmpText(
                text = stringResource(Strings.string.mountpassport_editor_status_photo_unavailable),
                style = CmpTextStyle.Additional1,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = CmpSpacing.space4),
            )
        }
        CmpIconButton(
            onClick = onDelete,
            icon = CmpIcons.Delete,
            contentDescription = stringResource(Strings.string.mountpassport_photo_a11y_delete),
            modifier = Modifier.align(Alignment.TopEnd).testTag(MountPassportTags.photoDelete(index)),
        )
    }
}

/** Удаление фото с плитки (недоступное фото), вне просмотра на весь экран. */
@Composable
internal fun PhotoDeleteDialog(index: Int, actions: PassportPhotoActions) {
    DeleteConfirmDialog(
        title = stringResource(Strings.string.mountpassport_photo_title_delete, index + 1),
        text = stringResource(Strings.string.mountpassport_photo_msg_delete),
        onConfirm = actions.onDeleteConfirm,
        onDismiss = actions.onDeleteDismiss,
        confirmTag = MountPassportTags.DELETE_CONFIRM,
    )
}

/** Перед «Сформировать»: фото без файла в документ не попадут — удалить их из паспорта или продолжить. */
@Composable
internal fun MissingPhotosDialog(count: Int, actions: PassportPhotoActions) {
    CmpDialog(
        onDismissRequest = actions.onMissingDismiss,
        icon = CmpIcons.NoPhotography.imageVector(),
        title = {
            CmpText(
                text = pluralStringResource(Strings.plurals.mountpassport_editor_title_missing_photos, count, count),
                style = CmpTextStyle.Headline1,
            )
        },
        text = {
            CmpText(
                text = pluralStringResource(Strings.plurals.mountpassport_editor_msg_missing_photos, count),
                style = CmpTextStyle.Paragraph2,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_editor_btn_continue_pdf),
                onClick = actions.onMissingContinue,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(MountPassportTags.MISSING_PHOTOS_CONTINUE),
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_editor_btn_remove_missing),
                onClick = actions.onMissingRemove,
                type = CmpButtonType.TertiaryDanger,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(MountPassportTags.MISSING_PHOTOS_REMOVE),
            )
        },
    )
}

@Composable
private fun LoadingTile(modifier: Modifier) {
    Column(
        modifier = modifier.background(CmpTheme.colors.backgroundSurfaceHighlight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CmpCircularProgressIndicator(modifier = Modifier.size(CmpSpacing.space24))
        CmpText(
            text = stringResource(Strings.string.mountpassport_editor_loading_photo),
            style = CmpTextStyle.Additional1,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.padding(top = CmpSpacing.space4),
        )
    }
}

@Composable
private fun AddTile(canTakePhoto: Boolean, onAdd: () -> Unit, modifier: Modifier) {
    val brand = CmpTheme.colors.brandPrimaryDefault
    Column(
        modifier = modifier
            .background(CmpTheme.colors.brandPrimaryHighlight)
            .cmpPressable(onClick = onAdd)
            .padding(CmpSpacing.space4)
            .testTag(MountPassportTags.ADD_PHOTO),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CmpIcon(icon = CmpIcons.PhotoCamera, contentDescription = null, tint = brand)
        CmpText(
            text = stringResource(
                if (canTakePhoto) {
                    Strings.string.mountpassport_editor_btn_add_photo
                } else {
                    Strings.string.mountpassport_editor_btn_add_file
                },
            ),
            style = CmpTextStyle.Additional0,
            color = brand,
            modifier = Modifier.padding(top = CmpSpacing.space4),
        )
    }
}

/** Источник фото (камера / галерея) и «Нет доступа к камере» с переходом в настройки. */
@Composable
internal fun PhotoSourceSheet(sheet: PhotoSheet, actions: PassportPhotoActions) {
    CmpBottomSheet(onDismissRequest = actions.onSheetDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            when (sheet) {
                PhotoSheet.Pick -> {
                    CmpText(
                        text = stringResource(Strings.string.mountpassport_photo_title_add),
                        style = CmpTextStyle.Headline2,
                    )
                    CmpListItem(
                        title = stringResource(Strings.string.mountpassport_photo_item_camera),
                        icon = CmpIcons.PhotoCamera,
                        flat = true,
                        onClick = { actions.onSource(PhotoSource.CAMERA) },
                    )
                    CmpListItem(
                        title = stringResource(Strings.string.mountpassport_photo_item_gallery),
                        icon = CmpIcons.PhotoLibrary,
                        flat = true,
                        onClick = { actions.onSource(PhotoSource.GALLERY) },
                    )
                }

                is PhotoSheet.CameraDenied -> {
                    Row(verticalAlignment = Alignment.Top) {
                        CmpIcon(
                            icon = CmpIcons.NoPhotography,
                            contentDescription = null,
                            tint = CmpTheme.colors.warningDefault,
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = CmpSpacing.space12)) {
                            CmpText(
                                text = stringResource(Strings.string.mountpassport_photo_title_denied),
                                style = CmpTextStyle.Headline2,
                            )
                            CmpText(
                                text = stringResource(Strings.string.mountpassport_photo_msg_denied),
                                style = CmpTextStyle.Paragraph2,
                                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                            )
                        }
                    }
                    if (sheet.canOpenSettings) {
                        CmpButton(
                            text = stringResource(Strings.string.mountpassport_photo_btn_settings),
                            onClick = actions.onOpenAppSettings,
                            icon = CmpIcons.Settings,
                            fullWidth = true,
                        )
                    }
                    CmpButton(
                        text = stringResource(Strings.string.mountpassport_photo_item_gallery),
                        onClick = { actions.onSource(PhotoSource.GALLERY) },
                        type = CmpButtonType.Secondary,
                        icon = CmpIcons.PhotoLibrary,
                        fullWidth = true,
                    )
                }
            }
        }
    }
}

/** Просмотр на весь экран: листание, подпись в одну строку, удаление с подтверждением. */
@Composable
internal fun PhotoViewer(
    photos: List<PassportPhoto>,
    index: Int,
    confirmDelete: Boolean,
    actions: PassportPhotoActions,
) {
    val pager = rememberPagerState(initialPage = index) { photos.size }
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { actions.onViewerIndex(it) } }
    val current = photos.getOrNull(pager.currentPage) ?: return
    CmpFullScreenDialog(onDismissRequest = actions.onViewerClose) {
        Column(modifier = Modifier.fillMaxSize().background(CmpTheme.colors.backgroundPagePrimary)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIconButton(
                    onClick = actions.onViewerClose,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.mountpassport_photo_a11y_close),
                )
                CmpText(
                    text = stringResource(
                        Strings.string.mountpassport_photo_label_position,
                        pager.currentPage + 1,
                        photos.size,
                    ),
                    style = CmpTextStyle.Headline3,
                    modifier = Modifier.weight(1f),
                )
                CmpIconButton(
                    onClick = { actions.onDeleteRequest(current.id) },
                    icon = CmpIcons.Delete,
                    contentDescription = stringResource(Strings.string.mountpassport_photo_a11y_delete),
                )
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth(), key = { photos[it].id }) {
                CmpZoomableImage(
                    model = photos[it].path,
                    contentDescription = stringResource(Strings.string.mountpassport_editor_a11y_photo, it + 1),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            CmpTextField(
                value = current.caption.orEmpty(),
                onValueChange = { actions.onCaptionChange(current.id, it) },
                label = stringResource(Strings.string.mountpassport_photo_label_caption),
                placeholder = stringResource(Strings.string.mountpassport_photo_placeholder_caption),
                singleLine = true,
                supportingText = {
                    CmpText(
                        text = stringResource(Strings.string.mountpassport_photo_hint_caption),
                        style = CmpTextStyle.Additional1,
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
            )
        }
        if (confirmDelete) {
            DeleteConfirmDialog(
                title = stringResource(Strings.string.mountpassport_photo_title_delete, pager.currentPage + 1),
                text = stringResource(Strings.string.mountpassport_photo_msg_delete),
                onConfirm = actions.onDeleteConfirm,
                onDismiss = actions.onDeleteDismiss,
                confirmTag = MountPassportTags.DELETE_CONFIRM,
            )
        }
    }
}
