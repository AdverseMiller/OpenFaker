package com.android1500.androidfaker.data.app.datasource.disk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.android1500.androidfaker.data.app.datasource.disk.entity.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

/** Method names reconstructed descriptively from wp2/aq2. */
@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_app")
    fun observeAll(): Flow<List<InstalledAppEntity>>

    @Upsert
    suspend fun upsert(apps: List<InstalledAppEntity>)

    @Delete
    suspend fun delete(apps: List<InstalledAppEntity>): Int
}
