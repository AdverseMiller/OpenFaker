package com.android1500.androidfaker.data.platform.datasource.disk

import com.tencent.mmkv.MMKV
import com.android1500.androidfaker.data.app.repository.SortType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription

interface AppDiskSource {
    var sortType: SortType
    var profileEnabledApps: Set<String>
    val sortTypeFlow: SharedFlow<SortType>
    val profileEnabledAppsFlow: SharedFlow<Set<String>>
}

/** Narrow storage seam permits contract tests without loading Android native MMKV. */
internal interface AppPreferenceStore {
    fun readString(key: String): String?
    fun writeString(key: String, value: String)
    fun readStringSet(key: String): Set<String>?
    fun writeStringSet(key: String, value: Set<String>)
}

class AppDiskSourceImpl internal constructor(private val store: AppPreferenceStore) : AppDiskSource {
    constructor(mmkv: MMKV) : this(object : AppPreferenceStore {
        override fun readString(key: String) = mmkv.decodeString(key, null)
        override fun writeString(key: String, value: String) { mmkv.encode(key, value) }
        override fun readStringSet(key: String) = mmkv.decodeStringSet(key, emptySet())
        override fun writeStringSet(key: String, value: Set<String>) { mmkv.encode(key, value) }
    })

    private val sortChanges = MutableSharedFlow<SortType>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
    private val profileChanges = MutableSharedFlow<Set<String>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    override var sortType: SortType
        get() = runCatching { SortType.valueOf(store.readString(SORT_KEY) ?: "") }
            .getOrDefault(SortType.NAME_ASC)
        set(value) {
            store.writeString(SORT_KEY, value.name)
            sortChanges.tryEmit(value)
        }

    override var profileEnabledApps: Set<String>
        get() = store.readStringSet(PROFILE_KEY).orEmpty().toSet()
        set(value) {
            val snapshot = value.toSet()
            store.writeStringSet(PROFILE_KEY, snapshot)
            profileChanges.tryEmit(snapshot)
        }

    // This deliberately preserves onSubscription + replay, rather than replacing
    // it with StateFlow: a new subscriber can receive the same value twice.
    override val sortTypeFlow: SharedFlow<SortType>
        get() = sortChanges.onSubscription { emit(sortType) }
    override val profileEnabledAppsFlow: SharedFlow<Set<String>>
        get() = profileChanges.onSubscription { emit(profileEnabledApps) }

    internal companion object {
        const val SORT_KEY = "afPreferencesStorage:app_sort_type"
        const val PROFILE_KEY = "afPreferencesStorage:profile_enabled_apps"
    }
}
