package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun CatalogDropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(200), label = "rot")
    val colors = MaterialTheme.colorScheme
    Box(modifier) {
        Surface(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { expanded = !expanded }.testTag("CatalogDropdown:$label"),
            shape = RoundedCornerShape(12.dp), color = colors.surfaceContainer) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium,
                        color = colors.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f))
                    Text(value.ifBlank { "Select" }, style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface.copy(alpha = if (enabled && value.isNotBlank()) 1f else 0.5f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.rotate(rotation),
                    tint = colors.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f))
            }
        }
        DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 280.dp)) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No options", color = colors.onSurfaceVariant.copy(alpha = 0.5f)) },
                    onClick = {})
            } else options.forEach { option ->
                val selected = option == value
                DropdownMenuItem(
                    text = { Text(option, color = if (selected) colors.primary else colors.onSurface,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                    onClick = { onSelect(option); expanded = false },
                    modifier = Modifier.background(if (selected) colors.primary.copy(alpha = 0.1f) else Color.Transparent),
                    trailingIcon = if (selected) ({ Icon(Icons.Default.Check, null, tint = colors.primary) }) else null,
                )
            }
        }
    }
}

@Composable
internal fun SimDropdown(label: String, value: String, options: List<String>, enabled: Boolean,
    onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box(modifier) {
        // Like the original, a disabled read-only field supplies the appearance and an overlay handles clicks.
        OutlinedTextField(value.ifBlank { "" }, {}, modifier = Modifier.fillMaxWidth(), enabled = false,
            readOnly = true, label = { Text(label) }, placeholder = { Text("Select") },
            trailingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = colors.onSurface.copy(alpha = if (value.isBlank()) 0.5f else 1f),
                disabledBorderColor = colors.outline.copy(alpha = if (enabled) 1f else 0.5f),
                disabledLabelColor = colors.onSurfaceVariant))
        Box(Modifier.matchParentSize().clickable(enabled = enabled) { expanded = !expanded }
            .testTag("SimDropdown:$label"))
        DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 280.dp)) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No options available", color = colors.onSurfaceVariant.copy(alpha = 0.5f)) },
                    onClick = {})
            } else options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}
