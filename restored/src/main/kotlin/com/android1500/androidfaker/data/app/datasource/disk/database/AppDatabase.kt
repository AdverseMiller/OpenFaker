package com.android1500.androidfaker.data.app.datasource.disk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android1500.androidfaker.data.app.datasource.disk.dao.InstalledAppDao
import com.android1500.androidfaker.data.app.datasource.disk.dao.LogDao
import com.android1500.androidfaker.data.app.datasource.disk.entity.InstalledAppEntity
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import com.android1500.androidfaker.data.app.util.InstantConverter

@Database(entities = [InstalledAppEntity::class, LogEntity::class], version = 2, exportSchema = true)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun logDao(): LogDao
}
