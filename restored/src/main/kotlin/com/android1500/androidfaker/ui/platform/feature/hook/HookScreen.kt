package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidfaker.ui.platform.component.dialog.FakerBasicDialog
import kotlinx.coroutines.launch

@Composable
fun HookScreen(appName: String, viewModel: HookViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.handle(HookEditorAction.ImportFile(uri))
    }
    HookScreenContent(appName, viewModel.packageName, state, viewModel::handle, onBack,
        onSelectFile = { launcher.launch(arrayOf("application/json")) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HookScreenContent(appName: String, packageName: String, state: HookState,
    onAction: (HookEditorAction) -> Unit, onBack: () -> Unit, onSelectFile: () -> Unit) {
    val pager = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var simDialog by remember { mutableStateOf(false) }
    val currentAction by rememberUpdatedState(onAction)

    LaunchedEffect(state.exportResultMessage) {
        state.exportResultMessage?.let { snackbar.showSnackbar(it); currentAction(HookEditorAction.ClearExportResult) }
    }
    LaunchedEffect(state.importResultMessage) {
        state.importResultMessage?.let { snackbar.showSnackbar(it); currentAction(HookEditorAction.ClearImportResult) }
    }
    LaunchedEffect(state.scopeErrorMessage) {
        state.scopeErrorMessage?.let {
            snackbar.showSnackbar(it, actionLabel = "Dismiss", duration = SnackbarDuration.Long)
            currentAction(HookEditorAction.ClearScopeError)
        }
    }
    LaunchedEffect(state.scopeSuccessMessage) {
        state.scopeSuccessMessage?.let { snackbar.showSnackbar(it); currentAction(HookEditorAction.ClearScopeSuccess) }
    }
    LaunchedEffect(state.dialogState) { if (state.dialogState != null) simDialog = false }

    if (state.exportDialogVisible) ExportFileDialog(state.isExporting,
        { onAction(HookEditorAction.ExportFile(it)) }, { onAction(HookEditorAction.HideExport) })
    if (state.importDialogVisible) ImportFileDialog(state.isImporting, onSelectFile,
        { onAction(HookEditorAction.HideImport) })
    if (simDialog) SimConfigDialog(state,
        { onAction(HookEditorAction.SelectSimCountry(it)) }, { onAction(HookEditorAction.SelectSimOperator(it)) },
        { onAction(HookEditorAction.SelectSimCode(it)) }, { onAction(HookEditorAction.LockSimCountry(it)) },
        { onAction(HookEditorAction.SaveSim); simDialog = false }, { simDialog = false })
    (state.dialogState as? HookState.DialogState.Error)?.let { error ->
        val resources = LocalContext.current.resources
        FakerBasicDialog(error.title?.resolve(resources)?.toString(), error.message.resolve(resources).toString(),
            onDismissRequest = { onAction(HookEditorAction.DismissError) })
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        Column {
            TopAppBar(title = {
                Column {
                    Text(appName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(packageName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }, actions = {
                IconButton(onClick = { onAction(HookEditorAction.RandomizeAll) }) { Icon(Icons.Outlined.Repeat, "Randomize All") }
                IconButton(onClick = { onAction(HookEditorAction.ClearConfig) }) { Icon(Icons.Outlined.Delete, "Delete") }
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, "More options") }
                    DropdownMenu(menuExpanded, { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Import") }, leadingIcon = { Icon(Icons.Outlined.FileDownload, null) },
                            onClick = { onAction(HookEditorAction.ShowImport); menuExpanded = false })
                        DropdownMenuItem(text = { Text("Export") }, leadingIcon = { Icon(Icons.Outlined.FileUpload, null) },
                            onClick = { onAction(HookEditorAction.ShowExport); menuExpanded = false })
                        DropdownMenuItem(text = { Text(if (state.isInModuleScope) "Remove from scope" else "Add to scope") },
                            leadingIcon = { Icon(if (state.isInModuleScope) Icons.Outlined.RemoveCircleOutline else Icons.Outlined.AddCircleOutline, null) },
                            onClick = { onAction(HookEditorAction.ToggleScope); menuExpanded = false })
                    }
                }
            })
            TabRow(pager.currentPage) {
                listOf("Device ID", "Fingerprint").forEachIndexed { index, title ->
                    Tab(selected = pager.currentPage == index, onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        text = { Text(title) })
                }
            }
        }
    }) { padding ->
        HorizontalPager(pager, Modifier.fillMaxSize().padding(padding).testTag("HookPager"), beyondViewportPageCount = 1) { page ->
            if (page == 0) {
                LazyColumn(Modifier.fillMaxSize().testTag("DeviceIdList"), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(state.hookData, key = { it.id }) { item ->
                        if (item.id == "sim_country") SimCountryItem(item, state, onEdit = { simDialog = true },
                            onRandom = { onAction(HookEditorAction.RandomSim) },
                            onToggle = { onAction(HookEditorAction.ToggleField(item.id, it)) })
                        else DeviceIdItem(item, { onAction(HookEditorAction.ToggleField(item.id, it)) },
                            { onAction(HookEditorAction.EditField(item.id, it)) }, { onAction(HookEditorAction.RandomField(item.id)) })
                    }
                }
            } else FingerprintTab(state, { onAction(HookEditorAction.ToggleFingerprint(it)) },
                { onAction(HookEditorAction.SelectManufacturer(it)) }, { onAction(HookEditorAction.SelectModel(it)) },
                { onAction(HookEditorAction.SelectOs(it)) })
        }
    }
}
