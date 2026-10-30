package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
internal fun EditDialog(title: String, value: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    // Original remember has no value key: repository changes do not replace an in-progress edit.
    var edited by remember { mutableStateOf(value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $title") },
        text = {
            OutlinedTextField(edited, { edited = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace))
        },
        confirmButton = { Button(onClick = { onSave(edited) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
internal fun ExportFileDialog(isExporting: Boolean, onExport: (String) -> Unit, onDismiss: () -> Unit) {
    var fileName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        icon = { Icon(Icons.Outlined.FileUpload, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Export Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Enter a name for the configuration file.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(fileName, { fileName = it }, modifier = Modifier.fillMaxWidth(),
                    enabled = !isExporting, singleLine = true, label = { Text("File Name") },
                    placeholder = { Text("e.g., pixel_config") }, suffix = { Text(".json") })
                if (isExporting) DocumentProgress("Exporting...")
            }
        },
        confirmButton = {
            Button(onClick = { onExport(fileName) }, enabled = fileName.isNotBlank() && !isExporting) { Text("Export") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isExporting) { Text("Cancel") } },
    )
}

@Composable
internal fun ImportFileDialog(isImporting: Boolean, onSelectFile: () -> Unit, onDismiss: () -> Unit) {
    // The original accepted appName but did not display or otherwise use it.
    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        icon = { Icon(Icons.Outlined.FileDownload, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Import Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select a configuration file to import.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isImporting) DocumentProgress("Importing...")
            }
        },
        confirmButton = {
            Button(onClick = onSelectFile, enabled = !isImporting) {
                Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Select File")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isImporting) { Text("Cancel") } },
    )
}

@Composable
private fun DocumentProgress(label: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
