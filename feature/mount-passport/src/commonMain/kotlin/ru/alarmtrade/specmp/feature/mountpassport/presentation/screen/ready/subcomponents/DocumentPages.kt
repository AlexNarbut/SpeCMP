package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_a11y_page
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_hint_forming
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_label_page
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_loading_forming
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_loading_refresh
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_msg_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_error
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpImage
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPdf
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

private const val A4_RATIO = 210f / 297f
private val SheetGap = 20.dp
private val SheetMinWidth = 160.dp
private val SheetBorder = 1.dp
private val SkeletonLines = listOf(0.46f, 0.3f, 0f, 1f, 1f, 1f, 0.82f, 0f, 1f, 1f, 0.64f)
private val SkeletonLine = 8.dp

/**
 * Листы документа картинками на сером поле: лист рисуется, когда попадает на экран (`onPageVisible`); над листами —
 * «Стр. N из M», при пересборке — «Обновляем…». Формирование — пустой лист и карточка, ошибка — «Повторить».
 */
@Composable
internal fun DocumentPages(
    state: PassportReadyState,
    actions: PassportReadyActions,
    maxSheetWidth: Dp,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(CmpTheme.colors.backgroundSurfaceSecondary)) {
        val sheetWidth = (maxWidth - SheetGap * 2).coerceIn(SheetMinWidth, maxSheetWidth)
        val widthPx = with(LocalDensity.current) { sheetWidth.roundToPx() }
        val pdf = state.pdf
        when {
            state.document is DocumentStatus.Failed && pdf == null -> ErrorState(actions.onRetry)
            pdf == null -> Forming(sheetWidth)
            else -> Sheets(state, pdf, sheetWidth, widthPx, actions.onPageVisible, bottomInset)
        }
        if (state.refreshing) {
            Chip(
                text = stringResource(Strings.string.mountpassport_ready_loading_refresh),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = CmpSpacing.space12),
                inverse = true,
            )
        }
    }
}

@Composable
private fun Sheets(
    state: PassportReadyState,
    pdf: PassportPdf,
    sheetWidth: Dp,
    widthPx: Int,
    onPageVisible: (Int, Int) -> Unit,
    bottomInset: Dp,
) {
    val listState = rememberLazyListState()
    val current by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SheetGap),
            contentPadding = PaddingValues(top = SheetGap, bottom = SheetGap + bottomInset),
        ) {
            items(count = pdf.pageCount, key = { it }) { index ->
                Sheet(pdf, index, state.pages[index], sheetWidth, widthPx, onPageVisible)
            }
        }
        if (pdf.pageCount > 1) {
            Chip(
                text = stringResource(Strings.string.mountpassport_ready_label_page, current, pdf.pageCount),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = CmpSpacing.space16 + bottomInset),
                inverse = true,
            )
        }
    }
}

@Composable
private fun Sheet(
    pdf: PassportPdf,
    index: Int,
    bitmap: ImageBitmap?,
    width: Dp,
    widthPx: Int,
    onPageVisible: (Int, Int) -> Unit,
) {
    val colors = CmpTheme.colors
    val currentVisible by rememberUpdatedState(onPageVisible)
    LaunchedEffect(pdf, index, widthPx) { currentVisible(index, widthPx) }
    Box(
        modifier = Modifier
            .width(width)
            .aspectRatio(A4_RATIO)
            .background(colors.staticWhite)
            .border(SheetBorder, colors.borderHighlight)
            .testTag(MountPassportTags.page(index)),
    ) {
        if (bitmap != null) {
            CmpImage(
                painter = remember(bitmap) { BitmapPainter(bitmap) },
                contentDescription = stringResource(Strings.string.mountpassport_ready_a11y_page, index + 1),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            SkeletonText()
        }
    }
}

@Composable
private fun Forming(sheetWidth: Dp) {
    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .padding(top = SheetGap)
                .width(sheetWidth)
                .aspectRatio(A4_RATIO)
                .background(CmpTheme.colors.staticWhite)
                .testTag(MountPassportTags.FORMING_SHEET),
        ) { SkeletonText() }
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .background(CmpTheme.colors.backgroundPagePrimary, RoundedCornerShape(CmpRadius.cardMedium))
                .border(SheetBorder, CmpTheme.colors.borderHighlight, RoundedCornerShape(CmpRadius.cardMedium))
                .padding(horizontal = CmpSpacing.space20, vertical = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSpacing.space24), strokeWidth = 3.dp)
            Column {
                CmpText(
                    text = stringResource(Strings.string.mountpassport_ready_loading_forming),
                    style = CmpTextStyle.Headline3,
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_ready_hint_forming),
                    style = CmpTextStyle.Paragraph3,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
    }
}

@Composable
private fun SkeletonText() {
    Column(
        modifier = Modifier.fillMaxSize().padding(CmpSpacing.space24),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        SkeletonLines.forEach { fraction ->
            if (fraction == 0f) {
                Box(Modifier.height(SkeletonLine))
            } else {
                CmpSkeleton(modifier = Modifier.fillMaxWidth(fraction).height(SkeletonLine))
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(CmpTheme.colors.backgroundPagePrimary),
        contentAlignment = Alignment.Center,
    ) {
        CmpStateBox(
            icon = CmpIcons.ErrorOutline,
            title = stringResource(Strings.string.mountpassport_ready_title_error),
            text = stringResource(Strings.string.mountpassport_ready_msg_error),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_common_btn_retry),
                onClick = onRetry,
                icon = CmpIcons.Refresh,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(MountPassportTags.READY_RETRY),
            )
        }
    }
}

@Composable
private fun Chip(text: String, modifier: Modifier = Modifier, inverse: Boolean = false) {
    val colors = CmpTheme.colors
    Box(
        modifier = modifier
            .background(
                if (inverse) colors.foregroundOnDarkPrimaryDefault else colors.backgroundSurfaceCard,
                RoundedCornerShape(CmpRadius.round),
            )
            .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
    ) {
        CmpText(
            text = text,
            style = CmpTextStyle.Additional0,
            color = if (inverse) colors.backgroundPagePrimary else colors.foregroundOnDarkPrimaryDefault,
        )
    }
}
