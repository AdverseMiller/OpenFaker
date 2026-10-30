package com.android1500.androidfaker.ui.platform.feature.apps

import android.content.pm.PackageInfo
import kotlin.time.Instant

/** Recovered wh model. The original package is not retained in this class. */
data class AppItem(
    val packageName: String = "",
    val versionName: String = "",
    val packageInfo: PackageInfo? = null,
    val firstInstallTime: Instant? = null,
    val lastUpdateTime: Instant? = null,
    val isSystem: Boolean = false,
    val label: String = "",
    // R8 eliminated this always-null field. Its original declared type is unknown.
    val icon: Nothing? = null,
)
