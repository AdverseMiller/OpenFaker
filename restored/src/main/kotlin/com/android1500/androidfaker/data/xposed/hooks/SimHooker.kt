package com.android1500.androidfaker.data.xposed.hooks

import android.os.Build
import com.android1500.androidfaker.data.loader.IHookBridge
import com.android1500.androidfaker.data.xposed.base.BaseHookItem
import com.android1500.androidfaker.data.xposed.base.HookLogSink

class SimHooker(private val logger: HookLogSink) : BaseHookItem() {
    override fun entry(classLoader: ClassLoader) {
        super.entry(classLoader)
        fun telephony() = classLoader.loadClass("android.telephony.TelephonyManager")
        attempt("getSubscriberId") {
            bridge.hook(telephony().getDeclaredMethod("getSubscriberId")).intercept(
                replacement("SIM-SubscriberID", { config.simSubscriberIdEnabled }, { config.simSubscriberId }))
        }
        attempt("getSimSerialNumber") {
            bridge.hook(telephony().getDeclaredMethod("getSimSerialNumber")).intercept(
                replacement("SIM-Serial", { config.simSerialEnabled }, { config.simSerial }))
        }
        attempt("operator methods") {
            val type = telephony()
            val callback = replacement("SIM-Operator", { config.simCountryEnabled }, { config.simOperatorName }, stringify = true)
            for (name in listOf("getNetworkOperatorName", "getSimOperatorName")) {
                bridge.hook(type.getDeclaredMethod(name)).intercept(callback)
            }
            bridge.hook(classLoader.loadClass("android.telephony.SubscriptionInfo").getDeclaredMethod("getCarrierName"))
                .intercept(callback)
        }
        attempt("country iso methods") {
            val type = telephony()
            for (name in listOf("getNetworkCountryIso", "getSimCountryIso")) {
                bridge.hook(type.getMethod(name)).intercept(
                    replacement("SIM-CountryISO", { config.simCountryEnabled }, { config.simCountryIso }))
            }
        }
        val types = mutableListOf("android.telephony.TelephonyManager", "android.telephony.SubscriptionInfo")
        if (Build.VERSION.SDK_INT >= 29) types += "android.telephony.CellIdentityNr"
        for (type in types) {
            // Class discovery errors escape this hook item in the original payload.
            for (method in classLoader.loadClass(type).declaredMethods) {
                val callback = when (method.name) {
                    "getNetworkOperator", "getSimOperator" -> replacement("SIM-MccMnc", { config.simCountryEnabled }, { config.simMccMnc })
                    "getMcc" -> numeric("SIM-MCC") { config.mcc }
                    "getMnc" -> numeric("SIM-MNC") { config.mnc }
                    else -> continue
                }
                attempt(method.name) { bridge.hook(method).intercept(callback) }
            }
        }
    }

    private fun attempt(label: String, install: () -> Unit) {
        runCatching(install).onFailure { logFailure("Hook failed [$label]", it) }
    }

    private fun replacement(label: String, enabled: () -> Boolean, value: () -> String?, stringify: Boolean = false) =
        IHookBridge.Interceptor { chain ->
            val original = chain.proceed()
            val old = if (stringify) original?.toString() else original as? String
            if (enabled() && old != value()) {
                logger.log(label, old, value())
                value()
            } else original
        }

    private fun numeric(label: String, value: () -> String?) = IHookBridge.Interceptor { chain ->
        val original = chain.proceed()
        if (config.simCountryEnabled) {
            // Keep original strict parsing: malformed configured MCC/MNC throws.
            val replacement = value()?.toInt()
            if (replacement != null && original?.toString() != replacement.toString()) {
                logger.log(label, original?.toString(), replacement.toString())
                return@Interceptor replacement
            }
        }
        original
    }
}
