package com.android1500.androidfaker.data.platform.repository

import android.util.Log

/** Global-operation sequencing recovered from ProfileRepositoryImpl/ik4.smali. */
internal class GlobalProfileActions(
    private val createForApp: suspend (String, String) -> Unit,
    private val switchForApp: suspend (String, String) -> Unit,
    private val deleteForApp: suspend (String, String) -> Unit,
    private val forceStop: suspend (String) -> Unit,
    private val activeProfile: suspend (Set<String>) -> String?,
) {
    suspend fun createGlobalProfile(enabledApps: Set<String>, profileName: String) {
        if (enabledApps.isEmpty()) throw RuntimeException("No enabled apps to create profiles for")
        var failures = 0
        for (app in enabledApps) {
            try { createForApp(app, profileName) }
            catch (error: Exception) {
                failures++
                Log.w(TAG, "Failed to create profile '$profileName' for $app", error)
            }
        }
        if (failures == enabledApps.size) throw RuntimeException("Failed to create profile for all apps")
        // Partial failure is logged but not thrown, matching the original contract.
    }

    suspend fun switchGlobalProfile(enabledApps: Set<String>, profileName: String) {
        if (enabledApps.isEmpty()) return
        // Complete the stop pass before any switch. Stop exceptions propagate directly.
        for (app in enabledApps) forceStop(app)
        var failures = 0
        for (app in enabledApps) {
            try { switchForApp(app, profileName) }
            catch (error: Exception) {
                failures++
                Log.w(TAG, "Failed to switch $app to profile '$profileName'", error)
            }
        }
        if (failures == enabledApps.size) throw RuntimeException("Failed to switch any app to profile '$profileName'")
    }

    suspend fun deleteGlobalProfile(enabledApps: Set<String>, profileName: String) {
        check(activeProfile(enabledApps) != profileName) {
            "Cannot delete the currently active profile: $profileName"
        }
        for (app in enabledApps) {
            try { deleteForApp(app, profileName) }
            catch (error: Exception) {
                Log.w(TAG, "Failed to delete profile '$profileName' for $app", error)
            }
        }
    }

    private companion object { const val TAG = "GlobalProfileActions" }
}
