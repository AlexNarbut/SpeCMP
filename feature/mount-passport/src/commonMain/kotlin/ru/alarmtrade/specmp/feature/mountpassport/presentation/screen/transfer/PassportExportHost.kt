package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportExportActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts

@Composable
internal fun PassportExportHost(
    passportId: String,
    ports: MountPassportPorts,
    content: @Composable (MountPassportExportActions) -> Unit,
) {
    val snackbar = rememberCmpSnackbarHostState()
    val transfer = rememberPassportTransfer("export-$passportId", ports, snackbar)
    val currentTransfer by rememberUpdatedState(transfer)
    val exporting = transfer.state.pdfExporting == passportId
    val actions = remember(passportId, exporting) {
        MountPassportExportActions(
            exportingPdf = exporting,
            onExportPdf = { currentTransfer.onExportPdf(passportId) },
            onExportZip = { currentTransfer.onExport(listOf(passportId)) },
        )
    }
    CmpAdaptiveBox(modifier = Modifier.fillMaxSize()) { window ->
        content(actions)
        CmpSnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
        PassportTransferSheet(transfer.state, transfer.actions, wide = window.atLeast(CmpWidthClass.Medium))
    }
}
