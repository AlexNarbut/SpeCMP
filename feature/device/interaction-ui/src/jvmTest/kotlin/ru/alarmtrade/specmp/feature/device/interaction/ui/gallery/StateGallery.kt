package ru.alarmtrade.specmp.feature.device.interaction.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.io.File
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark

/**
 * Галерея состояний: рендерит stateless-экран headless (без окна) в PNG — для визуальной проверки
 * состояний, которые не воспроизвести живым блоком. Лист на состояние: строки — светлая/тёмная тема,
 * колонки — опорные окна Compact 412×915, Medium 800×1280, Expanded 1280×800.
 */
internal object StateGallery {
    private val windows = listOf(412 to 915, 800 to 1280, 1280 to 800)
    private const val GAP = 24

    fun render(outDir: File, name: String, content: @Composable () -> Unit) {
        val shots = listOf(false, true).map { dark -> windows.map { (w, h) -> shot(w, h, dark, content) } }
        val width = windows.sumOf { it.first } + GAP * (windows.size + 1)
        val rowHeight = windows.maxOf { it.second }
        val sheet = Surface.makeRasterN32Premul(width, rowHeight * 2 + GAP * 3)
        sheet.canvas.clear(Color.makeRGB(120, 120, 120))
        shots.forEachIndexed { row, images ->
            var x = GAP
            images.forEach { image ->
                sheet.canvas.drawImageRect(
                    image,
                    Rect.makeXYWH(
                        x.toFloat(),
                        (GAP + row * (rowHeight + GAP)).toFloat(),
                        image.width.toFloat(),
                        image.height.toFloat()
                    ),
                    Paint()
                )
                x += image.width + GAP
            }
        }
        outDir.mkdirs()
        File(outDir, "$name.png").writeBytes(sheet.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }

    private fun shot(width: Int, height: Int, dark: Boolean, content: @Composable () -> Unit): Image {
        val scene = ImageComposeScene(width, height, Density(1f)) {
            AppTheme(onThemeChanged = {}) {
                val isDark = LocalThemeIsDark.current
                SideEffect { isDark.value = dark }
                content()
            }
        }
        return try {
            // Несколько кадров: смена темы, загрузка ресурсов, появление плиток (≤ 0.7 c).
            var image = scene.render(0)
            for (frame in 1..30) image = scene.render(frame * 100_000_000L)
            image
        } finally {
            scene.close()
        }
    }
}
