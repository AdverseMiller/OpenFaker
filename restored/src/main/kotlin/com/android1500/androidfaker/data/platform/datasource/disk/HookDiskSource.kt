package com.android1500.androidfaker.data.platform.datasource.disk

import android.content.SharedPreferences
import com.androidfaker.data.repository.util.HookConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface HookDiskSource {
    val configs: StateFlow<Map<String, HookConfig>>
    fun get(packageName: String): HookConfig?
    fun put(config: HookConfig)
    fun remove(packageName: String)
}

/**
 * Recovered storage contract. The caller supplies local or module-backed preferences;
 * the original module-service preference selection is not yet reconstructed.
 */
class HookDiskSourceImpl(
    private val json: Json,
    private val preferences: SharedPreferences,
) : HookDiskSource {
    private val state = MutableStateFlow(read())
    override val configs = state.asStateFlow()

    // Hold a strong reference: Android stores preference listeners weakly.
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == STORAGE_NAME) state.value = read()
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun get(packageName: String): HookConfig? = state.value[packageName]

    override fun put(config: HookConfig) {
        val packageName = requireNotNull(config.pkgName) { "HookConfig.pkgName is required" }
        update(state.value + (packageName to config))
    }

    override fun remove(packageName: String) = update(state.value - packageName)

    private fun read(): Map<String, HookConfig> {
        val encoded = preferences.getString(STORAGE_NAME, null) ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<String, HookConfig>>(encoded) }
            .getOrDefault(emptyMap())
    }

    private fun update(configs: Map<String, HookConfig>) {
        // Preserve the original order: publish, serialize, then apply to preferences.
        state.value = configs
        preferences.edit().putString(STORAGE_NAME, json.encodeToString(configs)).apply()
    }

    companion object {
        // Both the preference file name and the JSON map key in the original APK.
        const val STORAGE_NAME = "hooks"
    }
}
