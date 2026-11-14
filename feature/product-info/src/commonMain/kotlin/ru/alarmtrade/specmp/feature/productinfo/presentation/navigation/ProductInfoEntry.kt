@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.productinfo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.barcode.isBarcodeScanAvailable
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.productinfo_common_msg_copied
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoActions
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoArgs
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoEffect
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoScreen
import ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product.ProductInfoViewModel

/**
 * Экран поверх текущего: с главного — без номера; из расширенного монтажа и ассистента — [query] = ID блока и
 * [connectedId] (пометка «Подключённый блок», пока на экране этот товар).
 */
@Serializable
@SerialName("product-info.ProductInfoRoute")
data class ProductInfoRoute(val query: String? = null, val connectedId: Long? = null) : NavKey

@Composable
fun ProductInfoEntry(onBack: () -> Unit, query: String? = null, connectedId: Long? = null) {
    val viewModel = koinViewModel<ProductInfoViewModel> { parametersOf(ProductInfoArgs(query, connectedId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val currentOnBack by rememberUpdatedState(onBack)

    @Suppress("DEPRECATION") // LocalClipboard требует платформенный ClipEntry; для текста хватает менеджера
    val clipboard = LocalClipboardManager.current

    val actions = remember(viewModel, clipboard) {
        ProductInfoActions(
            onBack = { currentOnBack() },
            onQueryChange = viewModel::onQueryChange,
            onClear = viewModel::onClear,
            onSearch = viewModel::onSearch,
            onRetry = viewModel::onRetry,
            onToggleComponent = viewModel::onToggleComponent,
            onToggleAll = viewModel::onToggleAll,
            onCopy = { value ->
                clipboard.setText(AnnotatedString(value))
                scope.launch { snackbar.showSnackbar(getString(Strings.string.productinfo_common_msg_copied, value)) }
            },
            onScan = viewModel::onScanClick,
            onScanAccessRetry = viewModel::onScanAccessRetry,
            onScanOpenSettings = viewModel::onScanOpenSettings,
            onScanned = viewModel::onScanned,
            onScanClose = viewModel::onScanClose,
            onScanManual = viewModel::onScanManual,
        )
    }
    val focusRequester = remember { FocusRequester() }
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            ProductInfoEffect.FocusQuery -> focusRequester.requestFocus()
        }
    }

    ProductInfoScreen(
        state = state,
        actions = actions,
        snackbar = snackbar,
        scanAvailable = isBarcodeScanAvailable,
        focusRequester = focusRequester,
    )
}
