package com.android1500.androidfaker

import android.app.Application
import androidx.room.Room
import com.android1500.androidfaker.data.app.datasource.disk.database.AppDatabase
import com.android1500.androidfaker.data.app.repository.LogRepository
import com.android1500.androidfaker.data.app.repository.AppRepositoryImpl
import com.android1500.androidfaker.data.app.repository.AppRepository
import com.android1500.androidfaker.data.app.repository.InstalledAppCache
import com.android1500.androidfaker.data.app.datasource.disk.InstalledAppDiskSourceImpl
import com.android1500.androidfaker.data.platform.datasource.disk.AppDiskSourceImpl
import com.android1500.androidfaker.data.platform.datasource.disk.SettingsDiskSourceImpl
import com.android1500.androidfaker.data.platform.repository.SettingsRepositoryImpl
import com.android1500.androidfaker.data.platform.repository.SettingsRepository
import com.android1500.androidfaker.data.platform.repository.HookRepositoryImpl
import com.android1500.androidfaker.data.platform.repository.ProfileRepositoryImpl
import com.android1500.androidfaker.data.platform.repository.DeviceRepositoryImpl
import com.android1500.androidfaker.data.platform.repository.CarrierRepositoryImpl
import com.android1500.androidfaker.data.platform.datasource.disk.database.DeviceDatabase
import com.android1500.androidfaker.data.logger.LoggerHost
import com.android1500.androidfaker.data.platform.manager.LsposedServiceManager
import com.android1500.androidfaker.data.platform.datasource.disk.HookDiskSourceImpl
import kotlinx.serialization.json.Json
import com.tencent.mmkv.MMKV
import java.io.File

/** Application services and repositories shared by the screens and hook logger. */
open class App : Application(), LoggerHost {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
    // App and settings repositories share one persistent preference store.
    // Lazy initialization keeps unrelated startup paths from loading native MMKV.
    private val preferences: MMKV by lazy {
        MMKV.initialize(this, File(filesDir, "mmkv").absolutePath)
        checkNotNull(MMKV.mmkvWithID("af_user_backup")) { "Unable to open app preferences" }
    }
    open val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(SettingsDiskSourceImpl(preferences)) }
    val installedAppSource by lazy { InstalledAppDiskSourceImpl(packageName, packageManager) }
    open val appRepository: AppRepository by lazy {
        AppRepositoryImpl(AppDiskSourceImpl(preferences),
            InstalledAppCache(installedAppSource, database.installedAppDao()))
    }
    val profileRepository by lazy { ProfileRepositoryImpl(this) }
    val deviceDatabase by lazy { DeviceDatabase(this) }
    val deviceRepository by lazy { DeviceRepositoryImpl(deviceDatabase) }
    val carrierRepository by lazy { CarrierRepositoryImpl(deviceDatabase) }
    val hookRepository by lazy { HookRepositoryImpl(this, hookDiskSource, settingsRepository, json) }
    val lsposedServiceManager = LsposedServiceManager()
    val hookDiskSource by lazy {
        HookDiskSourceImpl(json,
            lsposedServiceManager.preferences(this, HookDiskSourceImpl.STORAGE_NAME))
    }

    override fun onCreate() {
        super.onCreate()
        lsposedServiceManager.init()
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "app_database").build()
    }
    override val logRepository: LogRepository by lazy { LogRepository(database.logDao()) }
}
