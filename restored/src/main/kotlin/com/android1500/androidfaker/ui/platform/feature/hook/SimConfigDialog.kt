package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SimConfigDialog(state: HookState, onCountrySelect: (String) -> Unit,
    onOperatorSelect: (String) -> Unit, onMncSelect: (String) -> Unit, onLockToggle: (Boolean) -> Unit,
    onSave: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.SimCard, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("SIM Country Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    SimDropdown("Country", state.selectedSimCountry, state.simCountries, !state.isSimCountryLocked,
                        onCountrySelect, Modifier.weight(1f))
                    IconButton(onClick = { onLockToggle(!state.isSimCountryLocked) }, enabled = state.selectedSimCountry.isNotBlank()) {
                        Icon(if (state.isSimCountryLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            if (state.isSimCountryLocked) "Unlock" else "Lock",
                            tint = if (state.isSimCountryLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (state.isSimCountryLocked) Text("Country locked. Random will only change operator & MNC.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SimDropdown("Operator", state.selectedSimOperator, state.simOperators, state.selectedSimCountry.isNotBlank(), onOperatorSelect)
                SimDropdown("MCC/MNC Code", state.selectedSimMnc, state.simMncCodes, state.selectedSimOperator.isNotBlank(), onMncSelect)
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = state.selectedSimCountry.isNotBlank() &&
                state.selectedSimOperator.isNotBlank() && state.selectedSimMnc.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
