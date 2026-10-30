package com.androidfaker.ui.platform.component.switch

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FakerSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true) {
    Switch(checked, onCheckedChange, modifier, enabled = enabled,
        thumbContent = if (checked) ({ Icon(Icons.Default.Check, null, Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary) }) else null,
        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest))
}
