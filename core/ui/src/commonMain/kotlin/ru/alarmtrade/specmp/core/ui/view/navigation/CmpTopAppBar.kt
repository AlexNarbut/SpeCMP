@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

enum class CmpTopAppBarStyle { Small, CenterAligned }

private val EdgeInset = 8.dp
private val SlotGap = 8.dp

// title() вызывается в двух взаимоисключающих ветках слота (с подзаголовком и без) — вынос сменил бы структуру узлов
@Suppress("ktlint:compose:content-slot-reused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmpTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    colors: TopAppBarColors = cmpTopAppBarColors(containerColor, contentColor),
    style: CmpTopAppBarStyle = CmpTopAppBarStyle.Small,
) {
    val titleSlot: @Composable () -> Unit = if (subtitle != null) {
        {
            Column(modifier = Modifier.padding(start = SlotGap)) {
                title()
                subtitle()
            }
        }
    } else {
        {
            Box(modifier = Modifier.padding(start = SlotGap)) {
                title()
            }
        }
    }

    val navigationIconSlot: @Composable () -> Unit = {
        Box(modifier = Modifier.padding(start = EdgeInset)) {
            navigationIcon()
        }
    }

    val actionsSlot: @Composable RowScope.() -> Unit = {
        Row(
            modifier = Modifier.padding(start = SlotGap, end = EdgeInset),
            horizontalArrangement = Arrangement.spacedBy(SlotGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }

    // Insets'ами (status bar) владеет CmpScaffold через topBar-слот — обнуляем здесь,
    // чтобы не было двойного верхнего отступа.
    val zeroInsets = WindowInsets(0, 0, 0, 0)

    when (style) {
        CmpTopAppBarStyle.Small -> TopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationIconSlot,
            actions = actionsSlot,
            windowInsets = zeroInsets,
            colors = colors,
        )

        CmpTopAppBarStyle.CenterAligned -> CenterAlignedTopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationIconSlot,
            actions = actionsSlot,
            windowInsets = zeroInsets,
            colors = colors,
        )
    }
}

/** Топбар со строковым заголовком: Headline2 (при [large] — Headline1), подзаголовок Paragraph3 60%. */
@Composable
fun CmpTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    large: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    style: CmpTopAppBarStyle = CmpTopAppBarStyle.Small,
) {
    CmpTopAppBar(
        title = {
            CmpText(
                text = title,
                style = if (large) CmpTextStyle.Headline1 else CmpTextStyle.Headline2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                autoSize = true,
            )
        },
        modifier = modifier,
        subtitle = subtitle?.let { text ->
            {
                CmpText(
                    text = text,
                    style = CmpTextStyle.Paragraph3,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        style = style,
    )
}

@Composable
private fun cmpTopAppBarColors(containerColor: Color, contentColor: Color): TopAppBarColors {
    val colorScheme = MaterialTheme.colorScheme
    // Прозрачный по умолчанию: фон и блюр под топбаром рисует CmpScaffold (полоса блюра при прокрутке)
    val resolvedContainerColor = if (containerColor != Color.Unspecified) containerColor else Color.Transparent
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        contentColor
    } else {
        colorScheme.onBackground
    }

    return TopAppBarDefaults.topAppBarColors(
        containerColor = resolvedContainerColor,
        scrolledContainerColor = resolvedContainerColor,
        navigationIconContentColor = resolvedContentColor,
        titleContentColor = resolvedContentColor,
        actionIconContentColor = resolvedContentColor,
    )
}
