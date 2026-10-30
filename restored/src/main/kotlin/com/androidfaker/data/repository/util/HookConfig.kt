package com.androidfaker.data.repository.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Field order, defaults, and availability semantics recovered from the APK serializer. */
@Serializable
data class HookConfig(
    val pkgName: String? = null,
    val imei1: String? = null,
    val imei1Enabled: Boolean = false,
    val imei2: String? = null,
    val imei2Enabled: Boolean = false,
    val androidId: String? = null,
    val androidIdEnabled: Boolean = false,
    val wifiMac: String? = null,
    val wifiMacEnabled: Boolean = false,
    val wifiSsid: String? = null,
    val wifiSsidEnabled: Boolean = false,
    val wifiBssid: String? = null,
    val wifiBssidEnabled: Boolean = false,
    val bluetoothMac: String? = null,
    val bluetoothMacEnabled: Boolean = false,
    val simSubscriberId: String? = null,
    val simSubscriberIdEnabled: Boolean = false,
    val simSerial: String? = null,
    val simSerialEnabled: Boolean = false,
    val mobileNo: String? = null,
    val mobileNoEnabled: Boolean = false,
    val simCountryIso: String? = null,
    val simCountryName: String? = null,
    val simOperatorName: String? = null,
    val simMccMnc: String? = null,
    val simCountryEnabled: Boolean = false,
    val simCountryLocked: Boolean = false,
    val hwSerial: String? = null,
    val hwSerialEnabled: Boolean = false,
    val adsId: String? = null,
    val adsIdEnabled: Boolean = false,
    val mediaDrm: String? = null,
    val mediaDrmEnabled: Boolean = false,
    val gsfId: String? = null,
    val gsfIdEnabled: Boolean = false,
    val appSetId: String? = null,
    val appSetIdEnabled: Boolean = false,
    val ipAddress: String? = null,
    val ipAddressEnabled: Boolean = false,
    val fingerprintEnabled: Boolean = false,
    val buildId: String? = null,
    val display: String? = null,
    val product: String? = null,
    val board: String? = null,
    val device: String? = null,
    val manufacturer: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val bootloader: String? = null,
    val radio: String? = null,
    val hardware: String? = null,
    val tag: String? = null,
    val type: String? = null,
    val fingerprint: String? = null,
    val incremental: String? = null,
    val releaseVersion: String? = null,
    val sdk: String? = null,
    val host: String? = null,
    val user: String? = null,
) {
    val mcc: String? get() = simMccMnc?.takeIf { it.length >= 3 }?.substring(0, 3)
    val mnc: String? get() = simMccMnc?.takeIf { it.length > 3 }?.substring(3)

    fun isAvailable(): Boolean = this != Empty.copy(pkgName = pkgName)

    fun toJson(): String = json.encodeToString(this)

    companion object {
        private val Empty = HookConfig()

        internal val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }
    }
}
