package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.productinfo_main_title_screen
import ru.alarmtrade.specmp.core.localization.resources.productinfo_result_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.productinfo_result_error_not_found
import ru.alarmtrade.specmp.core.localization.resources.productinfo_result_error_server
import ru.alarmtrade.specmp.core.localization.resources.productinfo_result_msg_not_found
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoError
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoTags
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.ComponentRows
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.ComponentTable
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.ComponentsHeader
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.EmptyHint
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.KitCard
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.ProductCard
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.SearchBar
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.subcomponents.SkeletonCard

private val MediumMaxWidth = 720.dp
private val ExpandedMaxWidth = 1040.dp
private const val PRODUCT_COLUMN_WEIGHT = 1.1f

/** Раскладка по ширине области: Compact — 16, Medium — 720 с отступом 24, Expanded — 1040 и две колонки. */
private enum class ProductInfoLayout(val maxWidth: Dp, val gutter: Dp) {
    Compact(Dp.Infinity, 16.dp),
    Medium(MediumMaxWidth, 24.dp),
    Expanded(ExpandedMaxWidth, 24.dp),
}

@Composable
internal fun ProductInfoScreen(
    state: ProductInfoState,
    actions: ProductInfoActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    CmpScaffold(
        modifier = modifier,
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.productinfo_main_title_screen)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.productinfo_common_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        CmpAdaptiveBox(modifier = Modifier.fillMaxSize().padding(padding)) { window ->
            val layout = when {
                window.atLeast(CmpWidthClass.Expanded) -> ProductInfoLayout.Expanded
                window.atLeast(CmpWidthClass.Medium) -> ProductInfoLayout.Medium
                else -> ProductInfoLayout.Compact
            }
            Column(modifier = Modifier.fillMaxSize()) {
                Centered(layout) {
                    SearchBar(
                        query = state.query,
                        error = state.queryError,
                        onQueryChange = actions.onQueryChange,
                        onClear = actions.onClear,
                        onSearch = actions.onSearch,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(
                        1f,
                    ).fillMaxWidth().hideKeyboardOnScroll().verticalScroll(rememberScrollState()),
                ) {
                    Centered(layout) {
                        Body(state, layout, actions, Modifier.padding(top = 8.dp, bottom = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Centered(layout: ProductInfoLayout, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = layout.maxWidth).fillMaxWidth().padding(horizontal = layout.gutter)) {
            content()
        }
    }
}

@Composable
private fun Body(state: ProductInfoState, layout: ProductInfoLayout, actions: ProductInfoActions, modifier: Modifier) {
    val wide = layout != ProductInfoLayout.Compact
    when (val result = state.result) {
        LCEState.None -> EmptyHint(wide = wide, modifier = modifier)
        LCEState.Loading -> Loading(layout, modifier)
        is LCEState.Content -> Result(result.content, state, layout, actions, modifier)
        is LCEState.Error -> ErrorView(result.error, actions.onRetry, modifier)
    }
}

@Composable
private fun Loading(layout: ProductInfoLayout, modifier: Modifier) {
    if (layout == ProductInfoLayout.Expanded) {
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonCard(wide = true, lines = 4, modifier = Modifier.weight(PRODUCT_COLUMN_WEIGHT))
            SkeletonCard(wide = true, lines = 3, modifier = Modifier.weight(1f))
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonCard(wide = false, lines = 4)
            SkeletonCard(wide = false, lines = 2)
        }
    }
}

@Composable
private fun Result(
    info: ProductInfo,
    state: ProductInfoState,
    layout: ProductInfoLayout,
    actions: ProductInfoActions,
    modifier: Modifier,
) {
    val expandedLayout = layout == ProductInfoLayout.Expanded
    val wide = layout != ProductInfoLayout.Compact
    val kit = info.kit
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 12.dp)) {
        if (expandedLayout && kit != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                ProductCard(
                    product = info.product,
                    connected = state.isConnectedUnit,
                    wide = true,
                    solo = false,
                    onCopy = actions.onCopy,
                    modifier = Modifier.weight(PRODUCT_COLUMN_WEIGHT),
                )
                KitCard(kit = kit, wide = true, onCopy = actions.onCopy, modifier = Modifier.weight(1f))
            }
        } else {
            ProductCard(
                product = info.product,
                connected = state.isConnectedUnit,
                wide = wide,
                solo = expandedLayout,
                onCopy = actions.onCopy,
            )
            kit?.let { KitCard(kit = it, wide = wide, onCopy = actions.onCopy) }
        }
        val components = kit?.components.orEmpty()
        if (components.isNotEmpty()) {
            ComponentsHeader(
                count = components.size,
                allExpanded = state.allExpanded,
                onToggleAll = if (expandedLayout) null else actions.onToggleAll,
            )
            val list = components.toImmutableList()
            if (expandedLayout) {
                ComponentTable(components = list, onCopy = actions.onCopy)
            } else {
                ComponentRows(
                    components = list,
                    expanded = state.expanded,
                    onToggle = actions.onToggleComponent,
                    onCopy = actions.onCopy,
                )
            }
        }
    }
}

@Composable
private fun ErrorView(error: ProductInfoError, onRetry: () -> Unit, modifier: Modifier) {
    val box = Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag(ProductInfoTags.ERROR)
    when (error) {
        is ProductInfoError.NotFound -> CmpStateBox(
            icon = CmpIcons.SearchOff,
            title = stringResource(Strings.string.productinfo_result_error_not_found, error.id.toString()),
            text = stringResource(Strings.string.productinfo_result_msg_not_found),
            modifier = modifier.then(box),
        )

        is ProductInfoError.Common -> CmpStateBox(
            icon = CmpIcons.CloudOff,
            title = error.error.message(),
            modifier = modifier.then(box),
        ) { RetryButton(onRetry) }

        ProductInfoError.Unknown -> CmpStateBox(
            icon = CmpIcons.ErrorOutline,
            title = stringResource(Strings.string.productinfo_result_error_server),
            modifier = modifier.then(box),
        ) { RetryButton(onRetry) }
    }
}

@Composable
private fun RetryButton(onRetry: () -> Unit) {
    CmpButton(
        text = stringResource(Strings.string.productinfo_result_btn_retry),
        onClick = onRetry,
        modifier = Modifier.testTag(ProductInfoTags.RETRY),
    )
}
