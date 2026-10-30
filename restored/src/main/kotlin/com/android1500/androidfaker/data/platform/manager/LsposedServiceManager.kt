package com.android1500.androidfaker.data.platform.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Narrow adapter seam for testing service events without a live Xposed framework. */
interface ModuleService {
    val frameworkName: String
    val frameworkVersion: String
    fun preferences(name: String): SharedPreferences
    fun scope(): List<String>
    fun removeScope(packages: List<String>)
    fun requestScope(packages: List<String>, approved: (List<String>) -> Unit, failed: (String) -> Unit)
}

sealed interface ServiceConnection {
    data object Connecting : ServiceConnection
    data class Connected(val service: ModuleService) : ServiceConnection
    data object Disconnected : ServiceConnection
}

class LsposedServiceManager internal constructor(private val register: (Listener) -> Unit) {
    interface Listener {
        fun bound(service: ModuleService)
        fun died(service: ModuleService)
    }

    constructor() : this({ listener ->
        val adapters = IdentityHashMap<XposedService, ModuleService>()
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                val adapter = synchronized(adapters) { adapters.getOrPut(service) { FrameworkService(service) } }
                listener.bound(adapter)
            }
            override fun onServiceDied(service: XposedService) {
                val adapter = synchronized(adapters) { adapters.remove(service) }
                if (adapter != null) listener.died(adapter)
            }
        })
    })

    private val initialized = AtomicBoolean(false)
    private val mutableState = MutableStateFlow<ServiceConnection>(ServiceConnection.Connecting)
    val state = mutableState.asStateFlow()
    val service: ModuleService? get() = (state.value as? ServiceConnection.Connected)?.service

    @Synchronized fun init() {
        if (!initialized.compareAndSet(false, true)) return
        register(object : Listener {
            override fun bound(service: ModuleService) {
                synchronized(this@LsposedServiceManager) {
                    val current = this@LsposedServiceManager.service
                    if (current != null) {
                        Log.w(TAG, "Already connected to ${current.frameworkName}. Ignoring ${service.frameworkName}.")
                        return
                    }
                    mutableState.value = ServiceConnection.Connected(service)
                    Log.i(TAG, "LSPosed service connected: ${service.frameworkName} v${service.frameworkVersion}")
                }
            }
            override fun died(service: ModuleService) {
                synchronized(this@LsposedServiceManager) {
                    if (this@LsposedServiceManager.service === service) {
                        Log.w(TAG, "LSPosed service (${service.frameworkName}) died.")
                        mutableState.value = ServiceConnection.Disconnected
                    }
                }
            }
        })
        Log.i(TAG, "Service initialized and listener registered.")
    }

    /** Original selection is made when the disk source is first constructed; no migration is implied. */
    fun preferences(context: Context, name: String): SharedPreferences =
        service?.preferences(name) ?: context.getSharedPreferences(name, Context.MODE_PRIVATE)

    suspend fun getScope(): List<String>? = withContext(Dispatchers.IO) {
        val current = service
        if (current == null) { Log.e(TAG, "getScope: $UNAVAILABLE"); return@withContext null }
        try { current.scope() } catch (error: Exception) { Log.e(TAG, "getScope failed", error); null }
    }

    suspend fun removeScope(packageName: String): String? = withContext(Dispatchers.IO) {
        val current = service
        if (current == null) { Log.e(TAG, "removeScope: $UNAVAILABLE"); return@withContext UNAVAILABLE }
        try { current.removeScope(listOf(packageName)); null }
        catch (error: Exception) { Log.e(TAG, "removeScope failed", error); error.message }
    }

    suspend fun addScope(packageName: String, approved: (List<String>) -> Unit, failed: (String) -> Unit) = withContext(Dispatchers.Main) {
        val current = service
        if (current == null) { failed(UNAVAILABLE); return@withContext }
        try {
            current.requestScope(listOf(packageName), approved) { failed("Failed to enable $packageName: $it") }
        } catch (error: Exception) {
            Log.e(TAG, "addScope failed", error)
            failed(error.message ?: "Unknown error")
        }
    }

    private class FrameworkService(private val delegate: XposedService) : ModuleService {
        override val frameworkName get() = delegate.frameworkName
        override val frameworkVersion get() = delegate.frameworkVersion
        override fun preferences(name: String) = delegate.getRemotePreferences(name)
        override fun scope() = delegate.scope
        override fun removeScope(packages: List<String>) = delegate.removeScope(packages)
        override fun requestScope(packages: List<String>, approved: (List<String>) -> Unit, failed: (String) -> Unit) {
            delegate.requestScope(packages, object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(packages: List<String>) = approved(packages)
                override fun onScopeRequestFailed(message: String) = failed(message)
            })
        }
    }
    private companion object {
        const val TAG = "LsposedServiceManager"
        const val UNAVAILABLE = "LSPosed service not available."
    }
}
