package com.android1500.androidfaker.data.app.repository

import com.android1500.androidfaker.data.platform.datasource.disk.AppDiskSource
import com.android1500.androidfaker.data.platform.util.LibsuRootCommandExecutor
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor
import com.androidfaker.data.app.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    var sortType: SortType
    val sortTypeFlow: Flow<SortType>
    val profileEnabledApps: Set<String>
    val profileEnabledAppsFlow: Flow<Set<String>>
    fun setProfileEnabled(packageName: String, enabled: Boolean)
    fun getApplicationList(): Flow<List<InstalledApp>>
    fun updateApplicationList(): Flow<Result<Unit>>
    suspend fun clearAppData(packageName: String)
    suspend fun forceStop(packageName: String)
    suspend fun uninstall(packageName: String)
}

class AppRepositoryImpl(
    private val disk: AppDiskSource,
    private val cache: InstalledAppCache,
    private val commands: RootCommandExecutor = LibsuRootCommandExecutor(),
) : AppRepository {
    override var sortType: SortType
        get() = disk.sortType
        set(value) { disk.sortType = value }
    override val sortTypeFlow get() = disk.sortTypeFlow
    override val profileEnabledApps get() = disk.profileEnabledApps
    override val profileEnabledAppsFlow get() = disk.profileEnabledAppsFlow

    override fun setProfileEnabled(packageName: String, enabled: Boolean) {
        val updated = disk.profileEnabledApps.toMutableSet()
        if (enabled) updated.add(packageName) else updated.remove(packageName)
        disk.profileEnabledApps = updated.toSet()
    }

    override fun getApplicationList() = cache.getApplicationList()
    override fun updateApplicationList() = cache.updateApplicationList()

    // Preserve the APK's Unit-returning behavior: nonzero exit status is not thrown.
    override suspend fun clearAppData(packageName: String) {
        commands.execute("pm clear ${checkedPackageName(packageName)}")
    }
    override suspend fun forceStop(packageName: String) {
        commands.execute("am force-stop ${checkedPackageName(packageName)}")
    }
    override suspend fun uninstall(packageName: String) {
        commands.execute("pm uninstall ${checkedPackageName(packageName)}")
    }

    private fun checkedPackageName(value: String): String {
        // Reconstruction hardening: only accept package-name characters before shell interpolation.
        require(value.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*"))) {
            "Invalid package name"
        }
        return value
    }
}
