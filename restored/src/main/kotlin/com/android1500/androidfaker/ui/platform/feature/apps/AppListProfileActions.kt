package com.android1500.androidfaker.ui.platform.feature.apps

import android.util.Log
import com.android1500.androidfaker.data.app.repository.AppRepository
import com.android1500.androidfaker.data.platform.repository.ProfileRepository
import com.androidfaker.ui.util.asText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Profile-toggle and confirmation handlers extracted from the recovered AppListViewModel. */
internal class AppListProfileActions(
    private val apps: AppRepository,
    private val profiles: ProfileRepository,
    private val controller: AppListStateController,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun toggle(packageName: String) = scope.launch(dispatcher) {
        if (packageName in controller.state.value.profileEnabledApps) requestDisable(packageName)
        else enable(packageName)
    }

    private suspend fun requestDisable(packageName: String) {
        try {
            // Original checks all enabled apps, not just the selected package.
            if (profiles.getGlobalProfiles(controller.state.value.profileEnabledApps).isEmpty()) {
                apps.setProfileEnabled(packageName, false)
            } else {
                controller.setDialog(AppListState.DialogState.ConfirmDisableProfile(packageName,
                    "Disable Data Profile".asText(),
                    "Disabling will delete all profile data for this app. Other apps' profiles are not affected.\n\nContinue?".asText()))
            }
        } catch (error: Exception) {
            Log.w(TAG, "Error checking profiles for $packageName", error)
            apps.setProfileEnabled(packageName, false)
        }
    }

    private suspend fun enable(packageName: String) {
        try {
            if (!profiles.isProfileFeatureAvailable()) {
                controller.setDialog(AppListState.DialogState.Error("Root Required".asText(),
                    "Data profile requires root access. Please grant root permission and try again.".asText(), null))
                return
            }
            apps.setProfileEnabled(packageName, true)
            // Read the projected state here, as in the original; do not substitute
            // the repository's just-updated set or unconditionally remove this app.
            val others = controller.state.value.profileEnabledApps
            if (others.isEmpty()) return
            val existing = profiles.getGlobalProfiles(others)
            val names = existing.map { it.name }
            val active = existing.firstOrNull { it.isActive }?.name
            if (names.isEmpty()) return
            profiles.onAppEnabled(packageName, names)
            if (active != null) {
                try {
                    profiles.switchGlobalProfile(setOf(packageName), active)
                } catch (error: Exception) {
                    // A failed active-profile switch does not undo successful enrollment.
                    Log.w(TAG, "Failed to sync active profile to $packageName", error)
                }
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to enable profile for $packageName", error)
            apps.setProfileEnabled(packageName, false)
            showError(error, "Failed to enable profile for $packageName")
        }
    }

    fun confirmDisable(packageName: String) = scope.launch(dispatcher) {
        controller.setDialog(null)
        runCatching {
            try {
                profiles.onAppDisabled(packageName)
            } catch (error: Exception) {
                Log.w(TAG, "Failed to clean up profile data for $packageName, disabling anyway", error)
            }
            apps.setProfileEnabled(packageName, false)
        }.onFailure { showError(it, "Failed to delete profiles for $packageName") }
    }

    private fun showError(error: Throwable, fallback: String) {
        controller.setDialog(AppListState.DialogState.Error("Error".asText(),
            (error.message ?: fallback).asText(), error))
    }

    private companion object { const val TAG = "AppListProfileActions" }
}
