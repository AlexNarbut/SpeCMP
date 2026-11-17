package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_firmware
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_less
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_load
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_more
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_open
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_save_mine
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_select
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_btn_write
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_error
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_label_can
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_msg_can_mismatch
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_title_attachments
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_title_manual
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_error
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_error_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_format_meta
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_msg_empty_all
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_title_empty
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_value_order_best
import ru.alarmtrade.specmp.core.localization.resources.configurator_cloud_value_order_new
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_attachment
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_retry
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.image.CmpZoomableImage
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpHtmlText
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationOrder
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.BusyOverlay
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.CloudItemCard
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailPane
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailWideFrom
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.OnlyAuthorTag
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.RatingLabel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.SkeletonCards
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.rememberDateTimeText
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.ModelFilterChip
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationInfoDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ModelBadge

@Composable
internal fun CloudListScreen(
    state: CloudListState,
    carTitle: String,
    onBack: () -> Unit,
    onOrder: (CloudConfigurationOrder) -> Unit,
    onModelFilter: (String?) -> Unit,
    onRetry: () -> Unit,
    onOpen: (CloudConfigurationSummary) -> Unit,
    onSelect: (Int) -> Unit,
    onListWidthChange: (Int) -> Unit,
    snackbarHostState: CmpSnackbarHostState,
    detail: @Composable (id: Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= ListDetailWideFrom
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = stringResource(Strings.string.configurator_cloud_title),
                    subtitle = carTitle,
                    navigationIcon = { BackButton(onBack) },
                )
            },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            ListDetailPane(
                wide = wide,
                listWidthDp = state.listWidthDp,
                onListWidthChange = onListWidthChange,
                detail = { state.selectedId?.let { detail(it) } },
                modifier = Modifier.padding(padding),
            ) {
                CloudToolbar(state, onOrder, onModelFilter)
                CloudListContent(state, wide, onRetry, onOpen, onSelect)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    CmpIconButton(
        onClick = onBack,
        icon = CmpIcons.ArrowBack,
        contentDescription = stringResource(Strings.string.configurator_common_a11y_back),
    )
}

@Composable
private fun CloudToolbar(
    state: CloudListState,
    onOrder: (CloudConfigurationOrder) -> Unit,
    onModelFilter: (String?) -> Unit,
) {
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth().padding(
            start = CmpSpacing.screenPaddingH,
            end = CmpSpacing.screenPaddingH,
            top = CmpSpacing.space4,
            bottom = CmpSpacing.space8,
        ),
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        val options = listOf(
            stringResource(Strings.string.configurator_cloud_value_order_best),
            stringResource(Strings.string.configurator_cloud_value_order_new),
        )
        CmpSegmentedButton(
            options = options,
            selectedIndex = state.order.ordinal,
            onSelect = { index -> onOrder(CloudConfigurationOrder.entries[index]) },
        )
        if (state.models.size > 1 || state.modelFilter != null) {
            ModelFilterChip(
                models = (state.models + listOfNotNull(state.modelFilter)).distinct(),
                selected = state.modelFilter,
                onSelect = onModelFilter,
            )
        }
    }
}

