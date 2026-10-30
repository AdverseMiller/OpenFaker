package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import android.net.Uri
import com.android1500.androidfaker.data.platform.datasource.disk.HookDiskSource
import com.androidfaker.data.repository.util.HookConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Result names/packages are descriptive reconstructions from the retained variants.
sealed interface ImportResult {
    data object Success : ImportResult
    data class Error(val message: String) : ImportResult
}

sealed interface ExportResult {
    data class Success(val filePath: String) : ExportResult
    data class Error(val message: String) : ExportResult
}

interface HookRepository {
    val configs: StateFlow<Map<String, HookConfig>>
    val exportFolder: String?
    fun get(packageName: String): HookConfig?
    fun put(config: HookConfig)
    fun remove(packageName: String)
    suspend fun importConfig(packageName: String, uri: Uri): ImportResult
    fun exportConfig(packageName: String, fileName: String): ExportResult
}

class HookRepositoryImpl internal constructor(
    private val diskSource: HookDiskSource,
    private val json: Json,
    private val getExportFolder: () -> String?,
    private val documents: HookDocuments,
) : HookRepository {
    // The provider corresponds to SettingsRepository's export-folder getter.
    constructor(context: Context, diskSource: HookDiskSource, json: Json, getExportFolder: () -> String?) :
        this(diskSource, json, getExportFolder, AndroidHookDocuments(context))

    constructor(context: Context, diskSource: HookDiskSource, settings: SettingsRepository, json: Json) :
        this(context, diskSource, json, { settings.exportFolder })

    override val configs get() = diskSource.configs
    override val exportFolder get() = getExportFolder()
    override fun get(packageName: String) = diskSource.get(packageName)
    override fun put(config: HookConfig) = diskSource.put(config)
    override fun remove(packageName: String) = diskSource.remove(packageName)

    override suspend fun importConfig(packageName: String, uri: Uri): ImportResult = try {
        val text = documents.openInput(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        if (text == null) ImportResult.Error("Cannot read file") else importJson(packageName, text)
    } catch (error: Exception) {
        ImportResult.Error("Import failed: ${error.message}")
    }

    fun importJson(packageName: String, text: String): ImportResult = try {
        put(json.decodeFromString<HookConfig>(text).copy(pkgName = packageName))
        ImportResult.Success
    } catch (error: Exception) {
        ImportResult.Error("Invalid configuration file: ${error.message}")
    }

    override fun exportConfig(packageName: String, fileName: String): ExportResult {
        val config = get(packageName) ?: return ExportResult.Error("No configuration found")
        val folder = exportFolder ?: return ExportResult.Error(
            "Export folder not set. Please configure it in Settings.",
        )
        return try {
            val directory = documents.openDirectory(Uri.parse(folder))
            val name = if (fileName.endsWith(".json", ignoreCase = true)) fileName else "$fileName.json"
            // The APK deletes an existing same-name document before creating its replacement.
            directory.find(name)?.let { directory.delete(it) }
            val uri = directory.create("application/json", name)
                ?: return ExportResult.Error("Cannot create file $name")
            val encoded = json.encodeToString(config.copy(pkgName = ""))
            val stream = documents.openOutput(uri) ?: return ExportResult.Error("Cannot write to file")
            stream.use { it.write(encoded.toByteArray(Charsets.UTF_8)) }
            ExportResult.Success(name)
        } catch (error: Exception) {
            ExportResult.Error("Export failed: ${error.message}")
        }
    }
}
