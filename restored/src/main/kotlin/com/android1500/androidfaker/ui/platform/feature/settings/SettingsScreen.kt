package com.android1500.androidfaker.ui.platform.feature.settings

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android1500.androidfaker.data.platform.datasource.disk.AppTheme
import com.androidfaker.ui.platform.component.colorpicker.ColorThemePicker
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onAbout: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        viewModel.selectExportFolder(it)
    }
    SettingsScreenContent(state, viewModel::setTheme, viewModel::setDynamicColors,
        viewModel::setPalette, viewModel::setShowSystemApps, { picker.launch(null) }, onAbout)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(state: SettingsState, onTheme: (AppTheme) -> Unit, onDynamicColors: (Boolean) -> Unit, onPalette: (Int, Int) -> Unit, onShowSystemApps: (Boolean) -> Unit, onChooseFolder: () -> Unit, onAbout: () -> Unit) {
    val version = installedAppVersion()
    var themeDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    if (themeDialog) {
        ThemeSelectionDialog(state.theme, { themeDialog = false }, {
            onTheme(it)
            themeDialog = false
        })
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("SettingsList")) {
            item { SettingsSection("Appearance") }
            item { ThemeListItem(state.theme) { themeDialog = true } }
            item {
                SwitchListItem("Dynamic Colors", if (state.isDynamicColorsSupported)
                    "Use Material You colors" else "Not available on this device",
                    Icons.Outlined.Colorize, state.isDynamicColorsEnabled,
                    { if (state.isDynamicColorsSupported) onDynamicColors(it) },
                    enabled = state.isDynamicColorsSupported)
            }
            item {
                ColorThemePicker(state.seedColor, state.paletteStyleIndex, state.isDynamicColorsEnabled, onPalette)
            }
            item { SettingsSection("Storage") }
            item {
                ExportFolderListItem(state.exportFolder) {
                    try {
                        onChooseFolder()
                    } catch (_: ActivityNotFoundException) {
                        scope.launch { snackbar.showSnackbar("No file manager found to pick a directory") }
                    }
                }
            }
            item { SettingsSection("Application List") }
            item {
                SwitchListItem("Show System Apps", "Include system applications in the list",
                    Icons.Outlined.Apps, state.isSystemAppShowEnabled, onShowSystemApps)
            }
            item {
                ListItem(headlineContent = { Text("About") },
                    supportingContent = { Text("OpenFaker $version") },
                    leadingContent = { Icon(Icons.Outlined.Info, null) },
                    trailingContent = { Icon(Icons.Outlined.ChevronRight, null) },
                    modifier = Modifier.clickable(onClick = onAbout))
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(title, Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary)
}
