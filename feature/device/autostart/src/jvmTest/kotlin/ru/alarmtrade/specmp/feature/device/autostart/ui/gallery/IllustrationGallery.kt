package ru.alarmtrade.specmp.feature.device.autostart.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustration
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components.AutostartIllustrationLayers

/** Все иллюстрации шагов (два слоя, перекраска темой); PNG — в `build/state-gallery/autostart`. */
class IllustrationGallery {

    @OptIn(ExperimentalLayoutApi::class)
    @Test
    fun illustrations() = StateGallery.render(File("build/state-gallery/autostart"), "00_illustrations") {
        val colors = cmpThemeColors()
        FlowRow(
            modifier = Modifier.fillMaxSize().background(colors.background).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AutostartIllustration.entries.forEach { illustration ->
                CmpColumn(modifier = Modifier.width(180.dp)) {
                    AutostartIllustrationLayers(
                        illustration = illustration,
                        ink = colors.onSurface,
                        accent = colors.primary,
                        modifier = Modifier
                            .aspectRatio(720f / 495f)
                            .background(colors.surfaceContainerLow, RoundedCornerShape(16.dp))
                    )
                    CmpText(text = illustration.name, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}
