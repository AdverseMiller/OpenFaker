package com.android1500.androidfaker.ui.platform.feature.apps

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.android1500.androidfaker.data.app.repository.SortType
import com.androidfaker.data.app.model.InstalledApp
import com.androidfaker.data.repository.util.HookConfig

/** State assembly extracted from the same flow combination; it does not initiate refreshes. */
internal fun projectAppListState(
    installedApps: List<InstalledApp>,
    showSystemApps: Boolean,
    sortType: SortType,
    searchQuery: String,
    configs: Map<String, HookConfig>,
    profileEnabledApps: Set<String>,
    dialogState: AppListState.DialogState?,
    packageManager: PackageManager,
): AppListState {
    val rows = filterAndSortInstalledApps(installedApps, showSystemApps, sortType,
        searchQuery, configs, profileEnabledApps).map { app ->
        AppItem(app.packageName, app.versionName, findPackageInfo(packageManager, app.packageName),
            app.firstInstallTime, app.lastUpdateTime, app.isSystem, app.label)
    }
    return AppListState(configs.filterValues { it.isAvailable() }.keys, profileEnabledApps,
        rows, searchQuery, sortType, showSystemApps, isRefreshing = false, isLoading = false,
        dialogState = dialogState)
}

@Suppress("DEPRECATION")
private fun findPackageInfo(pm: PackageManager, packageName: String): PackageInfo? = try {
    if (Build.VERSION.SDK_INT >= 33) pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    else pm.getPackageInfo(packageName, 0)
} catch (_: PackageManager.NameNotFoundException) {
    null
}

/** Extracted filtering/sorting portion of AppListViewModel's six-flow combination. */
internal fun filterAndSortInstalledApps(
    installedApps: List<InstalledApp>,
    showSystemApps: Boolean,
    sortType: SortType,
    searchQuery: String,
    configs: Map<String, HookConfig>,
    profileEnabledApps: Set<String>,
): List<InstalledApp> {
    val configured = configs.filterValues { it.isAvailable() }.keys
    val priority = compareByDescending<InstalledApp> {
        (if (it.packageName in configured) 2 else 0) +
            (if (it.packageName in profileEnabledApps) 1 else 0)
    }
    val secondary = when (sortType) {
        SortType.NAME_ASC -> compareBy<InstalledApp> { it.label }
        SortType.NAME_DESC -> compareByDescending<InstalledApp> { it.label }
        SortType.INSTALL_TIME -> compareByDescending<InstalledApp> { it.firstInstallTime }
        SortType.LAST_UPDATE_TIME -> compareByDescending<InstalledApp> { it.lastUpdateTime }
    }
    return installedApps.filter {
        (showSystemApps || !it.isSystem) &&
            (searchQuery.isBlank() || it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true))
    }.sortedWith(priority.then(secondary))
}
