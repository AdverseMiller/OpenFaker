package com.android1500.androidfaker.ui.platform.feature.apps

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android1500.androidfaker.data.app.repository.SortType
import com.androidfaker.restored.R
import com.androidfaker.ui.platform.component.dialog.FakerBasicDialog

/** Navigation is supplied explicitly instead of the original generated destinations dependency. */
@Composable
fun AppListScreen(viewModel: AppListViewModel, onOpenApp: (AppItem) -> Unit, bottomInnerPadding: Dp = 0.dp) {
    val state by viewModel.state.collectAsState()
    AppListScreenContent(state, onOpenApp, viewModel::setQuery, viewModel::clearQuery,
        viewModel::setSortType, viewModel::refresh, viewModel::forceStop, viewModel::clearAppData,
        viewModel::uninstall, viewModel::toggleProfile, viewModel::dismissDialog,
        viewModel::confirmDisableProfile, viewModel::dismissDialog, bottomInnerPadding)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppListScreenContent(
    state: AppListState,
    onOpenApp: (AppItem) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSort: (SortType) -> Unit,
    onRefresh: () -> Unit,
    onForceStop: (String) -> Unit,
    onClearData: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onToggleProfile: (String) -> Unit,
    onDismissError: () -> Unit,
    onConfirmDisableProfile: (String) -> Unit,
    onCancelDisableProfile: () -> Unit,
    bottomInnerPadding: Dp = 0.dp,
) {
    var searching by remember { mutableStateOf(false) }
    var sorting by remember { mutableStateOf(false) }
    AppListDialogHandler(state.dialogState, onDismissError, onConfirmDisableProfile, onCancelDisableProfile)
    Scaffold(topBar = {
        Crossfade(searching, label = "Search") { isSearching ->
            if (isSearching) {
                AppSearchBar(state.query, onQueryChange, onClearQuery,
                    onBack = { searching = false; onClearQuery() })
            } else {
                TopAppBar(title = { Text(stringResource(R.string.apps)) }, actions = {
                    IconButton(onClick = { searching = true }) { Icon(Icons.Default.Search, "Search apps") }
                    Box {
                        IconButton(onClick = { sorting = true }) { Icon(Icons.AutoMirrored.Filled.Sort, "Sort apps") }
                        DropdownMenu(expanded = sorting, onDismissRequest = { sorting = false }) {
                            SortType.entries.forEach { type ->
                                DropdownMenuItem(text = { Text(type.displayName) }, onClick = {
                                    onSort(type)
                                    sorting = false
                                })
                            }
                        }
                    }
                })
            }
        }
    }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading…", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize().padding(padding).testTag("AppListRefresh")) {
                LazyColumn(Modifier.fillMaxSize().testTag("AppList"), contentPadding = PaddingValues(
                    start = 12.dp, top = 12.dp, end = 12.dp, bottom = bottomInnerPadding + 12.dp)) {
                    // The recovered list uses positional identity (no explicit item key).
                    items(state.appList) { app ->
                        AppListItem(app, onClick = { onOpenApp(app) },
                            onForceStopClick = { onForceStop(app.packageName) },
                            onClearDataClick = { onClearData(app.packageName) },
                            onUninstallClick = { onUninstall(app.packageName) },
                            onEnableDataProfile = { onToggleProfile(app.packageName) },
                            isAppEnabled = app.packageName in state.enabledApps,
                            isProfileEnabled = app.packageName in state.profileEnabledApps)
                    }
                }
            }
        }
    }
}

private val SortType.displayName: String get() = when (this) {
    SortType.NAME_ASC -> "Name A → Z"
    SortType.NAME_DESC -> "Name Z → A"
    SortType.INSTALL_TIME -> "Install Time"
    SortType.LAST_UPDATE_TIME -> "Last Updated"
}

/** Material text field replaces the shared app bar's custom BasicTextField decoration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, onBack: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    TopAppBar(title = {
        TextField(query, onQueryChange, singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("AppSearchQuery"),
            placeholder = { Text("Search apps") },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent))
    }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search") }
    }, actions = {
        if (query.isNotBlank()) IconButton(onClick = onClear) { Icon(Icons.Default.Close, "Clear search") }
    })
}

@Composable
internal fun AppListDialogHandler(
    dialogState: AppListState.DialogState?,
    onDismissError: () -> Unit,
    onConfirmDisableProfile: (String) -> Unit,
    onCancelDisableProfile: () -> Unit,
) {
    when (dialogState) {
        null -> Unit
        is AppListState.DialogState.Error -> FakerBasicDialog(
            dialogState.title?.invoke(), dialogState.message(), onDismissError)
        is AppListState.DialogState.ConfirmDisableProfile -> AlertDialog(
            onDismissRequest = onCancelDisableProfile,
            title = { Text(dialogState.title()) },
            text = { Text(dialogState.message(), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { onConfirmDisableProfile(dialogState.packageName) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete All Profiles")
                }
            },
            dismissButton = { TextButton(onClick = onCancelDisableProfile) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        )
    }
}
