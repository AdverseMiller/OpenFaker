package com.android1500.androidfaker.data.xposed.hooks

import android.content.ContentResolver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.CancellationSignal
import android.os.Parcel
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.android1500.androidfaker.data.loader.IHookBridge
import com.android1500.androidfaker.data.xposed.base.BaseHookItem
import com.android1500.androidfaker.data.xposed.base.HookLogSink
import java.util.Locale

class IdentifierHooker(private val logger: HookLogSink) : BaseHookItem() {
    override fun entry(classLoader: ClassLoader) {
        super.entry(classLoader)
        fun install(type: String, name: String, label: String, vararg parameters: Class<*>, callback: IHookBridge.Interceptor) {
            attempt(label) { bridge.hook(classLoader.loadClass(type).getDeclaredMethod(name, *parameters)).intercept(callback) }
        }
        install("android.provider.Settings\$Secure", "getString", "Settings.Secure.getString", ContentResolver::class.java, String::class.java, callback = secure(false))
        install("android.provider.Settings\$Secure", "getStringForUser", "Settings.Secure.getStringForUser", ContentResolver::class.java, String::class.java, Int::class.javaPrimitiveType!!, callback = secure(true))
        install("android.os.Build", "getSerial", "Build.getSerial", callback = stringResult("HWSerial", { config.hwSerialEnabled }, { config.hwSerial }))
        attempt("TelephonyManager.getImei") {
            val type = classLoader.loadClass("android.telephony.TelephonyManager")
            bridge.hook(type.getDeclaredMethod("getImei")).intercept(stringResult("IMEI", { config.imei1Enabled }, { config.imei1 }))
            bridge.hook(type.getDeclaredMethod("getImei", Int::class.javaPrimitiveType)).intercept { chain ->
                val original = chain.proceed()
                if (!config.imei1Enabled) original else {
                    val slot = chain.args[0] as Int
                    if (slot == 0) replace(original, "IMEI-Slot0", true, config.imei1)
                    else replace(original, "IMEI-Slot1", config.imei2Enabled, config.imei2)
                }
            }
        }
        install("android.telephony.TelephonyManager", "getDeviceId", "TelephonyManager.getDeviceId", callback = stringResult("IMEI", { config.imei1Enabled }, { config.imei1 }))
        install("android.os.Binder", "execTransact", "Binder.execTransact", Int::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, callback = ::advertisingBinder)
        install("android.telecom.TelecomManager", "getLine1Number", "TelecomManager.getLine1Number", PhoneAccountHandle::class.java, callback = stringResult("PhoneNumber", { config.mobileNoEnabled }, { config.mobileNo }))
        for (type in listOf("android.content.ContentProviderClient", "android.content.ContentResolver")) {
            for (withCancel in listOf(false, true)) {
                val parameters = mutableListOf(Uri::class.java, Array<String>::class.java, String::class.java, Array<String>::class.java, String::class.java)
                if (withCancel) parameters += CancellationSignal::class.java
                install(type, "query", "$type.query withCancel=$withCancel", *parameters.toTypedArray(), callback = { chain ->
                    val original = chain.proceed()
                    interceptGsfId(original, chain.args.toList()) ?: original
                })
            }
        }
        install("android.bluetooth.BluetoothAdapter", "getAddress", "BluetoothAdapter.getAddress", callback = stringResult("BluetoothMAC", { config.bluetoothMacEnabled }, { config.bluetoothMac }))
        attempt("MediaDrm.getPropertyByteArray") {
            bridge.hook(classLoader.loadClass("android.media.MediaDrm").getMethod("getPropertyByteArray", String::class.java)).intercept { chain ->
                val original = chain.proceed()
                val bytes = original as? ByteArray
                val value = config.mediaDrm
                // The recovered payload does not consult mediaDrmEnabled here.
                if (bytes != null && bytes.isNotEmpty() && chain.args[0] == "deviceUniqueId" && !value.isNullOrEmpty()) {
                    val replacement = generateMediaDrmByteArray(value, bytes.size)
                    if (!bytes.contentEquals(replacement)) {
                        fun hex(data: ByteArray) = data.joinToString("") { "%02x".format(it) }.take(32)
                        logger.log("MediaDRM", hex(bytes), hex(replacement))
                        replacement
                    } else original
                } else original
            }
        }
        install("com.google.android.gms.ads.identifier.AdvertisingIdClient\$Info", "getId", "AdvertisingIdClient.Info.getId", callback = stringResult("AdsID-Client", { config.adsIdEnabled }, { config.adsId }))
        install("android.os.Binder", "execTransactInternal", "Binder.execTransactInternal (AppSetId)", Int::class.javaPrimitiveType!!, Parcel::class.java, Parcel::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, callback = { chain ->
            if (config.appSetIdEnabled && !config.appSetId.isNullOrEmpty()) {
                val binder = chain.thisObject as? Binder
                if (binder != null) runCatching {
                    if (binder.interfaceDescriptor == APP_SET_ID_DESCRIPTOR) {
                        val parcel = chain.args[1] as? Parcel
                        if (parcel != null && parcel.dataSize() == 260) interceptAppSetId(parcel, config.appSetId!!)
                    }
                }
            }
            chain.proceed()
        })
    }

