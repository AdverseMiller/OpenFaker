package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidfaker.ui.platform.component.switch.FakerSwitch

@Composable
internal fun SimCountryItem(item: HookData, state: HookState, onEdit: () -> Unit, onRandom: () -> Unit,
    onToggle: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val configured = item.checked && state.selectedSimCountry.isNotBlank()
    val summary = if (!configured) "Not configured" else buildList {
        add(state.selectedSimCountry)
        if (state.selectedSimOperator.isNotBlank()) add(state.selectedSimOperator)
        if (state.selectedSimMnc.isNotBlank()) add(state.selectedSimMnc)
    }.joinToString(" • ")
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.checked) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(if (item.checked) colors.primary else colors.outlineVariant, CircleShape))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SIM Country", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    if (state.isSimCountryLocked && item.checked) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.Lock, "Locked", Modifier.size(14.dp), tint = colors.primary)
                    }
                }
                Text(summary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace,
                    color = colors.onSurfaceVariant.copy(alpha = if (configured) 1f else 0.5f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (item.checked) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Edit, "Edit", Modifier.size(18.dp), tint = colors.primary)
                }
                IconButton(onClick = onRandom, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Repeat, "Random", Modifier.size(18.dp), tint = colors.primary)
                }
            }
            FakerSwitch(item.checked, onToggle)
        }
    }
}
