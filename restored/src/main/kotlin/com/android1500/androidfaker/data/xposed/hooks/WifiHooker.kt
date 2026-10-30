package com.android1500.androidfaker.data.xposed.hooks

import com.android1500.androidfaker.data.xposed.base.BaseHookItem
import com.android1500.androidfaker.data.xposed.base.HookLogSink

/** Payload singleton reconstructed as an explicitly owned instance for logger injection. */
class WifiHooker(private val logger: HookLogSink) : BaseHookItem() {
    override fun entry(classLoader: ClassLoader) {
        super.entry(classLoader)
        val type = classLoader.loadClass("android.net.wifi.WifiInfo")
        install(type, "getMacAddress", "WifiMAC", { config.wifiMacEnabled }, { config.wifiMac })
        install(type, "getSSID", "WifiSSID", { config.wifiSsidEnabled }, { config.wifiSsid })
        install(type, "getBSSID", "WifiBSSID", { config.wifiBssidEnabled }, { config.wifiBssid })
    }

    private fun install(type: Class<*>, method: String, label: String, enabled: () -> Boolean, value: () -> String?) {
        runCatching {
            bridge.hook(type.getMethod(method)).intercept { chain ->
                val original = chain.proceed()
                val replacement = value()
                if (enabled() && !replacement.isNullOrEmpty() && (original as? String) != replacement) {
                    logger.log(label, original as? String, replacement)
                    replacement
                } else original
            }
        }.onFailure { logFailure("Hook failed [$method]", it) }
    }
}