@Composable
private fun CloudListContent(
    state: CloudListState,
    wide: Boolean,
    onRetry: () -> Unit,
    onOpen: (CloudConfigurationSummary) -> Unit,
    onSelect: (Int) -> Unit,
) {
    when (val list = state.list) {
        LCEState.None, LCEState.Loading -> SkeletonCards(SKELETON_ROWS, cardHeight = CloudSkeletonHeight)

        is LCEState.Error -> CloudLoadError(
            error = list.error,
            title = Strings.string.configurator_cloud_error,
            onRetry = onRetry,
            retryModifier = Modifier.testTag(ConfiguratorTestTags.RETRY),
        )

        is LCEState.Content -> {
            val items = state.shown
            if (items.isEmpty()) {
                CmpStateBox(
                    icon = CmpIcons.Inventory2,
                    title = stringResource(Strings.string.configurator_cloud_title_empty),
                    text = state.modelFilter?.let { stringResource(Strings.string.configurator_cloud_msg_empty, it) }
                        ?: stringResource(Strings.string.configurator_cloud_msg_empty_all),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val dateText = rememberDateTimeText()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = CmpSpacing.screenPaddingH,
                        end = CmpSpacing.screenPaddingH,
                        bottom = CmpSpacing.screenPaddingBottom,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                ) {
                    items(items, key = { it.id }) { item ->
                        CloudItemCard(
                            item = item,
                            selected = wide && item.id == state.selectedId,
                            dateText = dateText,
                            onClick = { if (wide) onSelect(item.id) else onOpen(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudLoadError(
    error: ConfiguratorError,
    title: StringResource,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryModifier: Modifier = Modifier,
) {
    val offline = error == ConfiguratorError.Common(ApiError.NoConnection)
    CmpStateBox(
        icon = if (offline) CmpIcons.WifiOff else CmpIcons.CloudOff,
        title = error.text(title),
        text = stringResource(Strings.string.configurator_cloud_error_msg),
        tone = if (offline) CmpStateTone.Neutral else CmpStateTone.Error,
        modifier = modifier.fillMaxSize(),
    ) {
        CmpButton(
            text = stringResource(Strings.string.configurator_common_btn_retry),
            onClick = onRetry,
            icon = CmpIcons.Refresh,
            modifier = retryModifier,
        )
    }
}

/** Пустое действие — кнопки нет. */
@Immutable
internal class CloudCardActions(
    val onRetry: () -> Unit,
    val onDescriptionToggle: () -> Unit,
    val onAttachmentOpen: (ConfigurationAttachment) -> Unit,
    val onViewerClose: () -> Unit,
    val onSaveRequest: () -> Unit,
    val onSaveChange: (String, String) -> Unit,
    val onSaveSubmit: () -> Unit,
    val onSaveDismiss: () -> Unit,
    val onLoad: ((CloudConfiguration) -> Unit)?,
    val onOpenSettings: ((CloudConfiguration) -> Unit)?,
    val onWrite: ((CloudConfiguration) -> Unit)?,
    val onOpenFirmwareUpdate: (() -> Unit)?,
    /** Выбор для быстрого монтажа: [onLoad] — «Выбрать», без «Сохранить в мои». */
    val selectForMount: Boolean = false,
)

@Composable
internal fun CloudCardScreen(
    state: CloudCardState,
    title: String,
    onBack: () -> Unit,
    actions: CloudCardActions,
    snackbarHostState: CmpSnackbarHostState,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < CmpBreakpoints.Medium
        CmpScaffold(
            topBar = { CmpTopAppBar(title = title, navigationIcon = { BackButton(onBack) }) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            CloudCardContent(state, actions, contentPadding = padding, pinnedActions = compact)
        }
    }
}

/** [contentPadding] — отступы каркаса: текст карточки прокручивается под прозрачным топбаром. */
@Composable
internal fun CloudCardContent(
    state: CloudCardState,
    actions: CloudCardActions,
    contentPadding: PaddingValues = PaddingValues(),
    pinnedActions: Boolean = false,
) {
    CmpBox(modifier = Modifier.fillMaxSize()) {
        when (val card = state.card) {
            LCEState.None, LCEState.Loading -> CmpBox(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CmpCircularProgressIndicator()
            }

            is LCEState.Error -> CloudLoadError(
                error = card.error,
                title = Strings.string.configurator_card_error,
                onRetry = actions.onRetry,
                modifier = Modifier.padding(contentPadding),
            )

            is LCEState.Content -> CardBody(state, card.content, actions, contentPadding, pinnedActions)
        }
        state.viewer?.let { AttachmentViewer(it, actions.onViewerClose) }
        state.saveForm?.let { form ->
            ConfigurationInfoDialog(
                title = stringResource(Strings.string.configurator_card_btn_save_mine),
                name = form.name,
                description = form.description,
                deviceName = state.card.content?.summary?.deviceName,
                onChange = actions.onSaveChange,
                onConfirm = actions.onSaveSubmit,
                onDismiss = actions.onSaveDismiss,
            )
        }
        if (state.busy) BusyOverlay()
    }
}

@Composable
private fun CardBody(
    state: CloudCardState,
    card: CloudConfiguration,
    actions: CloudCardActions,
    contentPadding: PaddingValues,
    pinnedActions: Boolean,
) {
    val horizontal = if (pinnedActions) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium
    val bottomInset = contentPadding.calculateBottomPadding()
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = horizontal,
                    end = horizontal,
                    top = contentPadding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                    bottom = CmpSpacing.screenPaddingBottom + if (pinnedActions) 0.dp else bottomInset,
                ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CardHead(card.summary)
            CardMeta(state, card.summary)
            if (state.canMismatch) CanMismatch(state, actions, fullWidth = pinnedActions)
            if (!pinnedActions) CardActions(state, card, actions)
            CardDescription(state, card, actions)
            CardAttachments(card, actions)
        }
        if (pinnedActions) CmpActionBar { CardActions(state, card, actions) }
    }
}

@Composable
private fun CardHead(summary: CloudConfigurationSummary) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpFlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            ModelBadge(summary.deviceName)
            RatingLabel(summary.rating)
            if (summary.onlyForAuthor) OnlyAuthorTag()
        }
        CmpText(text = summary.name, style = CmpTextStyle.Headline1)
        if (summary.shortDescription.isNotBlank()) {
            CmpText(
                text = summary.shortDescription,
                style = CmpTextStyle.Paragraph1,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun CardMeta(state: CloudCardState, summary: CloudConfigurationSummary) {
    val dateText = rememberDateTimeText()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        MetaRow(CmpIcons.DirectionsCar, summary.car.title)
        MetaRow(
            CmpIcons.Person,
            stringResource(Strings.string.configurator_cloud_format_meta, summary.author, dateText(summary.updated)),
        )
        state.canCode?.let { MetaRow(CmpIcons.Memory, stringResource(Strings.string.configurator_card_label_can, it)) }
    }
}

@Composable
private fun MetaRow(icon: CmpIcons, text: String) {
    CmpRow(
        modifier = Modifier.heightIn(min = CmpSize.tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconM),
        )
        CmpText(text = text, style = CmpTextStyle.Paragraph2)
    }
}

@Composable
private fun CanMismatch(state: CloudCardState, actions: CloudCardActions, fullWidth: Boolean) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.errorDefault.copy(alpha = ALERT_BORDER_ALPHA), shape)
            .padding(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpIcon(
                icon = CmpIcons.Warning,
                contentDescription = null,
                tint = colors.errorDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
            CmpText(
                text = stringResource(
                    Strings.string.configurator_card_msg_can_mismatch,
                    state.firmwareCanCode.orEmpty(),
                    state.canCode.orEmpty(),
                ),
                style = CmpTextStyle.Paragraph1,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
        }
        actions.onOpenFirmwareUpdate?.let { open ->
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_firmware),
                onClick = open,
                type = CmpButtonType.Danger,
                size = CmpButtonSize.M,
                fullWidth = fullWidth,
                maxLines = 2,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_FIRMWARE),
            )
        }
    }
}

@Composable
private fun CardActions(state: CloudCardState, card: CloudConfiguration, actions: CloudCardActions) {
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        actions.onLoad?.let { load ->
            CmpButton(
                text = stringResource(
                    if (actions.selectForMount) {
                        Strings.string.configurator_card_btn_select
                    } else {
                        Strings.string.configurator_card_btn_load
                    },
                ),
                onClick = { load(card) },
                enabled = !state.canMismatch,
                icon = CmpIcons.Download,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_LOAD),
            )
        }
        actions.onWrite?.takeIf { state.connectedDeviceName != null }?.let { write ->
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_write),
                onClick = { write(card) },
                enabled = !state.canMismatch,
                icon = CmpIcons.Upload,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_WRITE),
            )
        }
        actions.onOpenSettings?.let { open ->
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_open),
                onClick = { open(card) },
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Visibility,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_OPEN),
            )
        }
        if (!actions.selectForMount) {
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_save_mine),
                onClick = actions.onSaveRequest,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Inventory2,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_SAVE_MINE),
            )
        }
    }
}

