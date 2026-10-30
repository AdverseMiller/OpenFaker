package com.android1500.androidfaker.ui.platform.feature.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android1500.androidfaker.data.app.repository.AppRepository
import com.android1500.androidfaker.data.app.repository.SortType
import com.android1500.androidfaker.data.platform.repository.HookRepository
import com.android1500.androidfaker.data.platform.repository.ProfileRepository
import com.android1500.androidfaker.data.platform.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Readable lifecycle wiring for the recovered handlers, without the retired entitlement dependency. */
class AppListViewModel(
    repository: AppRepository,
    settingsRepository: SettingsRepository,
    hookRepository: HookRepository,
    profileRepository: ProfileRepository,
    context: Context,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val controller = AppListStateController(repository,
        settingsRepository.isSystemAppShowEnabledFlow, hookRepository.configs,
        context.applicationContext.packageManager, viewModelScope, backgroundDispatcher)
    private val profiles = AppListProfileActions(repository, profileRepository, controller,
        viewModelScope, backgroundDispatcher)

    val state = controller.state
    fun refresh() = controller.refresh()
    fun setQuery(value: String) = controller.setQuery(value)
    fun clearQuery() = controller.clearQuery()
    fun setSortType(value: SortType) = controller.setSortType(value)
    fun clearAppData(packageName: String) = controller.clearAppData(packageName)
    fun forceStop(packageName: String) = controller.forceStop(packageName)
    fun uninstall(packageName: String) = controller.uninstall(packageName)
    fun toggleProfile(packageName: String) = profiles.toggle(packageName)
    fun confirmDisableProfile(packageName: String) = profiles.confirmDisable(packageName)
    fun dismissDialog() = controller.setDialog(null)
}
