@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class CmpFabSize { Small, Default, Large }

@Composable
fun CmpFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: CmpFabSize = CmpFabSize.Default,
    content: @Composable () -> Unit
) {
    when (size) {
        CmpFabSize.Small -> SmallFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            content = content
        )

        CmpFabSize.Default -> FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            content = content
        )

        CmpFabSize.Large -> LargeFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            content = content
        )
    }
}
