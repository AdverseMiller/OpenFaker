package com.android1500.androidfaker.ui.platform.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android1500.androidfaker.data.platform.datasource.disk.AppTheme
import com.androidfaker.ui.platform.component.switch.FakerSwitch

private val AppTheme.label: String
    get() = when (this) {
        AppTheme.DEFAULT -> "System Default"
        AppTheme.LIGHT -> "Light"
        AppTheme.DARK -> "Dark"
    }

@Composable
internal fun ThemeListItem(currentTheme: AppTheme, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Theme") },
        supportingContent = { Text(currentTheme.label) },
        leadingContent = { Icon(Icons.Outlined.DarkMode, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
internal fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        text = {
            Column {
                listOf(
                    AppTheme.DEFAULT to "Follow system theme settings",
                    AppTheme.LIGHT to "Always use light theme",
                    AppTheme.DARK to "Always use dark theme",
                ).forEach { (theme, description) ->
                    ThemeOption(theme.label, description, theme == currentTheme) { onThemeSelected(theme) }
                }
            }
        },
    )
}

@Composable
private fun ThemeOption(label: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Only the switch is interactive, as in the original list item. */
@Composable
internal fun SwitchListItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = .38f)
    val detailColor = if (enabled) colors.onSurfaceVariant else colors.onSurfaceVariant.copy(alpha = .38f)
    ListItem(
        headlineContent = { Text(title, color = titleColor) },
        supportingContent = { Text(subtitle, color = detailColor) },
        leadingContent = { Icon(icon, null, tint = detailColor) },
        trailingContent = { FakerSwitch(checked, onCheckedChange, enabled = enabled) },
        modifier = Modifier.testTag("SettingsSwitch:$title"),
    )
}

@Composable
internal fun ExportFolderListItem(displayPath: String?, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Save Location") },
        supportingContent = { Text(displayPath?.takeIf { it.isNotEmpty() } ?: "Directory is invalid or not set") },
        leadingContent = { Icon(Icons.Outlined.Folder, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
