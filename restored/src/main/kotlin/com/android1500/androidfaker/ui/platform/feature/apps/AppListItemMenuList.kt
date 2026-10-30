package com.android1500.androidfaker.ui.platform.feature.apps

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.androidfaker.restored.R

/** Recovered menu order and callback-before-dismiss semantics from AppListScreen.kt. */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AppListItemMenuList(
    packageName: String,
    expanded: Boolean,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    isProfileEnabled: Boolean,
    onForceStopClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onEnableDataProfile: () -> Unit,
    onUninstallClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (expanded) key(isProfileEnabled) {
        DropdownMenu(expanded = true, onDismissRequest = onDismissRequest, offset = offset) {
            DropdownMenuItem(text = { Text(stringResource(R.string.force_stop)) }, onClick = {
                onForceStopClick(); onDismissRequest()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.clear_data)) }, onClick = {
                onClearDataClick(); onDismissRequest()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.uninstall)) }, onClick = {
                onUninstallClick(); onDismissRequest()
            })
            DropdownMenuItem(text = {
                Text(stringResource(if (isProfileEnabled) R.string.disable_data_profile else R.string.enable_data_profile))
            }, onClick = {
                onEnableDataProfile(); onDismissRequest()
            })
        }
    }
}
