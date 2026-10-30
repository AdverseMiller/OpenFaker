package com.android1500.androidfaker.data.logger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.android1500.androidfaker.ILoggerService
import com.android1500.androidfaker.data.app.datasource.disk.entity.LogEntity
import com.android1500.androidfaker.data.xposed.base.HookLogSink
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Module-owned instance; module package is injected to support the separate recovery APK. */
class LoggerProxy(
    private val modulePackageName: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : HookLogSink {
    private enum class State { DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING }
    private val state = AtomicReference(State.DISCONNECTED)
    private val initialized = AtomicBoolean(false)
    private val queue = Channel<LogEntity>(1000, BufferOverflow.DROP_OLDEST)
    @Volatile private var currentPackageName: String? = null
    @Volatile private var service: ILoggerService? = null

    fun init(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            currentPackageName = context.packageName
            scope.launch {
                while (isActive) { flushQueue(); delay(1000) }
            }
            connectToLogger(context)
        }
    }

    @Suppress("DEPRECATION")
    private fun connectToLogger(original: Context) {
        val context = original.applicationContext ?: original
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(unused: Context, intent: Intent) {
                try { context.unregisterReceiver(this) } catch (_: Exception) {
                    Log.e("LoggerProxy", "failed to unregister receiver")
                }
                val binder = intent.getBundleExtra("binder_bundle")?.getBinder("logger_binder")
                if (binder == null) { Log.w("LoggerProxy", "binder is null in broadcast payload."); return }
                service = ILoggerService.asInterface(binder)
                state.set(State.CONNECTED)
                Log.i("LoggerProxy", "successfully got binder via broadcast.")
                fun disconnected() {
                    service = null
                    state.set(State.DISCONNECTED)
                    connectToLogger(context)
                }
                try { binder.linkToDeath({ disconnected() }, 0) } catch (_: RemoteException) { disconnected() }
            }
        }
        val filter = IntentFilter(LoggerProtocol.RECEIVE)
        if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        else context.registerReceiver(receiver, filter)
        context.sendBroadcast(Intent(LoggerProtocol.REQUEST).setPackage(modulePackageName)
            .putExtra("target_app_pkg", context.packageName))
    }

    internal fun flushQueue() {
        if (state.get() != State.CONNECTED) return
        val batch = ArrayList<LogEntity>()
        while (batch.size < 500) batch += queue.tryReceive().getOrNull() ?: break
        if (batch.isNotEmpty()) {
            try { service?.addLogs(batch) } catch (error: RemoteException) {
                // The original drains before sending and does not requeue a failed batch.
                Log.e("LoggerProxy", "Failed to send log batch. Error: ${error.message}")
            }
        }
    }

    override fun log(hookName: String, originalValue: String?, spoofedValue: String?) {
        val name = currentPackageName
        if (initialized.get() && name != null) {
            queue.trySend(LogEntity(hookName = hookName, packageName = name,
                originalValue = originalValue.orEmpty(), spoofedValue = spoofedValue.orEmpty()))
        }
    }
}