@Composable
private fun CardDescription(state: CloudCardState, card: CloudConfiguration, actions: CloudCardActions) {
    if (card.fullDescriptionHtml.isBlank()) return
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpHtmlText(
            html = card.fullDescriptionHtml,
            maxLines = if (state.descriptionExpanded) Int.MAX_VALUE else DESCRIPTION_COLLAPSED_LINES,
        )
        CmpButton(
            text = stringResource(
                if (state.descriptionExpanded) {
                    Strings.string.configurator_card_btn_less
                } else {
                    Strings.string.configurator_card_btn_more
                },
            ),
            onClick = actions.onDescriptionToggle,
            type = CmpButtonType.Tertiary,
            size = CmpButtonSize.S,
            icon = if (state.descriptionExpanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
        )
    }
}

@Composable
private fun CardAttachments(card: CloudConfiguration, actions: CloudCardActions) {
    card.mountingManual?.let { manual ->
        CmpListItem(
            title = stringResource(Strings.string.configurator_card_title_manual),
            icon = CmpIcons.PictureAsPdf,
            onClick = { actions.onAttachmentOpen(manual) },
            trailing = {
                CmpIcon(
                    icon = CmpIcons.OpenInNew,
                    contentDescription = null,
                    tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            },
            modifier = Modifier.fillMaxWidth().testTag(ConfiguratorTestTags.CARD_MANUAL),
        )
    }
    if (card.attachments.isEmpty()) return
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(Strings.string.configurator_card_title_attachments),
            style = CmpTextStyle.Headline2,
        )
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            card.attachments.forEachIndexed { index, attachment ->
                AttachmentTile(attachment, index) { actions.onAttachmentOpen(attachment) }
            }
        }
    }
}

