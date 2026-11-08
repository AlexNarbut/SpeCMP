package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import specmp.feature.device.autostart.generated.resources.Res
import specmp.feature.device.autostart.generated.resources.autostart_alarm_clone_ink
import specmp.feature.device.autostart.generated.resources.autostart_alarm_cloning_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_close_hood_successfully_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_close_hood_successfully_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_exchange_v2_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_exchange_v2_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_stop_request_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_stop_request_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_stop_success_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_engine_stop_success_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_key_out_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_key_out_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_lada_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_lada_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_need_close_hood_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_need_close_hood_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_need_open_hood_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_need_open_hood_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_open_hood_successfully_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_open_hood_successfully_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_process_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_process_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_result_fail_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_result_fail_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_result_success_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_result_success_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_toyota_v2_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_toyota_v2_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_off_ignition_request_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_off_ignition_success_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_off_ignition_success_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_engine_request_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_engine_success_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_engine_success_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_ignition_request_ink
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_ignition_success_accent
import specmp.feature.device.autostart.generated.resources.autostart_bypass_turn_on_ignition_success_ink

/**
 * Иллюстрация шага (макет `design/features/bypass-clone/illus`, карта `BC_ILLUS`). Каждая — два слоя vector XML:
 * линии ([ink]) и акцент ([accent], в исходнике `#2672d1`), перекрашиваются темой (решение D8 задачи).
 * Пропорция 720:495 — общий артборд, одна толщина линий у всех.
 */
enum class AutostartIllustration(internal val ink: DrawableResource, internal val accent: DrawableResource? = null) {
    IGNITION_ON_REQUEST(Res.drawable.autostart_bypass_turn_on_ignition_request_ink),
    IGNITION_ON_DONE(
        Res.drawable.autostart_bypass_turn_on_ignition_success_ink,
        Res.drawable.autostart_bypass_turn_on_ignition_success_accent,
    ),
    IGNITION_OFF_REQUEST(Res.drawable.autostart_bypass_turn_off_ignition_request_ink),
    IGNITION_OFF_DONE(
        Res.drawable.autostart_bypass_turn_off_ignition_success_ink,
        Res.drawable.autostart_bypass_turn_off_ignition_success_accent,
    ),
    ENGINE_START_REQUEST(Res.drawable.autostart_bypass_turn_on_engine_request_ink),
    ENGINE_START_DONE(
        Res.drawable.autostart_bypass_turn_on_engine_success_ink,
        Res.drawable.autostart_bypass_turn_on_engine_success_accent,
    ),
    ENGINE_STOP_REQUEST(
        Res.drawable.autostart_bypass_engine_stop_request_ink,
        Res.drawable.autostart_bypass_engine_stop_request_accent,
    ),
    ENGINE_STOP_DONE(
        Res.drawable.autostart_bypass_engine_stop_success_ink,
        Res.drawable.autostart_bypass_engine_stop_success_accent,
    ),
    KEY_OUT(Res.drawable.autostart_bypass_key_out_ink, Res.drawable.autostart_bypass_key_out_accent),
    HOOD_OPEN_REQUEST(
        Res.drawable.autostart_bypass_need_open_hood_ink,
        Res.drawable.autostart_bypass_need_open_hood_accent,
    ),
    HOOD_OPEN_DONE(
        Res.drawable.autostart_bypass_open_hood_successfully_ink,
        Res.drawable.autostart_bypass_open_hood_successfully_accent,
    ),
    HOOD_CLOSE_REQUEST(
        Res.drawable.autostart_bypass_need_close_hood_ink,
        Res.drawable.autostart_bypass_need_close_hood_accent,
    ),
    HOOD_CLOSE_DONE(
        Res.drawable.autostart_bypass_close_hood_successfully_ink,
        Res.drawable.autostart_bypass_close_hood_successfully_accent,
    ),
    EXCHANGE(
        Res.drawable.autostart_bypass_engine_exchange_v2_ink,
        Res.drawable.autostart_bypass_engine_exchange_v2_accent,
    ),
    LEARNING(Res.drawable.autostart_bypass_process_ink, Res.drawable.autostart_bypass_process_accent),
    LADA(Res.drawable.autostart_bypass_lada_ink, Res.drawable.autostart_bypass_lada_accent),
    TOYOTA(Res.drawable.autostart_bypass_toyota_v2_ink, Res.drawable.autostart_bypass_toyota_v2_accent),
    RESULT_SUCCESS(
        Res.drawable.autostart_bypass_result_success_ink,
        Res.drawable.autostart_bypass_result_success_accent,
    ),
    RESULT_FAIL(Res.drawable.autostart_bypass_result_fail_ink, Res.drawable.autostart_bypass_result_fail_accent),
    CLONE_START(Res.drawable.autostart_alarm_clone_ink),
    CLONE_RUN(Res.drawable.autostart_alarm_cloning_ink),
}

/** Слои иллюстрации, перекрашенные в [ink] и [accent]; размер и рамку задаёт вызывающий через [modifier]. */
@Composable
fun AutostartIllustrationLayers(
    illustration: AutostartIllustration,
    ink: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CmpIcon(
            painter = painterResource(illustration.ink),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = ink,
        )
        illustration.accent?.let { layer ->
            CmpIcon(
                painter = painterResource(layer),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = accent,
            )
        }
    }
}
