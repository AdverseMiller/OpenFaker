package com.android1500.androidfaker.data.xposed.hooks

import com.android1500.androidfaker.data.xposed.base.BaseHookItem
import com.android1500.androidfaker.data.xposed.base.HookLogSink

class DeviceHooker(private val logger: HookLogSink) : BaseHookItem() {
    override fun entry(classLoader: ClassLoader) {
        super.entry(classLoader)
        if (!config.fingerprintEnabled) return
        val fields = linkedMapOf(
            "ID" to config.buildId, "DISPLAY" to config.display, "PRODUCT" to config.product,
            "BOARD" to config.board, "DEVICE" to config.device, "MANUFACTURER" to config.manufacturer,
            "BRAND" to config.brand, "MODEL" to config.model, "BOOTLOADER" to config.bootloader,
            "RADIO" to config.radio, "HARDWARE" to config.hardware, "TAGS" to config.tag,
            "TYPE" to config.type, "FINGERPRINT" to config.fingerprint, "HOST" to config.host,
            "USER" to config.user, "SERIAL" to config.hwSerial,
        )
        // The APK catches around each entire field group, not each individual field.
        try {
            updateFields(classLoader.loadClass("android.os.Build"), fields, "Build", skipEmpty = true)
        } catch (_: Throwable) { }
        try {
            updateFields(classLoader.loadClass("android.os.Build\$VERSION"), linkedMapOf(
                "INCREMENTAL" to config.incremental, "RELEASE" to config.releaseVersion, "SDK" to config.sdk,
            ), "Version", skipEmpty = false)
        } catch (_: Throwable) { }
        if (config.radio.isNullOrEmpty()) return
        runCatching {
            bridge.hook(classLoader.loadClass("android.os.Build").getDeclaredMethod("getRadioVersion"))
                .intercept { chain ->
                    val original = chain.proceed()
                    if ((original as? String) == config.radio) original else {
                        logger.log("RadioVersion", original as? String, config.radio)
                        config.radio
                    }
                }
        }.onFailure { logFailure("Hook failed [Build.getRadioVersion]", it) }
    }

    private fun updateFields(type: Class<*>, values: Map<String, String?>, prefix: String, skipEmpty: Boolean) {
        for ((name, value) in values) {
            if (value == null || (skipEmpty && value.isEmpty())) continue
            val field = type.getDeclaredField(name).apply { isAccessible = true }
            val original = field.get(null)?.toString()
            if (original != value) {
                field.set(null, value)
                logger.log("$prefix.$name", original, value)
            }
        }
    }
}