@Composable
private fun AttachmentTile(attachment: ConfigurationAttachment, index: Int, onClick: () -> Unit) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    val label = attachment.title.ifBlank { attachment.url.substringAfterLast('/') }
    val description = stringResource(Strings.string.configurator_common_a11y_attachment, label)
    CmpColumn(
        modifier = Modifier
            .width(TileWidth)
            .cmpPressable(onClick = onClick, shape = shape)
            .testTag(ConfiguratorTestTags.attachment(index)),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TILE_ASPECT)
                .clip(shape)
                .background(colors.backgroundSurfaceHighlight)
                .border(CmpBorderWidth.listItem, colors.borderHighlight, shape),
            contentAlignment = Alignment.Center,
        ) {
            val preview = attachment.previewUrl
            if (attachment.isPdf || preview == null) {
                CmpIcon(
                    icon = if (attachment.isPdf) CmpIcons.PictureAsPdf else CmpIcons.Image,
                    contentDescription = description,
                    tint = if (attachment.isPdf) colors.errorDefault else colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            } else {
                CmpAsyncImage(
                    model = preview,
                    contentDescription = description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        CmpText(text = label, style = CmpTextStyle.Paragraph3, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AttachmentViewer(attachment: ConfigurationAttachment, onClose: () -> Unit) {
    val colors = CmpTheme.colors
    CmpFullScreenDialog(onDismissRequest = onClose) {
        CmpBox(modifier = Modifier.fillMaxSize().background(colors.staticBlack)) {
            CmpZoomableImage(
                model = attachment.url,
                contentDescription = attachment.title,
                modifier = Modifier.fillMaxSize(),
            )
            CmpIconButton(
                onClick = onClose,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.configurator_common_btn_close),
                tint = colors.staticWhite,
                modifier = Modifier.align(Alignment.TopEnd).padding(CmpSpacing.space8),
            )
        }
    }
}

private const val DESCRIPTION_COLLAPSED_LINES = 4
private const val SKELETON_ROWS = 4
private const val TILE_ASPECT = 4f / 3f
private const val ALERT_BORDER_ALPHA = 0.3f
private val CloudSkeletonHeight = CmpSize.rowSection + CmpSize.touchMin
private val TileWidth = 120.dp
