package com.android1500.androidfaker.ui.platform.feature.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity

/** ViewModel is supplied explicitly until application DI/navigation are rebuilt. */
@Composable
fun LogScreen(viewModel: LogViewModel) {
    val logs by viewModel.logs.collectAsState()
    val filter by viewModel.filterState.collectAsState()
    val packages by viewModel.packageNames.collectAsState()
    val hooks by viewModel.hookNames.collectAsState()
    LogScreenContent(logs, filter, packages, hooks, viewModel::updateSearchQuery,
        viewModel::selectPackage, viewModel::selectHook, viewModel::resetFilters, viewModel::clearAllLogs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogScreenContent(
    logs: List<LogEntity>,
    filter: LogViewModel.FilterState,
    packages: List<String>,
    hookNames: List<String>,
    onSearch: (String) -> Unit,
    onPackage: (String?) -> Unit,
    onHook: (String?) -> Unit,
    onReset: () -> Unit,
    onClearLogs: () -> Unit,
) {
    var searching by remember { mutableStateOf(false) }
    var filtering by remember { mutableStateOf(false) }
    var confirmingClear by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    if (confirmingClear) {
        AlertDialog(
            onDismissRequest = { confirmingClear = false },
            title = { Text("Clear Logs") },
            text = { Text("Delete all hook logs? This cannot be undone.") },
            confirmButton = { Button(onClick = { onClearLogs(); confirmingClear = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { confirmingClear = false }) { Text("Cancel") } },
        )
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceContainer,
                        titleContentColor = colors.onSurface),
                    title = {
                        if (searching) {
                            OutlinedTextField(
                                value = filter.searchQuery,
                                onValueChange = onSearch,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search logs...") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.outline.copy(alpha = 0.3f),
                                ),
                            )
                        } else Column {
                            Text("Logs", maxLines = 1)
                            Text("${logs.size} entries", style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        if (searching) IconButton(onClick = { searching = false; onSearch("") }) {
                            Icon(Icons.Outlined.Close, "Close search")
                        }
                    },
                    actions = {
                        if (!searching) IconButton(onClick = { searching = true }) {
                            Icon(Icons.Outlined.Search, "Search")
                        }
                        IconButton(onClick = { filtering = !filtering }) {
                            Icon(Icons.Outlined.FilterList, "Filter", tint =
                                if (filter.selectedPackage != null || filter.selectedHookName != null)
                                    colors.primary else colors.onSurface)
                        }
                        IconButton(onClick = { confirmingClear = true }) { Icon(Icons.Outlined.Delete, "Clear") }
                    },
                )
                AnimatedVisibility(filtering) {
                    FilterSection(packages, hookNames, filter.selectedPackage, filter.selectedHookName,
                        onPackage, onHook, onReset)
                }
            }
        },
    ) { padding ->
        if (logs.isEmpty()) EmptyLogs(Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(logs, key = { it.id }) { LogItem(it) }
        }
    }
}

@Composable
private fun EmptyLogs(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Outlined.Assignment, null, Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp))
            Text("No hook logs yet", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text("Logs appear when hooked apps are opened", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
