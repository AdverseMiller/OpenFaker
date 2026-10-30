package com.android1500.androidfaker.ui.platform.feature.hook

import com.android1500.androidfaker.data.platform.repository.HookRepository
import com.androidfaker.data.repository.util.HookConfig

/** Single-field action from HookViewModel.randomizeField; does not enable disabled fields. */
internal class HookFieldRandomizer(
    private val packageName: String,
    private val hooks: HookRepository,
    private val simSlotCount: () -> Int,
    private val generator: HookValueGenerator = HookValueGenerator(),
) {
    private fun current() = hooks.get(packageName) ?: HookConfig(pkgName = packageName)

    fun randomize(id: String) {
        val config = current()
        val enabled = when (id) {
            "imei" -> config.imei1Enabled
            "android_id" -> config.androidIdEnabled
            "wifi_mac" -> config.wifiMacEnabled
            "wifi_ssid" -> config.wifiSsidEnabled
            "wifi_bssid" -> config.wifiBssidEnabled
            "bluetooth_mac" -> config.bluetoothMacEnabled
            "sim_subscriber_id" -> config.simSubscriberIdEnabled
            "sim_serial" -> config.simSerialEnabled
            "mobile_no" -> config.mobileNoEnabled
            "sim_country" -> config.simCountryEnabled
            "hw_serial" -> config.hwSerialEnabled
            "ads_id" -> config.adsIdEnabled
            "media_drm" -> config.mediaDrmEnabled
            "gsf_id" -> config.gsfIdEnabled
            "app_set_id" -> config.appSetIdEnabled
            "ip_address" -> config.ipAddressEnabled
            else -> false
        }
        if (!enabled) return
        if (id == "imei") {
            var changed = current().copy(imei1 = generator.imei())
            if (simSlotCount() >= 2) changed = changed.copy(imei2 = generator.imei())
            hooks.put(changed)
            return
        }
        val value = when (id) {
            "android_id" -> generator.hex(16)
            "wifi_mac", "wifi_bssid", "bluetooth_mac" -> generator.mac()
            "wifi_ssid" -> "WiFi_${generator.hex(6)}"
            "sim_subscriber_id" -> generator.decimal(15)
            "sim_serial" -> generator.decimal(20)
            "mobile_no" -> generator.phoneNumber()
            "hw_serial" -> generator.hardwareSerial()
            "ads_id", "app_set_id" -> generator.uuidShape()
            "media_drm" -> generator.hex(32)
            "gsf_id" -> generator.gsfId()
            "ip_address" -> generator.ipv4()
            else -> return // SIM-country randomization has its own catalog action.
        }
        hooks.put(current().withHookValue(id, value))
    }
}
