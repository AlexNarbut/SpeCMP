package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.core.common.AppDispatchers

@Composable
expect fun rememberTextFilePicker(dispatchers: AppDispatchers): TextFilePicker

@Composable
expect fun rememberTextFileSaver(dispatchers: AppDispatchers): TextFileSaver
