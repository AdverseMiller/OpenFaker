package com.androidfaker.data.app.model

import com.android1500.androidfaker.data.app.datasource.disk.entity.InstalledAppEntity
import kotlin.time.Instant

/** Recovered vp2 model; qualified name retained in AppListViewModel's cast diagnostic. */
data class InstalledApp(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val firstInstallTime: Instant?,
    val lastUpdateTime: Instant?,
    val isEnabled: Boolean,
    val isSystem: Boolean,
    val label: String,
) {
    constructor() : this("", "", 0L, 0, 0, null, null, true, false, "")
}

fun InstalledApp.toEntity() = InstalledAppEntity(packageName, versionName, versionCode,
    minSdkVersion, targetSdkVersion, firstInstallTime, lastUpdateTime, isEnabled, isSystem, label)

fun InstalledAppEntity.toInstalledApp() = InstalledApp(packageName, versionName, versionCode,
    minSdkVersion, targetSdkVersion, firstInstallTime, lastUpdateTime, isEnabled, isSystem, label)
