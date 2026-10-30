package com.android1500.androidfaker.ui.platform.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Read the installed package so version labels also follow custom builds. */
@Composable
@Suppress("DEPRECATION")
internal fun installedAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName ?: info.versionCode.toString()
    }
}
