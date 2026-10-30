package com.android1500.androidfaker.data.app.datasource.disk.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import kotlinx.coroutines.flow.Flow

/** Method names reconstructed descriptively; queries and limits recovered from kc2. */
@Dao
interface LogDao {
    @Query("SELECT DISTINCT hookName FROM logs ORDER BY hookName")
    fun observeHookNames(): Flow<List<String>>

    @Query("SELECT DISTINCT packageName FROM logs ORDER BY packageName")
    fun observePackageNames(): Flow<List<String>>

    @Query("SELECT * FROM logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun observeByPackage(packageName: String): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE hookName = :hookName ORDER BY timestamp DESC")
    fun observeByHook(hookName: String): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 500): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(logs: List<LogEntity>)

    @Query("DELETE FROM logs")
    suspend fun clear()
}
