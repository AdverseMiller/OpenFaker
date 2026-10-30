package com.android1500.androidfaker.data.xposed.base

import com.android1500.androidfaker.data.loader.IHookBridge
import com.androidfaker.data.repository.util.HookConfig

abstract class BaseHookItem {
    var isLoad = false
        private set
    val simpleName: String get() = javaClass.simpleName
    lateinit var config: HookConfig
    lateinit var bridge: IHookBridge

    open fun entry(classLoader: ClassLoader) { isLoad = true }

    fun logFailure(label: String, t: Throwable) {
        try { bridge.log(5, simpleName, "$label: ${t.message}", t) } catch (_: Throwable) { }
    }
}

/** Explicit boundary to the original LoggerProxy, whose IPC transport is still pending. */
fun interface HookLogSink {
    fun log(hookName: String, originalValue: String?, spoofedValue: String?)
}
