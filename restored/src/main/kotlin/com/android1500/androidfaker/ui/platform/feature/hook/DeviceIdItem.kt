package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidfaker.ui.platform.component.switch.FakerSwitch

@Composable
internal fun DeviceIdItem(item: HookData, onToggle: (Boolean) -> Unit, onEdit: (String) -> Unit, onRandom: () -> Unit) {
    var editing by remember { mutableStateOf(false) }
    if (editing) {
        EditDialog(item.title, item.value ?: "", onSave = { onEdit(it); editing = false }, onDismiss = { editing = false })
    }
    val colors = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.checked) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(if (item.checked) colors.primary else colors.outlineVariant, CircleShape))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                IdentifierValue(item.displayValue, item.value.isNullOrEmpty())
                if (item.value2 != null) IdentifierValue(item.displayValue2, item.value2.isEmpty())
            }
            if (item.checked) {
                IconButton(onClick = { editing = true }, modifier = Modifier.size(36.dp)) {
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

@Composable
private fun IdentifierValue(value: String, empty: Boolean) {
    Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (empty) 0.5f else 1f),
        maxLines = 1, overflow = TextOverflow.Ellipsis)
}
