package com.android1500.androidfaker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.android1500.androidfaker.ui.platform.feature.hook.HookViewModel

/** Keeps the active editor across activity recreation, but clears its jobs when the destination closes. */
internal class EditorStore : ViewModel() {
    private val store = ViewModelStore()
    private var activePackage: String? = null

    fun get(app: App, packageName: String): HookViewModel {
        if (activePackage != packageName) {
            close()
            activePackage = packageName
        }
        return ViewModelProvider(store, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == HookViewModel::class.java)
                return modelClass.cast(HookViewModel(app.hookRepository, app.deviceRepository, app.carrierRepository,
                    packageName, app, app.lsposedServiceManager))!!
            }
        })[HookViewModel::class.java]
    }

    fun close() { store.clear(); activePackage = null }
    override fun onCleared() = close()
}
