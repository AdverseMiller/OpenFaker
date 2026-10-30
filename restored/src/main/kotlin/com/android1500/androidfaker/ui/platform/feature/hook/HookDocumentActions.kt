package com.android1500.androidfaker.ui.platform.feature.hook

import android.net.Uri
import com.android1500.androidfaker.data.platform.repository.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Recovered editor document workflow; actual SAF access stays in HookRepository. */
internal class HookDocumentActions(
    private val packageName: String,
    private val hooks: HookRepository,
    private val devices: DeviceRepository,
    private val carriers: CarrierRepository,
    private val update: ((HookState) -> HookState) -> Unit,
    private val restoreSavedSelections: () -> Unit,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    fun showExportDialog() {
        when {
            hooks.exportFolder == null -> update { it.copy(exportResultMessage = "Export folder not set. Please configure it in Settings.") }
            hooks.get(packageName) == null -> update { it.copy(exportResultMessage = "No configuration to export. Enable some hooks first.") }
            else -> update { it.copy(exportDialogVisible = true) }
        }
    }

    fun hideExportDialog() = update { it.copy(exportDialogVisible = false) }
    fun showImportDialog() = update { it.copy(importDialogVisible = true) }
    fun hideImportDialog() = update { it.copy(importDialogVisible = false) }
    fun clearExportResult() = update { it.copy(exportResultMessage = null) }
    fun clearImportResult() = update { it.copy(importResultMessage = null) }

    fun exportToFile(fileName: String) = scope.launch(ioDispatcher) {
        update { it.copy(isExporting = true) }
        val result = hooks.exportConfig(packageName, fileName)
        withContext(mainDispatcher) {
            when (result) {
                is ExportResult.Success -> update { it.copy(isExporting = false, exportDialogVisible = false,
                    exportResultMessage = "Exported to ${result.filePath}") }
                is ExportResult.Error -> update { it.copy(isExporting = false, exportResultMessage = result.message) }
            }
        }
    }

    fun importFromFile(uri: Uri) = scope.launch(ioDispatcher) {
        update { it.copy(importDialogVisible = false, isImporting = true) }
        when (val result = hooks.importConfig(packageName, uri)) {
            ImportResult.Success -> {
                val config = hooks.get(packageName)
                config?.manufacturer?.let(devices::loadModels)
                config?.model?.let(devices::loadOsVersions)
                config?.simCountryName?.let(carriers::loadOperators)
                withContext(mainDispatcher) {
                    update { it.copy(isImporting = false, importResultMessage = "Configuration imported successfully") }
                    restoreSavedSelections()
                }
            }
            is ImportResult.Error -> withContext(mainDispatcher) {
                update { it.copy(isImporting = false, importResultMessage = result.message) }
            }
        }
    }
}
