package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.image.CmpZoomableImage
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
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
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.CloudMeta
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailPane
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailWideFrom
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.RatingLabel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.rememberDateTimeText
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.ModelFilterChip
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.ConfigurationInfoDialog

/**
 * K4 список облачных конфигураций автомобиля: «Лучшие / Новые», фильтр по модели блока, карточки с оценкой.
 * Широкое окно (≥ 600 dp) — список слева, карточка выбранной справа ([detail]).
 */
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
    snackbarHostState: CmpSnackbarHostState,
    detail: @Composable (id: Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= ListDetailWideFrom
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = { CmpText(text = stringResource(Strings.string.configurator_cloud_title)) },
                    subtitle = { CmpText(text = carTitle, style = CmpTextStyle.Caption, maxLines = 1) },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.configurator_common_a11y_back)
                        )
                    }
                )
            },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            ListDetailPane(
                wide,
                detail = { state.selectedId?.let { detail(it) } },
                modifier = Modifier.padding(padding)
            ) {
                CloudToolbar(state, onOrder, onModelFilter)
                CloudListContent(state, wide, onRetry, onOpen, onSelect)
            }
        }
    }
}

@Composable
private fun CloudToolbar(
    state: CloudListState,
    onOrder: (CloudConfigurationOrder) -> Unit,
    onModelFilter: (String?) -> Unit
) {
    CmpFlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            stringResource(Strings.string.configurator_cloud_value_order_best),
            stringResource(Strings.string.configurator_cloud_value_order_new)
        )
        CmpSegmentedButton(
            options = options,
            selectedIndex = state.order.ordinal,
            onSelect = { index -> onOrder(CloudConfigurationOrder.entries[index]) }
        )
        if (state.models.size > 1 || state.modelFilter != null) {
            ModelFilterChip(
                models = (state.models + listOfNotNull(state.modelFilter)).distinct(),
                selected = state.modelFilter,
                onSelect = onModelFilter
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
    onSelect: (Int) -> Unit
) {
    when (val list = state.list) {
        LCEState.None, LCEState.Loading -> CmpBox(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CmpCircularProgressIndicator()
        }

        is LCEState.Error -> CmpStateBox(
            icon = CmpIcons.CloudOff,
            title = list.error.text(Strings.string.configurator_cloud_error),
            text = stringResource(Strings.string.configurator_cloud_error_msg),
            tone = CmpStateTone.Error,
            modifier = Modifier.fillMaxSize()
        ) {
            CmpButton(
                text = stringResource(Strings.string.configurator_common_btn_retry),
                onClick = onRetry,
                modifier = Modifier.testTag(ConfiguratorTestTags.RETRY)
            )
        }

        is LCEState.Content -> {
            val items = state.shown
            if (items.isEmpty()) {
                CmpStateBox(
                    icon = CmpIcons.Inventory2,
                    title = stringResource(Strings.string.configurator_cloud_title_empty),
                    text = state.modelFilter?.let { stringResource(Strings.string.configurator_cloud_msg_empty, it) }
                        ?: stringResource(Strings.string.configurator_cloud_msg_empty_all),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val dateText = rememberDateTimeText()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        CloudItemCard(
                            item = item,
                            selected = wide && item.id == state.selectedId,
                            dateText = dateText,
                            onClick = { if (wide) onSelect(item.id) else onOpen(item) }
                        )
                    }
                }
            }
        }
    }
}

/** Действия карточки облачной конфигурации. Пустое действие — кнопки нет. */
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
    /** Выбор для загрузки в редактор блока (режим выбора). */
    val onLoad: ((CloudConfiguration) -> Unit)?,
    /** «Открыть настройки» — просмотр в редакторе. */
    val onOpenSettings: ((CloudConfiguration) -> Unit)?,
    /** «Записать в блок» (при подключённом блоке). */
    val onWrite: ((CloudConfiguration) -> Unit)?,
    /** Переход к обновлению прошивки при несовпадении CAN-кода. */
    val onOpenFirmwareUpdate: (() -> Unit)?,
    /** Выбор для быстрого монтажа: [onLoad] — «Выбрать», без «Сохранить в мои». */
    val selectForMount: Boolean = false
)

/** Экран карточки (телефон): шапка с «назад» и [CloudCardContent]. */
@Composable
internal fun CloudCardScreen(
    state: CloudCardState,
    title: String,
    onBack: () -> Unit,
    actions: CloudCardActions,
    snackbarHostState: CmpSnackbarHostState
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.configurator_common_a11y_back)
                    )
                }
            )
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        CmpBox(modifier = Modifier.fillMaxSize().padding(padding)) { CloudCardContent(state, actions) }
    }
}

/**
 * Карточка облачной конфигурации: шапка (название, описание, автомобиль, модель, автор, дата, оценка), полное описание
 * («Показать полностью»), CAN-код и предупреждение о прошивке, монтажная карта, вложения, действия.
 */
@Composable
internal fun CloudCardContent(state: CloudCardState, actions: CloudCardActions) {
    CmpBox(modifier = Modifier.fillMaxSize()) {
        when (val card = state.card) {
            LCEState.None, LCEState.Loading -> CmpBox(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CmpCircularProgressIndicator()
            }

            is LCEState.Error -> CmpStateBox(
                icon = CmpIcons.CloudOff,
                title = card.error.text(Strings.string.configurator_card_error),
                text = stringResource(Strings.string.configurator_cloud_error_msg),
                tone = CmpStateTone.Error,
                modifier = Modifier.fillMaxSize()
            ) {
                CmpButton(
                    text = stringResource(Strings.string.configurator_common_btn_retry),
                    onClick = actions.onRetry
                )
            }

            is LCEState.Content -> CardBody(state, card.content, actions)
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
                onDismiss = actions.onSaveDismiss
            )
        }
        if (state.busy) BusyOverlay()
    }
}

