package com.android1500.androidfaker.data.logger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.android1500.androidfaker.ILoggerService
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import com.android1500.androidfaker.data.app.repository.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Explicit application dependency replaces the original Koin lookup. */
interface LoggerHost { val logRepository: LogRepository }

class LoggerReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != LoggerProtocol.REQUEST) return
        val target = intent.getStringExtra("target_app_pkg") ?: return
        val repository = (context.applicationContext as LoggerHost).logRepository
        val binder = object : ILoggerService.Stub() {
            override fun addLogs(logs: List<LogEntity>?) {
                if (!logs.isNullOrEmpty()) scope.launch { repository.addLogs(logs) }
            }
        }
        context.sendBroadcast(Intent(LoggerProtocol.RECEIVE).setPackage(target).putExtra(
            "binder_bundle", Bundle().apply { putBinder("logger_binder", binder) },
        ))
    }
}

internal object LoggerProtocol {
    const val REQUEST = "com.android1500.androidfaker.ACTION_REQUEST_BINDER"
    const val RECEIVE = "com.android1500.androidfaker.ACTION_RECEIVE_BINDER"
}
