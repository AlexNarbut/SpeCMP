package ru.alarmtrade.specmp.feature.device.autostart.ui.gallery

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassActions
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassContent
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassPhase
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassState

/**
 * Раскадровка анимаций экрана обхода (Compact, светлая тема): смена шага (`bcIn`), пульс рамки, затухание полосы
 * статуса на итоге. PNG — `build/state-gallery/autostart/anim_*.png`; кадры подписаны временем в мс.
 */
class AnimationFilmstrip {

    private val outDir = File("build/state-gallery/autostart")
    private val base = BypassState(deviceName = "Pandora DX-4GS", deviceId = 107131)
    private fun running(kind: BypassStepKind, subject: BypassSubject) =
        base.copy(phase = BypassPhase.Running(BypassStep(kind, subject)))

    /** Кадры [times] (мс) после смены состояния `from` → `to`; возвращает, отличаются ли кадры между собой. */
    private fun filmstrip(name: String, from: BypassState, to: BypassState, times: List<Long>): Boolean {
        val state = mutableStateOf(from)
        val scene = ImageComposeScene(412, 700, Density(1f)) {
            AppTheme(onThemeChange = {}) { BypassContent(state.value, BypassActions()) }
        }
        val frames = try {
            var clock = 0L
            repeat(20) {
                scene.render(clock)
                clock += 100_000_000L
            }
            state.value = to
            // Живой поток кадров (60 Гц): анимации стартуют со следующего кадра после смены состояния.
            val start = clock
            val captured = mutableMapOf<Long, Image>()
            var t = 0L
            while (t <= times.max()) {
                val image = scene.render(start + t * 1_000_000L)
                if (t in times) captured[t] = image
                t += FRAME_MS
            }
            times.map { captured.getValue(it) }
        } finally {
            scene.close()
        }
        save(name, frames)
        val bytes = frames.map { it.encodeToData(EncodedImageFormat.PNG)!!.bytes.contentHashCode() }
        return bytes.distinct().size > 1
    }

    private fun save(name: String, frames: List<Image>) {
        val gap = 12
        val sheet = Surface.makeRasterN32Premul(
            frames.sumOf { it.width } + gap * (frames.size + 1),
            frames[0].height + 2 * gap,
        )
        sheet.canvas.clear(Color.makeRGB(120, 120, 120))
        var x = gap
        frames.forEach { image ->
            sheet.canvas.drawImageRect(
                image,
                Rect.makeXYWH(x.toFloat(), gap.toFloat(), image.width.toFloat(), image.height.toFloat()),
                Paint(),
            )
            x += image.width + gap
        }
        outDir.mkdirs()
        File(outDir, "$name.png").writeBytes(sheet.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }

    @Test
    fun stepAppears() = assertTrue(
        filmstrip(
            "anim_01_step_in_0_96_160_320ms",
            running(BypassStepKind.ACTION, BypassSubject.IGNITION_ON),
            running(BypassStepKind.ACCEPTED, BypassSubject.IGNITION_ON),
            listOf(0, 96, 160, 320),
        ),
        "new step must animate in",
    )

    @Test
    fun statusFadesOut() = assertTrue(
        filmstrip(
            "anim_02_status_out_0_160_240_400ms",
            running(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE),
            base.copy(phase = BypassPhase.Finished(BypassResult.Success)),
            listOf(0, 160, 240, 400),
        ),
        "status strip must fade out",
    )

    @Test
    fun ringPulses() = assertTrue(
        filmstrip(
            "anim_03_pulse_320_640_960_1280ms",
            base,
            running(BypassStepKind.ACTION, BypassSubject.ENGINE_START),
            listOf(320, 640, 960, 1280),
        ),
        "request ring must pulse",
    )

    private companion object {
        const val FRAME_MS = 16L
    }
}
