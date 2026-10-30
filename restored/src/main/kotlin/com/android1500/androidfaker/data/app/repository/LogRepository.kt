package com.android1500.androidfaker.data.app.repository

import com.android1500.androidfaker.data.app.datasource.disk.dao.LogDao
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogRepository(
    private val dao: LogDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun observeRecent() = dao.observeRecent()
    fun observeByPackage(packageName: String) = dao.observeByPackage(packageName)
    fun observeByHook(hookName: String) = dao.observeByHook(hookName)
    fun observePackageNames() = dao.observePackageNames()
    fun observeHookNames() = dao.observeHookNames()
    suspend fun addLogs(logs: List<LogEntity>) = withContext(ioDispatcher) { dao.insert(logs) }
    suspend fun clearAll() = withContext(ioDispatcher) { dao.clear() }
}
