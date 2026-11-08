package ru.alarmtrade.specmp.core.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpStepButton
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCodeField
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumb
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumbs
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpHtmlText

/**
 * Галерея новых компонентов кита: PNG в `core/ui/build/kit-gallery/` (светлая и тёмная тема) — для глазной
 * проверки. Запуск: `./gradlew :core:ui:jvmTest --tests '*KitGallery*'`.
 */
class KitGalleryTest {

    @Test
    fun `render kit components`() = render("kit", HEIGHT) { Gallery() }

    @Test
    fun `render catalog components`() = render("kit-catalog", CATALOG_HEIGHT) { CatalogGallery() }

    private fun render(name: String, height: Int, content: @Composable () -> Unit) {
        val out = File("build/kit-gallery").apply { mkdirs() }
        listOf(false, true).forEach { dark ->
            val scene = ImageComposeScene(WIDTH, height, Density(1f)) { Themed(dark, content) }
            try {
                var image = scene.render(0)
                for (frame in 1..FRAMES) image = scene.render(frame * FRAME_NANOS)
                val bytes = image.encodeToData(EncodedImageFormat.PNG)?.bytes ?: error("PNG encode failed")
                File(out, if (dark) "$name-dark.png" else "$name-light.png").writeBytes(bytes)
            } finally {
                scene.close()
            }
        }
    }

    @Composable
    private fun Themed(dark: Boolean, content: @Composable () -> Unit) {
        AppTheme(onThemeChange = {}) {
            val isDark = LocalThemeIsDark.current
            SideEffect { isDark.value = dark }
            content()
        }
    }

    @Composable
    private fun Gallery() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CmpSegmentedButton(options = listOf("Имп", "Пост", "Выкл"), selectedIndex = 0, onSelect = {})
            CmpSegmentedButton(
                options = listOf("НО", "НЗ"),
                selectedIndex = 1,
                onSelect = {},
                modifier = Modifier.width(160.dp),
                minHeight = 44.dp,
            )
            CmpSegmentedButton(
                options = listOf("Тон", "Мелодия"),
                selectedIndex = 0,
                onSelect = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpStepButton(icon = CmpIcons.Remove, contentDescription = "Меньше", onStep = {})
                CmpStepButton(icon = CmpIcons.Add, contentDescription = "Больше", onStep = {})
                CmpStepButton(icon = CmpIcons.KeyboardArrowUp, contentDescription = "Часы", onStep = {}, size = 56.dp)
                CmpStepButton(icon = CmpIcons.Add, contentDescription = "Нельзя", onStep = {}, enabled = false)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CmpCountPill(count = 3)
                CmpCountPill(count = 12)
                CmpDot()
            }
            CmpBreadcrumbs(
                items = listOf(CmpBreadcrumb("Все"), CmpBreadcrumb("3 Охрана", marked = true)),
                onItemClick = {},
            )
            CmpBreadcrumbs(
                items = listOf(
                    CmpBreadcrumb("Все"),
                    CmpBreadcrumb("3 Охрана", marked = true),
                    CmpBreadcrumb("3.2 Датчики", marked = true),
                    CmpBreadcrumb("3.2.5 Доп. датчики"),
                ),
                onItemClick = {},
            )
        }
    }

    @Composable
    private fun CatalogGallery() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CmpSearchField(value = "toyota cam", onValueChange = {
            }, placeholder = "Марка", clearContentDescription = "")
            CmpSearchField(value = "", onValueChange = {
            }, placeholder = "Марка, модель или CAN-код", clearContentDescription = "")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpChip(text = "Сигнализация", onClick = {}, icon = CmpIcons.Security)
                CmpChip(text = "Фильтр: 2 группы", onClick = {
                }, icon = CmpIcons.FilterAlt, selected = true, onClear = {})
            }
            CmpCodeField(value = "21", onValueChange = {}, label = "CAN-код", clearContentDescription = "")
            CmpCodeField(value = "2154", onValueChange = {
            }, label = "CAN-код", clearContentDescription = "", isError = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpSkeleton(Modifier.size(40.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CmpSkeleton(Modifier.width(220.dp).height(18.dp))
                    CmpSkeleton(Modifier.width(140.dp).height(14.dp))
                }
            }
            CmpHtmlText(
                html = "<p>Блокировка <b>по CAN2</b>, нужен модуль.</p>" +
                    "<ul><li>Снять пломбу</li><li>Подключить <i>CAN-H</i></li></ul>",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val domain = cmpDomainColors()
                with(domain) { listOf(busCan, busCanFd, busLin, busBean, busImmobilizer, busUnknown) }
                    .forEach { Box(Modifier.size(32.dp).background(it, CircleShape)) }
            }
            CmpStateBox(
                icon = CmpIcons.CloudOff,
                title = "Не удалось загрузить",
                text = "Сервер не ответил. Выбранные марка и модель сохранены.",
                tone = CmpStateTone.Error,
                compact = true,
            )
        }
    }

    private companion object {
        const val WIDTH = 480
        const val HEIGHT = 560
        const val CATALOG_HEIGHT = 1040
        const val FRAMES = 10
        const val FRAME_NANOS = 100_000_000L
    }
}
