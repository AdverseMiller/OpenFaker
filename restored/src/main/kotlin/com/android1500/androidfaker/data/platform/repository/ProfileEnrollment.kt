package com.android1500.androidfaker.data.platform.repository

import android.util.Log
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor

/** onAppEnabled/onAppDisabled recovered from ProfileRepositoryImpl. */
internal class ProfileEnrollment(
    private val createProfile: suspend (String, String) -> Unit,
    private val switchProfile: suspend (String, String) -> Unit,
    private val dataDirectory: (String) -> String,
    private val clearCurrent: (String) -> Unit,
    private val commands: RootCommandExecutor,
) {
    suspend fun onAppEnabled(packageName: String, profileNames: List<String>) {
        if (profileNames.isEmpty()) return
        try {
            for (name in profileNames) createProfile(packageName, name)
            switchProfile(packageName, profileNames.first())
        } catch (error: Exception) {
            // The repository swallows this exception; the outer view-model handler
            // therefore cannot observe a failed enrollment through this implementation.
            Log.w(TAG, "Failed to sync profiles to $packageName", error)
        }
    }

    suspend fun onAppDisabled(packageName: String) {
        try {
            require(packageName.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*"))) {
                "Invalid package name"
            }
            val directory = dataDirectory(packageName)
            // Reconstruction hardening: deletion is restricted to this app's profile
            // child under an Android app-data directory, never an arbitrary root.
            require(directory == "/data/data/$packageName" ||
                directory.matches(Regex("/data/user(?:_de)?/[0-9]+/${Regex.escape(packageName)}"))) {
                "Unexpected app data directory"
            }
            commands.execute("am force-stop $packageName")
            val result = commands.execute("rm -rf ${quoteProfilePath("$directory/.data_profiles")}")
            if (!result.isSuccess) Log.w(TAG, "Failed to clean up profiles for disabled app $packageName")
            // As in the APK, clear cache even on nonzero status, but not if execute throws.
            clearCurrent(packageName)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to clean up profiles for $packageName", error)
        }
    }

    private companion object { const val TAG = "ProfileEnrollment" }
}
