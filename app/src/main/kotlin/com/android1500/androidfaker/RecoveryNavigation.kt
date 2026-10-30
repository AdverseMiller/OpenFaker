package com.android1500.androidfaker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

/** Incremental navigation for recovered destinations; profile destinations are still pending. */
@Composable
internal fun RecoveryNavigation(
    apps: @Composable ((String, String) -> Unit) -> Unit,
    logs: @Composable () -> Unit,
    settings: @Composable (() -> Unit) -> Unit,
    about: @Composable (() -> Unit) -> Unit,
    editor: @Composable (String, String, () -> Unit) -> Unit,
    onCloseEditor: () -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf("apps") }
    var editorPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var editorName by rememberSaveable { mutableStateOf("") }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val closeEditor = { editorPackage = null; onCloseEditor() }
    BackHandler(enabled = showAbout || editorPackage != null || destination != "apps") {
        if (showAbout) showAbout = false
        else if (editorPackage != null) closeEditor() else destination = "apps"
    }
    val currentPackage = editorPackage
    if (showAbout) {
        about { showAbout = false }
    } else if (currentPackage != null) {
        editor(currentPackage, editorName, closeEditor)
    } else {
        // Child screens own their top/system insets; this column reserves only the bottom bar's height.
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (destination) {
                    "apps" -> apps { packageName, appName ->
                        editorName = appName
                        editorPackage = packageName
                    }
                    "settings" -> settings { showAbout = true }
                    else -> logs()
                }
            }
            NavigationBar {
                NavigationBarItem(selected = destination == "apps", onClick = { destination = "apps" },
                    icon = { Icon(Icons.Outlined.Apps, null) }, label = { Text("Apps") })
                NavigationBarItem(selected = destination == "logs", onClick = { destination = "logs" },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null) }, label = { Text("Logs") })
                NavigationBarItem(selected = destination == "settings", onClick = { destination = "settings" },
                    icon = { Icon(Icons.Outlined.Settings, null) }, label = { Text("Settings") })
            }
        }
    }
}
