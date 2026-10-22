package ru.alarmtrade.specmp.core.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import ru.alarmtrade.specmp.core.ui.view.badge.CmpCountPill
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpStepButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumb
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumbs

/**
 * Галерея новых компонентов кита: PNG в `core/ui/build/kit-gallery/` (светлая и тёмная тема) — для глазной
 * проверки. Запуск: `./gradlew :core:ui:jvmTest --tests '*KitGallery*'`.
 */
class KitGalleryTest {

    @Test
    fun `render kit components`() {
        val out = File("build/kit-gallery").apply { mkdirs() }
        listOf(false, true).forEach { dark ->
            val scene = ImageComposeScene(WIDTH, HEIGHT, Density(1f)) { Themed(dark) { Gallery() } }
            try {
                var image = scene.render(0)
                for (frame in 1..FRAMES) image = scene.render(frame * FRAME_NANOS)
                val bytes = image.encodeToData(EncodedImageFormat.PNG)?.bytes ?: error("PNG encode failed")
                File(out, if (dark) "kit-dark.png" else "kit-light.png").writeBytes(bytes)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CmpSegmentedButton(options = listOf("Имп", "Пост", "Выкл"), selectedIndex = 0, onSelect = {})
            CmpSegmentedButton(
                options = listOf("НО", "НЗ"),
                selectedIndex = 1,
                onSelect = {},
                modifier = Modifier.width(160.dp),
                minHeight = 44.dp
            )
            CmpSegmentedButton(
                options = listOf("Тон", "Мелодия"),
                selectedIndex = 0,
                onSelect = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
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
                onItemClick = {}
            )
            CmpBreadcrumbs(
                items = listOf(
                    CmpBreadcrumb("Все"),
                    CmpBreadcrumb("3 Охрана", marked = true),
                    CmpBreadcrumb("3.2 Датчики", marked = true),
                    CmpBreadcrumb("3.2.5 Доп. датчики")
                ),
                onItemClick = {}
            )
        }
    }

    private companion object {
        const val WIDTH = 480
        const val HEIGHT = 560
        const val FRAMES = 10
        const val FRAME_NANOS = 100_000_000L
    }
}
