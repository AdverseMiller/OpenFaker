package com.android1500.androidfaker.ui.platform.feature.hook

import com.androidfaker.data.repository.util.HookConfig

/** Pure helpers recovered from HookViewModelKt; no persistence, hooks or root commands. */
internal fun HookConfig.withHookEnabled(id: String, enabled: Boolean): HookConfig = when (id) {
    "imei" -> copy(imei1Enabled = enabled, imei2Enabled = enabled)
    "android_id" -> copy(androidIdEnabled = enabled)
    "wifi_mac" -> copy(wifiMacEnabled = enabled)
    "wifi_ssid" -> copy(wifiSsidEnabled = enabled)
    "wifi_bssid" -> copy(wifiBssidEnabled = enabled)
    "bluetooth_mac" -> copy(bluetoothMacEnabled = enabled)
    "sim_subscriber_id" -> copy(simSubscriberIdEnabled = enabled)
    "sim_serial" -> copy(simSerialEnabled = enabled)
    "mobile_no" -> copy(mobileNoEnabled = enabled)
    "sim_country" -> copy(simCountryEnabled = enabled)
    "hw_serial" -> copy(hwSerialEnabled = enabled)
    "ads_id" -> copy(adsIdEnabled = enabled)
    "media_drm" -> copy(mediaDrmEnabled = enabled)
    "gsf_id" -> copy(gsfIdEnabled = enabled)
    "app_set_id" -> copy(appSetIdEnabled = enabled)
    "ip_address" -> copy(ipAddressEnabled = enabled)
    else -> this
}

/** The original generic editor changes only IMEI slot 1; slot 2 has a separate handler. */
internal fun HookConfig.withHookValue(id: String, value: String): HookConfig = when (id) {
    "imei" -> copy(imei1 = value)
    "android_id" -> copy(androidId = value)
    "wifi_mac" -> copy(wifiMac = value)
    "wifi_ssid" -> copy(wifiSsid = value)
    "wifi_bssid" -> copy(wifiBssid = value)
    "bluetooth_mac" -> copy(bluetoothMac = value)
    "sim_subscriber_id" -> copy(simSubscriberId = value)
    "sim_serial" -> copy(simSerial = value)
    "mobile_no" -> copy(mobileNo = value)
    "sim_country" -> copy(simCountryIso = value)
    "hw_serial" -> copy(hwSerial = value)
    "ads_id" -> copy(adsId = value)
    "media_drm" -> copy(mediaDrm = value)
    "gsf_id" -> copy(gsfId = value)
    "app_set_id" -> copy(appSetId = value)
    "ip_address" -> copy(ipAddress = value)
    else -> this
}

internal fun HookConfig.withDeviceModel(model: Model, osVersion: String, enabled: Boolean): HookConfig = copy(
    fingerprintEnabled = enabled,
    buildId = model.buildId, display = model.display, product = model.product, board = model.board,
    device = model.device, manufacturer = model.manufacture, brand = model.brand, model = model.model,
    bootloader = model.bootloader, radio = model.radio, hardware = model.hardware, tag = model.tag,
    type = model.type, fingerprint = model.fingerprint, incremental = model.incremental,
    releaseVersion = osVersion, sdk = model.sdk, host = model.host, user = model.user,
)

internal val defaultHookData: List<HookData> = listOf(
    HookData("imei", "IMEI"), HookData("android_id", "Android ID"),
    HookData("wifi_mac", "WiFi MAC"), HookData("wifi_ssid", "WiFi SSID"),
    HookData("wifi_bssid", "WiFi BSSID"), HookData("bluetooth_mac", "Bluetooth MAC"),
    HookData("sim_subscriber_id", "SIM Subscriber ID"), HookData("sim_serial", "SIM Serial"),
    HookData("mobile_no", "Mobile Number"), HookData("sim_country", "SIM Country"),
    HookData("hw_serial", "Hardware Serial"), HookData("ads_id", "Advertising ID"),
    HookData("media_drm", "Media DRM ID"), HookData("gsf_id", "GSF ID"),
    HookData("app_set_id", "App Set ID"), HookData("ip_address", "IP Address"),
)

/** Repository observation from HookViewModel's OooOOO0 coroutine, including null-config semantics. */
internal fun HookState.withObservedConfig(config: HookConfig?): HookState {
    if (config == null) return copy(hookData = defaultHookData)
    val rows = listOf(
        HookData("imei", "IMEI", config.imei1,
            if (simSlotCount >= 2) config.imei2 ?: "" else "Not set", config.imei1Enabled),
        HookData("android_id", "Android ID", config.androidId, checked = config.androidIdEnabled),
        HookData("wifi_mac", "WiFi MAC", config.wifiMac, checked = config.wifiMacEnabled),
        HookData("wifi_ssid", "WiFi SSID", config.wifiSsid, checked = config.wifiSsidEnabled),
        HookData("wifi_bssid", "WiFi BSSID", config.wifiBssid, checked = config.wifiBssidEnabled),
        HookData("bluetooth_mac", "Bluetooth MAC", config.bluetoothMac, checked = config.bluetoothMacEnabled),
        HookData("sim_subscriber_id", "SIM Subscriber ID", config.simSubscriberId, checked = config.simSubscriberIdEnabled),
        HookData("sim_serial", "SIM Serial", config.simSerial, checked = config.simSerialEnabled),
        HookData("mobile_no", "Mobile Number", config.mobileNo, checked = config.mobileNoEnabled),
        HookData("sim_country", "SIM Country", config.simCountryName ?: config.simCountryIso, checked = config.simCountryEnabled),
        HookData("hw_serial", "Hardware Serial", config.hwSerial, checked = config.hwSerialEnabled),
        HookData("ads_id", "Advertising ID", config.adsId, checked = config.adsIdEnabled),
        HookData("media_drm", "Media DRM ID", config.mediaDrm, checked = config.mediaDrmEnabled),
        HookData("gsf_id", "GSF ID", config.gsfId, checked = config.gsfIdEnabled),
        HookData("app_set_id", "App Set ID", config.appSetId, checked = config.appSetIdEnabled),
        HookData("ip_address", "IP Address", config.ipAddress, checked = config.ipAddressEnabled),
    )
    return copy(hookData = rows, isFingerprintSpoofEnabled = config.fingerprintEnabled,
        isSimCountryLocked = config.simCountryLocked)
}
