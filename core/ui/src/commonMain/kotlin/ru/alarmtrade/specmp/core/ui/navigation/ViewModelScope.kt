package ru.alarmtrade.specmp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ViewModel поддерева [content] живут, пока не сменится [scopeKey], а не пока поддерево в композиции.
 *
 * Уход поддерева из композиции (смена экрана авторизации) VM не очищает — иначе их не пережил бы и поворот экрана.
 * Поэтому границу задаёт ключ: новый [scopeKey] (например, номер сессии аккаунта) очищает VM прежнего, а пересоздание
 * Activity при том же ключе их сохраняет. Включая VM записей nav3 (`rememberViewModelStoreNavEntryDecorator` хранит
 * их в ближайшем [LocalViewModelStoreOwner]).
 */
@Composable
fun ViewModelScope(scopeKey: Any, content: @Composable () -> Unit) {
    val stores = viewModel { ScopedViewModelStores() }
    CompositionLocalProvider(LocalViewModelStoreOwner provides stores.ownerFor(scopeKey), content = content)
}

/** Хранилище текущего ключа; живёт в ViewModelStore родителя (Activity), поэтому переживает пересоздание. */
private class ScopedViewModelStores : ViewModel() {
    private var key: Any? = null
    private var owner: ViewModelStoreOwner = newOwner()

    fun ownerFor(scopeKey: Any): ViewModelStoreOwner {
        if (scopeKey != key) {
            owner.viewModelStore.clear()
            owner = newOwner()
            key = scopeKey
        }
        return owner
    }

    override fun onCleared() = owner.viewModelStore.clear()

    private fun newOwner(): ViewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
}
