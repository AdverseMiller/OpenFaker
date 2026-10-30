package com.android1500.androidfaker.data.platform.repository

/** Fields recovered from ck4; the original model's qualified name is not retained. */
data class Profile(val name: String, val isActive: Boolean, val appCount: Int, val totalSize: Long)

/** Method names recovered from ProfileRepositoryImpl coroutine metadata (hk4). */
interface ProfileRepository {
    suspend fun createGlobalProfile(enabledApps: Set<String>, profileName: String)
    suspend fun deleteGlobalProfile(enabledApps: Set<String>, profileName: String)
    suspend fun getGlobalProfiles(enabledApps: Set<String>): List<Profile>
    suspend fun isProfileFeatureAvailable(): Boolean
    suspend fun onAppEnabled(packageName: String, profileNames: List<String>)
    suspend fun onAppDisabled(packageName: String)
    suspend fun switchGlobalProfile(enabledApps: Set<String>, profileName: String)
}