    private fun attempt(label: String, install: () -> Unit) {
        runCatching(install).onFailure { logFailure("Hook failed [$label]", it) }
    }

    private fun replace(original: Any?, label: String, enabled: Boolean, value: String?): Any? {
        if (enabled && !value.isNullOrEmpty() && (original as? String) != value) {
            logger.log(label, original as? String, value)
            return value
        }
        return original
    }

    private fun stringResult(label: String, enabled: () -> Boolean, value: () -> String?) = IHookBridge.Interceptor { chain ->
        replace(chain.proceed(), label, enabled(), value())
    }

    private fun secure(forUser: Boolean) = IHookBridge.Interceptor { chain ->
        val original = chain.proceed()
        val key = chain.args[1]
        when {
            key == "android_id" -> replace(original, "AndroidID", config.androidIdEnabled, config.androidId)
            !forUser && (key as? String).equals("bluetooth_address", ignoreCase = true) ->
                replace(original, "BluetoothSecure", config.bluetoothMacEnabled, config.bluetoothMac)
            else -> original
        }
    }

    private fun advertisingBinder(chain: IHookBridge.Chain): Any? {
        val original = chain.proceed()
        if (!config.adsIdEnabled || config.adsId.isNullOrEmpty()) return original
        val binder = chain.thisObject as? Binder ?: return original
        try {
            if (binder.interfaceDescriptor != "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService" || chain.args[0] as Int != 1) return original
            val pointer = chain.args[2] as Long
            val obtain = Parcel::class.java.getDeclaredMethod("obtain", Long::class.javaPrimitiveType).apply { isAccessible = true }
            val parcel = obtain.invoke(null, pointer) as Parcel
            var previous = "<unknown>"
            runCatching {
                parcel.setDataPosition(0)
                parcel.readException()
                previous = parcel.readString() ?: "<null>"
            }
            if (previous == config.adsId) return original
            parcel.setDataPosition(0)
            parcel.writeNoException()
            parcel.writeString(config.adsId)
            logger.log("AdsID-Binder", previous, config.adsId)
        } catch (error: Exception) {
            bridge.log(5, simpleName, "AdsID-Binder interception failed: ${error.message}", error)
        }
        return original
    }

    internal fun interceptGsfId(result: Any?, args: List<Any?>): Any? {
        if (!config.gsfIdEnabled || config.gsfId.isNullOrEmpty() || args.size <= 1 || args[0] !is Uri || result == null) return null
        val uri = args[0].toString().lowercase(Locale.getDefault())
        val cursor = result as Cursor
        val requested = uri.startsWith("content://com.google.android.gsf.gservices") && args.size > 3 && args[3] != null && (args[3] as Array<*>).contains("android_id")
        val key = cursor.getColumnIndex("key")
        val value = cursor.getColumnIndex("value")
        if (!requested || key != 0 || value != 1 || cursor.columnCount != 2) return null
        val replacement = MatrixCursor(cursor.columnNames)
        while (cursor.moveToNext()) {
            val rowKey = cursor.getString(key)
            val old = cursor.getString(value)
            if (rowKey != "android_id" || old == null) copyColumns(cursor, replacement) else {
                val newId = config.gsfId!!.lowercase(Locale.ROOT).toLong(16)
                if (old.toLongOrNull() == newId) copyColumns(cursor, replacement) else {
                    replacement.addRow(arrayOf<Any>("android_id", newId))
                    logger.log("GSF-ID", old, newId.toString())
                }
            }
        }
        replacement.respond(cursor.extras)
        cursor.close()
        return replacement
    }

    internal fun interceptAppSetId(parcel: Parcel, newId: String) {
        val oldPosition = parcel.dataPosition()
        try {
            val data = parcel.marshall()
            val oldBytes = data.copyOfRange(176, 248)
            val old = oldBytes.toString(Charsets.UTF_16LE)
            if (old != newId && newId.length == old.length) {
                val bytes = newId.toByteArray(Charsets.UTF_16LE)
                if (bytes.size != oldBytes.size) return
                val modified = data.clone()
                bytes.copyInto(modified, 176)
                parcel.unmarshall(modified, 0, modified.size)
                parcel.setDataPosition(0)
                logger.log("AppSetID", old, newId)
            }
        } catch (error: Exception) {
            parcel.setDataPosition(oldPosition)
            Log.e(simpleName, "AppSetId interception failed: ${error.message}")
        }
    }

    private fun copyColumns(cursor: Cursor, target: MatrixCursor) {
        runCatching {
            target.addRow(Array<Any?>(cursor.columnCount) { index ->
                when (cursor.getType(index)) {
                    Cursor.FIELD_TYPE_INTEGER -> cursor.getInt(index)
                    Cursor.FIELD_TYPE_FLOAT -> cursor.getFloat(index)
                    Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                    Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(index)
                    else -> null
                }
            })
        }
    }

    internal fun generateMediaDrmByteArray(value: String, length: Int): ByteArray {
        val bytes = value.toByteArray(Charsets.UTF_8)
        return ByteArray(length) { index -> if (index < bytes.size) bytes[index] else (Math.random() * 256).toInt().toByte() }
    }

    private companion object { const val APP_SET_ID_DESCRIPTOR = "com.google.android.gms.appset.internal.IAppSetIdCallback" }
}
