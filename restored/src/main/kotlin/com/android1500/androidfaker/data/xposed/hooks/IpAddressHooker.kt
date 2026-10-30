package com.android1500.androidfaker.data.xposed.hooks

import com.android1500.androidfaker.data.loader.IHookBridge
import com.android1500.androidfaker.data.xposed.base.BaseHookItem
import com.android1500.androidfaker.data.xposed.base.HookLogSink
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections

class IpAddressHooker(private val logger: HookLogSink) : BaseHookItem() {
    override fun entry(classLoader: ClassLoader) {
        super.entry(classLoader)
        install(classLoader, "android.net.wifi.WifiInfo", "getIpAddress") { chain ->
            val original = chain.proceed()
            if (enabled()) {
                try {
                    val old = original as? Int
                    val replacement = ByteBuffer.wrap(InetAddress.getByName(config.ipAddress).address)
                        .order(ByteOrder.LITTLE_ENDIAN).int
                    if (old == replacement) return@install original
                    val previous = old?.let {
                        InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(it).array()).hostAddress
                    }
                    logger.log("WifiIP", previous, config.ipAddress)
                    return@install replacement
                } catch (_: Throwable) { }
            }
            original
        }
        install(classLoader, "java.net.NetworkInterface", "getInetAddresses") { chain ->
            val original = chain.proceed()
            if (enabled()) {
                try {
                    val address = InetAddress.getByName(config.ipAddress)
                    if (address is Inet4Address) {
                        logger.log("NetworkIP", null, config.ipAddress)
                        return@install Collections.enumeration(listOf(address))
                    }
                } catch (_: Throwable) { }
            }
            original
        }
        install(classLoader, "java.net.InetAddress", "getHostAddress") { chain ->
            val original = chain.proceed()
            val address = original as? String
            if (enabled() && address != null && ':' !in address && !address.startsWith("127.") && address != config.ipAddress) {
                logger.log("HostIP", address, config.ipAddress)
                config.ipAddress
            } else original
        }
    }

    private fun enabled() = config.ipAddressEnabled && !config.ipAddress.isNullOrEmpty()

    private fun install(loader: ClassLoader, type: String, name: String, callback: IHookBridge.Interceptor) {
        runCatching { bridge.hook(loader.loadClass(type).getDeclaredMethod(name)).intercept(callback) }
            .onFailure { logFailure("Hook failed [$name]", it) }
    }
}
