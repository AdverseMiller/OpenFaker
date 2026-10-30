package com.android1500.androidfaker.data.app.datasource.disk.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "installed_app", indices = [Index("package_name")])
data class InstalledAppEntity(
    @PrimaryKey @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "version_name") val versionName: String,
    @ColumnInfo(name = "version_code") val versionCode: Long,
    @ColumnInfo(name = "min_sdk_version") val minSdkVersion: Int,
    @ColumnInfo(name = "target_sdk_version") val targetSdkVersion: Int,
    @ColumnInfo(name = "first_install_time") val firstInstallTime: Instant?,
    @ColumnInfo(name = "last_update_time") val lastUpdateTime: Instant?,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "is_system") val isSystem: Boolean,
    val label: String,
) {
    constructor() : this("", "", 0L, 0, 0, null, null, true, false, "")
}
