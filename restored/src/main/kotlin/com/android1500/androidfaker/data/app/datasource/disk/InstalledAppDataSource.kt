package com.android1500.androidfaker.data.app.datasource.disk

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.androidfaker.data.app.model.InstalledApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Instant

interface InstalledAppDataSource {
    fun getApplicationList(): Flow<List<InstalledApp>>
}

/** cq2 and cq2$OooO00o. IO dispatcher is injected directly pending original DI restoration. */
class InstalledAppDiskSourceImpl(
    private val appPackageName: String,
    private val pm: PackageManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : InstalledAppDataSource {
    @Suppress("DEPRECATION")
    override fun getApplicationList(): Flow<List<InstalledApp>> = flow {
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            pm.getInstalledPackages(0)
        }
        emit(packages.filter { it.packageName != appPackageName }
            .distinctBy { it.packageName }.map(::toInstalledApp))
    }.flowOn(ioDispatcher)

    @Suppress("DEPRECATION")
    internal fun toInstalledApp(info: PackageInfo): InstalledApp {
        val application = info.applicationInfo
        return InstalledApp(
            packageName = info.packageName,
            versionName = info.versionName ?: "",
            versionCode = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong(),
            minSdkVersion = application?.minSdkVersion ?: 0,
            targetSdkVersion = application?.targetSdkVersion ?: 0,
            firstInstallTime = Instant.fromEpochMilliseconds(info.firstInstallTime),
            lastUpdateTime = Instant.fromEpochMilliseconds(info.lastUpdateTime),
            isEnabled = application?.enabled ?: false,
            isSystem = isSystemPackage(info.packageName),
            // The original uses String.valueOf(nullableLabel), producing literal "null".
            label = application?.loadLabel(pm).toString(),
        )
    }

    @Suppress("DEPRECATION")
    private fun isSystemPackage(packageName: String?): Boolean {
        if (packageName == null || packageName.trim().isEmpty()) return false
        return try {
            val application = if (Build.VERSION.SDK_INT >= 33) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                pm.getApplicationInfo(packageName, 0)
            }
            application.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (_: PackageManager.NameNotFoundException) {
            Log.w("InstalledAppDiskSource", "$packageName is not installed.")
            false
        }
    }
}
