package com.android1500.androidfaker.data.platform.repository

/** Recovered nested ProfileRepositoryImpl.AppProfileInfo fields. */
internal data class AppProfileInfo(val name: String, val isActive: Boolean, val size: Long)

/**
 * getGlobalProfiles reconstructed from ik4.smali; JADX could not recover its loop.
 * Per-app filesystem enumeration is supplied separately, without hiding its errors.
 */
internal class GlobalProfileReader(
    private val listProfilesForApp: suspend (String) -> List<AppProfileInfo>,
) {
    suspend fun getGlobalProfiles(enabledApps: Set<String>): List<Profile> {
        if (enabledApps.isEmpty()) return emptyList()
        val names = linkedSetOf<String>()
        val byApp = linkedMapOf<String, Map<String, AppProfileInfo>>()
        for (packageName in enabledApps) {
            val profiles = listProfilesForApp(packageName)
            byApp[packageName] = profiles.associateBy { it.name }
            names.addAll(profiles.map { it.name })
        }
        return names.sorted().map { name ->
            var totalSize = 0L
            var appCount = 0
            var allActive = true
            for (packageName in enabledApps) {
                val profile = byApp[packageName]?.get(name)
                if (profile != null) {
                    totalSize += profile.size
                    appCount++
                    if (!profile.isActive) allActive = false
                } else {
                    allActive = false
                }
            }
            Profile(name, allActive && appCount == enabledApps.size, appCount, totalSize)
        }
    }
}
