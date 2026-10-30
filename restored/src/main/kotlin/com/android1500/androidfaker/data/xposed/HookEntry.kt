package com.android1500.androidfaker.data.xposed

import com.android1500.androidfaker.data.loader.IHookBridge
import com.android1500.androidfaker.data.loader.IHookEntry
import com.android1500.androidfaker.data.xposed.base.HookLogSink
import com.android1500.androidfaker.data.xposed.hooks.*
import com.androidfaker.data.repository.util.HookConfig

class HookEntry(logger: HookLogSink) : IHookEntry {
    private val items = listOf(DeviceHooker(logger), IdentifierHooker(logger), SimHooker(logger), WifiHooker(logger), IpAddressHooker(logger))

    override fun hook(bridge: IHookBridge, classLoader: ClassLoader, config: HookConfig) {
        for (item in items) {
            try {
                item.bridge = bridge
                item.config = config
                item.entry(classLoader)
            } catch (error: Throwable) {
                bridge.log(5, "HookEntry", "${item::class.simpleName} failed: ${error.message}", error)
            }
        }
    }
}
