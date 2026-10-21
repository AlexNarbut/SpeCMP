package ru.alarmtrade.specmp.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.alarmtrade.permissions.common.PermissionsController

@Composable
actual fun BindEffect(permissionsController: PermissionsController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(permissionsController, lifecycleOwner, context) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        permissionsController.bind(lifecycleOwner.lifecycle, fragmentManager)
    }
}
