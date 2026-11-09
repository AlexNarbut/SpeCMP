package ru.alarmtrade.specmp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * VM поддерева живут до смены [scopeKey], а не пока оно в композиции — иначе их не пережил бы поворот экрана.
 * [name] — своё хранилище на зону: соседние зоны во время перехода не очищают VM друг друга.
 */
@Composable
fun ViewModelScope(name: String, scopeKey: Any, content: @Composable () -> Unit) {
    val stores = viewModel(key = name) { ScopedViewModelStores() }
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
