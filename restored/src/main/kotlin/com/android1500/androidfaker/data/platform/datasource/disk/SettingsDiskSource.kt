package com.android1500.androidfaker.data.platform.datasource.disk

import com.tencent.mmkv.MMKV
import java.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription

/** Enum values recovered from rk; original qualified name is not retained. */
enum class AppTheme(val osValue: Int, val value: String) {
    DEFAULT(-1, "default"), DARK(2, "dark"), LIGHT(1, "light")
}

interface SettingsDiskSource {
    var appTheme: AppTheme
    var seedColor: Int
    var paletteStyleIndex: Int
    var isDynamicColorsEnabled: Boolean?
    var isSystemAppShowEnabled: Boolean?
    var exportFolder: String?
    val termAndCondition: Boolean?
    val appThemeFlow: SharedFlow<AppTheme>
    val seedColorFlow: SharedFlow<Int>
    val paletteStyleIndexFlow: SharedFlow<Int>
    val isDynamicColorsEnabledFlow: SharedFlow<Boolean?>
    val isSystemAppShowEnabledFlow: SharedFlow<Boolean?>
    val exportFolderFlow: SharedFlow<String?>
    fun acceptTerms()
    fun getVaultLastSyncTime(userId: String): Instant?
    fun setVaultLastSyncTime(userId: String, instant: Instant?)
}

internal interface SettingsPreferenceStore {
    fun string(key: String): String?
    fun boolean(key: String): Boolean?
    fun int(key: String): Int?
    fun long(key: String): Long?
    fun put(key: String, value: Any?)
}

class SettingsDiskSourceImpl internal constructor(private val store: SettingsPreferenceStore) : SettingsDiskSource {
    constructor(mmkv: MMKV) : this(object : SettingsPreferenceStore {
        override fun string(key: String) = mmkv.decodeString(key, null)
        override fun boolean(key: String) = if (mmkv.containsKey(key)) mmkv.decodeBool(key) else null
        override fun int(key: String) = if (mmkv.containsKey(key)) mmkv.decodeInt(key) else null
        override fun long(key: String) = if (mmkv.containsKey(key)) mmkv.decodeLong(key) else null
        override fun put(key: String, value: Any?) {
            when (value) {
                null -> mmkv.removeValueForKey(key)
                is String -> mmkv.encode(key, value)
                is Boolean -> mmkv.encode(key, value)
                is Int -> mmkv.encode(key, value)
                is Long -> mmkv.encode(key, value)
                else -> error("Unsupported preference type")
            }
        }
    })

    private fun key(name: String) = "afPreferencesStorage:$name"
    private fun <T> changes(replay: Int) = MutableSharedFlow<T>(replay, Int.MAX_VALUE)
    private val themeChanges = changes<AppTheme>(1)
    private val seedChanges = changes<Int>(1)
    private val paletteChanges = changes<Int>(1)
    private val dynamicChanges = changes<Boolean?>(0)
    private val systemChanges = changes<Boolean?>(0)
    private val folderChanges = changes<String?>(0)
    private val syncChanges = linkedMapOf<String, MutableSharedFlow<Instant?>>()

    override var appTheme: AppTheme
        get() = AppTheme.entries.firstOrNull { it.value == store.string(key("theme")) } ?: AppTheme.DEFAULT
        set(value) { store.put(key("theme"), value.value); themeChanges.tryEmit(appTheme) }
    override var seedColor: Int
        get() = store.int(key("seedColor")) ?: -13866427
        set(value) { store.put(key("seedColor"), value); seedChanges.tryEmit(value) }
    override var paletteStyleIndex: Int
        get() = store.int(key("paletteStyleIndex")) ?: 0
        set(value) { store.put(key("paletteStyleIndex"), value); paletteChanges.tryEmit(value) }
    override var isDynamicColorsEnabled: Boolean?
        get() = store.boolean(key("isDynamicColorsEnabled"))
        set(value) {
            // BaseMMKVDiskSource unboxes nullable booleans; null writes fail in the APK.
            store.put(key("isDynamicColorsEnabled"), value!!)
            dynamicChanges.tryEmit(value)
        }
    override var isSystemAppShowEnabled: Boolean?
        get() = store.boolean(key("isSystemAppShowEnabled"))
        set(value) { store.put(key("isSystemAppShowEnabled"), value!!); systemChanges.tryEmit(value) }
    override var exportFolder: String?
        get() = store.string(key("exportFolder"))
        set(value) { store.put(key("exportFolder"), value); folderChanges.tryEmit(value) }
    override val termAndCondition get() = store.boolean(key("termAndCondition"))
    override fun acceptTerms() = store.put(key("termAndCondition"), true)

    override val appThemeFlow get() = themeChanges.onSubscription { emit(appTheme) }
    override val seedColorFlow get() = seedChanges.onSubscription { emit(seedColor) }
    override val paletteStyleIndexFlow get() = paletteChanges.onSubscription { emit(paletteStyleIndex) }
    override val isDynamicColorsEnabledFlow get() = dynamicChanges.onSubscription { emit(isDynamicColorsEnabled) }
    override val isSystemAppShowEnabledFlow get() = systemChanges.onSubscription { emit(isSystemAppShowEnabled) }
    override val exportFolderFlow get() = folderChanges.onSubscription { emit(exportFolder) }

    override fun getVaultLastSyncTime(userId: String): Instant? =
        store.long(key("vaultLastSyncTime_$userId"))?.let(Instant::ofEpochMilli)

    override fun setVaultLastSyncTime(userId: String, instant: Instant?) {
        store.put(key("vaultLastSyncTime_$userId"), instant?.toEpochMilli())
        syncChanges.getOrPut(userId) { changes(1) }.tryEmit(instant)
    }
}