@Composable
private fun CardBody(state: CloudCardState, card: CloudConfiguration, actions: CloudCardActions) {
    val colors = cmpThemeColors()
    val summary = card.summary
    val dateText = rememberDateTimeText()
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpRow(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpText(text = summary.name, style = CmpTextStyle.Title, modifier = Modifier.weight(1f))
            RatingLabel(summary.rating)
        }
        if (summary.shortDescription.isNotBlank()) {
            CmpText(text = summary.shortDescription, style = CmpTextStyle.BodyLarge, color = colors.onSurfaceVariant)
        }
        CloudMeta(summary, dateText)
        if (card.fullDescriptionHtml.isNotBlank()) {
            CmpHtmlText(
                html = card.fullDescriptionHtml,
                maxLines = if (state.descriptionExpanded) Int.MAX_VALUE else DESCRIPTION_COLLAPSED_LINES
            )
            CmpTextButton(
                text = stringResource(
                    if (state.descriptionExpanded) {
                        Strings.string.configurator_card_btn_less
                    } else {
                        Strings.string.configurator_card_btn_more
                    }
                ),
                onClick = actions.onDescriptionToggle
            )
        }
        CanBlock(state, actions)
        card.mountingManual?.let { manual ->
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_card_title_manual),
                onClick = { actions.onAttachmentOpen(manual) },
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_MANUAL)
            )
        }
        if (card.attachments.isNotEmpty()) {
            CmpText(
                text = stringResource(Strings.string.configurator_card_title_attachments),
                style = CmpTextStyle.BodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                card.attachments.forEachIndexed { index, attachment ->
                    AttachmentTile(attachment, index) { actions.onAttachmentOpen(attachment) }
                }
            }
        }
        CardActions(state, card, actions)
    }
}

@Composable
private fun CanBlock(state: CloudCardState, actions: CloudCardActions) {
    val code = state.canCode ?: return
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(text = stringResource(Strings.string.configurator_card_label_can, code), style = CmpTextStyle.BodyLarge)
        if (state.canMismatch) CanMismatch(state, code, actions)
    }
}

@Composable
private fun CanMismatch(state: CloudCardState, code: String, actions: CloudCardActions) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = Modifier.fillMaxWidth().background(colors.errorContainer, CardShape).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpIcon(icon = CmpIcons.Memory, contentDescription = null, tint = colors.onErrorContainer)
            CmpText(
                text = stringResource(
                    Strings.string.configurator_card_msg_can_mismatch,
                    state.firmwareCanCode.orEmpty(),
                    code
                ),
                color = colors.onErrorContainer
            )
        }
        actions.onOpenFirmwareUpdate?.let { open ->
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_firmware),
                onClick = open,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_FIRMWARE)
            )
        }
    }
}

@Composable
private fun CardActions(state: CloudCardState, card: CloudConfiguration, actions: CloudCardActions) {
    CmpFlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.onLoad?.let { load ->
            CmpButton(
                text = stringResource(
                    if (actions.selectForMount) {
                        Strings.string.configurator_card_btn_select
                    } else {
                        Strings.string.configurator_card_btn_load
                    }
                ),
                onClick = { load(card) },
                enabled = !state.canMismatch,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_LOAD)
            )
        }
        actions.onWrite?.takeIf { state.connectedDeviceName != null }?.let { write ->
            CmpButton(
                text = stringResource(Strings.string.configurator_card_btn_write),
                onClick = { write(card) },
                enabled = !state.canMismatch,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_WRITE)
            )
        }
        actions.onOpenSettings?.let { open ->
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_card_btn_open),
                onClick = { open(card) },
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_OPEN)
            )
        }
        if (!actions.selectForMount) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_card_btn_save_mine),
                onClick = actions.onSaveRequest,
                modifier = Modifier.testTag(ConfiguratorTestTags.CARD_SAVE_MINE)
            )
        }
    }
}

/** Превью вложения: картинка или значок PDF, подпись (2 строки). */
@Composable
private fun AttachmentTile(attachment: ConfigurationAttachment, index: Int, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    val label = attachment.title.ifBlank { attachment.url.substringAfterLast('/') }
    val description = stringResource(Strings.string.configurator_common_a11y_attachment, label)
    CmpColumn(
        modifier = Modifier
            .width(TileWidth)
            .clip(CardShape)
            .clickable(onClick = onClick)
            .testTag(ConfiguratorTestTags.attachment(index)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CmpBox(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(colors.surfaceContainerHigh, CardShape),
            contentAlignment = Alignment.Center
        ) {
            val preview = attachment.previewUrl
            if (attachment.isPdf || preview == null) {
                CmpIcon(
                    icon = if (attachment.isPdf) CmpIcons.PictureAsPdf else CmpIcons.Image,
                    contentDescription = description,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                CmpAsyncImage(
                    model = preview,
                    contentDescription = description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CardShape)
                )
            }
        }
        CmpText(text = label, style = CmpTextStyle.Caption, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Просмотр картинки-вложения с зумом. */
@Composable
private fun AttachmentViewer(attachment: ConfigurationAttachment, onClose: () -> Unit) {
    CmpFullScreenDialog(onDismissRequest = onClose) {
        CmpBox(modifier = Modifier.fillMaxSize().background(cmpThemeColors().scrim)) {
            CmpZoomableImage(
                model = attachment.url,
                contentDescription = attachment.title,
                modifier = Modifier.fillMaxSize()
            )
            CmpIconButton(
                onClick = onClose,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.configurator_common_btn_close),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            )
        }
    }
}

private const val DESCRIPTION_COLLAPSED_LINES = 4
private val CardShape = RoundedCornerShape(12.dp)
private val TileWidth = 120.dp
