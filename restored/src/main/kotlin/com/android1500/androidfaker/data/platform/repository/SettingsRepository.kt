package com.android1500.androidfaker.data.platform.repository

import com.android1500.androidfaker.data.platform.datasource.disk.AppTheme
import com.android1500.androidfaker.data.platform.datasource.disk.SettingsDiskSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface SettingsRepository {
    var appTheme: AppTheme
    var seedColor: Int
    var paletteStyleIndex: Int
    var isDynamicColorsEnabled: Boolean
    var isSystemAppShowEnabled: Boolean
    var exportFolder: String?
    val termAndCondition: Boolean?
    val appThemeFlow: StateFlow<AppTheme>
    val seedColorFlow: StateFlow<Int>
    val paletteStyleIndexFlow: StateFlow<Int>
    val isDynamicColorsEnabledFlow: StateFlow<Boolean>
    val isSystemAppShowEnabledFlow: StateFlow<Boolean>
    val exportFolderFlow: StateFlow<String?>
    fun acceptTerms()
}

class SettingsRepositoryImpl(
    private val disk: SettingsDiskSource,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : SettingsRepository {
    override var appTheme: AppTheme
        get() = disk.appTheme
        set(value) { disk.appTheme = value }
    override var seedColor: Int
        get() = disk.seedColor
        set(value) { disk.seedColor = value }
    override var paletteStyleIndex: Int
        get() = disk.paletteStyleIndex
        set(value) { disk.paletteStyleIndex = value }
    override var isDynamicColorsEnabled: Boolean
        get() = disk.isDynamicColorsEnabled ?: false
        set(value) { disk.isDynamicColorsEnabled = value }
    override var isSystemAppShowEnabled: Boolean
        get() = disk.isSystemAppShowEnabled ?: false
        set(value) { disk.isSystemAppShowEnabled = value }
    override var exportFolder: String?
        get() = disk.exportFolder
        set(value) { disk.exportFolder = value }
    override val termAndCondition get() = disk.termAndCondition
    override fun acceptTerms() = disk.acceptTerms()

    // Each original getter creates a new eagerly collected StateFlow.
    override val appThemeFlow get() = disk.appThemeFlow.stateIn(scope, SharingStarted.Eagerly, appTheme)
    override val seedColorFlow get() = disk.seedColorFlow.stateIn(scope, SharingStarted.Eagerly, seedColor)
    override val paletteStyleIndexFlow get() = disk.paletteStyleIndexFlow.stateIn(scope, SharingStarted.Eagerly, paletteStyleIndex)
    override val isDynamicColorsEnabledFlow get() = disk.isDynamicColorsEnabledFlow
        .map { it ?: false }.stateIn(scope, SharingStarted.Eagerly, isDynamicColorsEnabled)
    override val isSystemAppShowEnabledFlow get() = disk.isSystemAppShowEnabledFlow
        .map { it ?: false }.stateIn(scope, SharingStarted.Eagerly, isSystemAppShowEnabled)
    override val exportFolderFlow: StateFlow<String?> get() = disk.exportFolderFlow
        .map { it.orEmpty() }.stateIn(scope, SharingStarted.Eagerly, exportFolder)
}
