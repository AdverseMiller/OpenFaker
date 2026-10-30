package com.android1500.androidfaker.ui.platform.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android1500.androidfaker.data.platform.datasource.disk.AppTheme
import com.android1500.androidfaker.data.platform.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Account identity is intentionally omitted from the recovered SettingsState. */
data class SettingsState(
    val theme: AppTheme,
    val seedColor: Int,
    val paletteStyleIndex: Int,
    val isDynamicColorsSupported: Boolean,
    val isDynamicColorsEnabled: Boolean,
    val isSystemAppShowEnabled: Boolean,
    val exportFolder: String?,
)

class SettingsViewModel internal constructor(
    private val repository: SettingsRepository,
    supportsDynamicColors: Boolean,
    private val persistFolderPermission: (Uri) -> Unit,
    private val resolveFolderPath: (String) -> String,
    private val pathDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    constructor(repository: SettingsRepository, context: Context) : this(
        repository,
        Build.VERSION.SDK_INT >= 31,
        { uri -> context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        ) },
        { uri -> resolveExportFolderPath(context, uri) },
    )

    private val mutableState = MutableStateFlow(SettingsState(
        repository.appTheme, repository.seedColor, repository.paletteStyleIndex,
        supportsDynamicColors, repository.isDynamicColorsEnabled,
        repository.isSystemAppShowEnabled, repository.exportFolder,
    ))
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.exportFolderFlow.collect { uri ->
                val path = withContext(pathDispatcher) { resolveFolderPath(uri.orEmpty()) }
                mutableState.update { it.copy(exportFolder = path) }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        mutableState.update { it.copy(theme = theme) }
        repository.appTheme = theme
    }

    fun setPalette(seedColor: Int, styleIndex: Int) {
        mutableState.update { it.copy(seedColor = seedColor, paletteStyleIndex = styleIndex) }
        repository.seedColor = seedColor
        repository.paletteStyleIndex = styleIndex
    }

    fun setDynamicColors(enabled: Boolean) {
        mutableState.update { it.copy(isDynamicColorsEnabled = enabled) }
        repository.isDynamicColorsEnabled = enabled
    }

    /** Original internal action changes the displayed switch without persisting. */
    fun disableDynamicColorsInState() {
        mutableState.update { it.copy(isDynamicColorsEnabled = false) }
    }

    fun setShowSystemApps(enabled: Boolean) {
        mutableState.update { it.copy(isSystemAppShowEnabled = enabled) }
        repository.isSystemAppShowEnabled = enabled
    }

    fun selectExportFolder(uri: Uri?) = viewModelScope.launch {
        if (uri == null) return@launch
        // Acquire the durable grant before changing storage; cancellation is a no-op.
        persistFolderPermission(uri)
        repository.exportFolder = uri.toString()
        mutableState.update { it.copy(exportFolder = uri.toString()) }
    }
}
