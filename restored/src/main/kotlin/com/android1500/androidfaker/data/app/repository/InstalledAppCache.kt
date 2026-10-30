package com.android1500.androidfaker.data.app.repository

import com.android1500.androidfaker.data.app.datasource.disk.InstalledAppDataSource
import com.android1500.androidfaker.data.app.datasource.disk.dao.InstalledAppDao
import com.androidfaker.data.app.model.InstalledApp
import com.androidfaker.data.app.model.toEntity
import com.androidfaker.data.app.model.toInstalledApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Recovered cache portion of AppRepositoryImpl, extracted while its preference
 * and command dependencies are being restored. Not a replacement for the full repository.
 */
class InstalledAppCache(
    private val source: InstalledAppDataSource,
    private val dao: InstalledAppDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun getApplicationList(): Flow<List<InstalledApp>> = dao.observeAll().map { rows ->
        rows.map { it.toInstalledApp() }
    }

    fun updateApplicationList(): Flow<Result<Unit>> = flow {
        val cached = getApplicationList().first()
        val installed = source.getApplicationList().first()
        val removed = cached.filter { old -> installed.none { it.packageName == old.packageName } }
        if (removed.isNotEmpty()) dao.delete(removed.map { it.toEntity() })
        val changed = installed - cached.toSet()
        if (changed.isNotEmpty()) dao.upsert(changed.map { it.toEntity() })
        emit(Result.success(Unit))
    }.catch { emit(Result.failure(it)) }.flowOn(ioDispatcher)
}
