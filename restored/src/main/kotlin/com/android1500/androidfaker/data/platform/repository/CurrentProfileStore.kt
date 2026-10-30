package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import android.content.SharedPreferences
import com.android1500.androidfaker.data.platform.util.LibsuRootCommandExecutor
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor

/** Extracted active-profile helpers from ProfileRepositoryImpl; descriptive class name. */
internal class CurrentProfileStore(
    private val preferences: SharedPreferences,
    private val commands: RootCommandExecutor,
) {
    constructor(context: Context, commands: RootCommandExecutor = LibsuRootCommandExecutor()) :
        this(context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE), commands)

    fun record(packageName: String, profileName: String?) {
        preferences.edit().putString("currentProfile_$packageName", profileName).apply()
    }

    suspend fun getCurrentProfileForApp(packageName: String): String? {
        // Reconstruction hardening, consistent with AppRepository's command boundary.
        require(packageName.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*"))) {
            "Invalid package name"
        }
        val result = commands.execute("readlink /data/data/$packageName/.data_profiles/.current")
        val current = if (result.isSuccess && result.out.isNotEmpty()) result.out.first().trim() else null
        if (current != null) {
            // Empty trimmed output is intentionally retained, matching the APK.
            record(packageName, current)
            return current
        }
        return preferences.getString("currentProfile_$packageName", null)
    }

    suspend fun getActiveProfileName(enabledApps: Set<String>): String? {
        var first = true
        var active: String? = null
        for (packageName in enabledApps) {
            val current = getCurrentProfileForApp(packageName)
            if (first) {
                active = current
                first = false
            } else if (current != active) return null
        }
        return active
    }

    companion object { const val STORAGE_NAME = "data_profile_cache" }
}
