package com.android1500.androidfaker.ui.platform.feature.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val shortLogTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
private val detailedLogTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

internal fun hookNameColor(name: String, colors: ColorScheme): Color = when {
    name.contains("IMEI", true) -> colors.error
    name.contains("Android", true) -> colors.primary
    name.contains("Ads", true) -> colors.tertiary
    name.contains("Bluetooth", true) -> colors.secondary
    name.contains("GSF", true) -> colors.error.copy(alpha = 0.8f)
    name.contains("MediaDRM", true) -> colors.secondary
    name.contains("AppSet", true) -> colors.tertiary
    name.contains("Phone", true) -> colors.primary
    name.contains("HWSerial", true) -> colors.secondary
    else -> colors.onSurfaceVariant
}

/** Expandable log row reconstructed from LogScreenKt.LogItem and its lambdas. */
@Composable
fun LogItem(log: LogEntity) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val hookColor = hookNameColor(log.hookName, colors)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = hookColor.copy(alpha = 0.15f)) {
                    Text(log.hookName, Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = hookColor, style = typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                Text(log.packageName.substringAfterLast('.'), Modifier.weight(1f),
                    color = colors.onSurfaceVariant, style = typography.labelSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(shortLogTime.format(Date(log.timestamp)), style = typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ValueDot(colors.tertiary)
                Text(log.spoofedValue.ifEmpty { "<empty>" }, color = colors.onSurface,
                    fontFamily = FontFamily.Monospace, style = typography.bodySmall,
                    maxLines = if (expanded) 5 else 1, overflow = TextOverflow.Ellipsis)
            }
            AnimatedVisibility(expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(Modifier.padding(bottom = 8.dp), color = colors.outline.copy(alpha = 0.3f))
                    ValueDetail("Original", log.originalValue, colors.error.copy(alpha = 0.8f),
                        colors.error.copy(alpha = 0.6f), 3)
                    Spacer(Modifier.height(6.dp))
                    ValueDetail("Spoofed", log.spoofedValue, colors.tertiary, colors.tertiary, 5)
                    Spacer(Modifier.height(6.dp))
                    Text(log.packageName, color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace, style = typography.labelSmall)
                    Text(detailedLogTime.format(Date(log.timestamp)),
                        color = colors.onSurfaceVariant.copy(alpha = 0.5f), style = typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun ValueDot(color: Color) {
    Box(Modifier.size(8.dp).background(color, CircleShape))
    Spacer(Modifier.width(8.dp))
}

@Composable
private fun ValueDetail(label: String, value: String, color: Color, dotColor: Color, lines: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ValueDot(dotColor)
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall)
            Text(value.ifEmpty { "<empty>" }, color = color, fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall, maxLines = lines, overflow = TextOverflow.Ellipsis)
        }
    }
}
