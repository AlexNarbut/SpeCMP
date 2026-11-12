package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_can_factory
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_connected_ble
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_connected_serial
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_connected_usb
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_next
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_unknown_model
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_msg_usb_state
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_title_ready
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_title_unknown_model
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts

/** Приветствие строится из фактов блока, а не хранится текстом: после переподключения оно актуально. */
@Composable
internal fun Greeting(facts: DeviceFacts) {
    if (!facts.inDatabase) {
        Message(
            title = stringResource(Strings.string.mountassistant_feed_title_unknown_model, facts.title),
            text = stringResource(Strings.string.mountassistant_feed_msg_unknown_model),
        )
        return
    }
    val parts = buildList {
        add(
            stringResource(
                when (facts.transport) {
                    AssistantTransport.BLE -> Strings.string.mountassistant_feed_msg_connected_ble
                    AssistantTransport.USB -> Strings.string.mountassistant_feed_msg_connected_usb
                    AssistantTransport.SERIAL -> Strings.string.mountassistant_feed_msg_connected_serial
                },
                facts.firmwareVersion,
            ),
        )
        val can = facts.can
        if (can != null) {
            add(
                if (can.factory) {
                    stringResource(Strings.string.mountassistant_feed_msg_can_factory)
                } else {
                    stringResource(Strings.string.mountassistant_feed_msg_can, can.version ?: can.code.orEmpty())
                },
            )
        }
        if (facts.transport !=
            AssistantTransport.BLE
        ) {
            add(stringResource(Strings.string.mountassistant_feed_msg_usb_state))
        }
        if (!facts.online) add(stringResource(Strings.string.mountassistant_feed_msg_offline))
        add(stringResource(Strings.string.mountassistant_feed_msg_next))
    }
    Message(
        title = stringResource(Strings.string.mountassistant_feed_title_ready, facts.title),
        text = parts.joinToString(" "),
    )
}
