package com.android1500.androidfaker.ui.platform.feature.apps

import android.content.pm.PackageManager
import com.android1500.androidfaker.data.app.repository.AppRepository
import com.android1500.androidfaker.data.app.repository.SortType
import com.androidfaker.data.repository.util.HookConfig
import com.androidfaker.ui.util.asText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Recovered observation/search/refresh and app-management portion of AppListViewModel, extracted for
 * reuse by its remaining action handlers. The caller owns the lifecycle scope.
 * Profile actions are not yet implemented by this controller.
 */
internal class AppListStateController(
    private val repository: AppRepository,
    showSystemApps: StateFlow<Boolean>,
    configs: StateFlow<Map<String, HookConfig>>,
    packageManager: PackageManager,
    private val scope: CoroutineScope,
    projectionDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val query = MutableStateFlow("")
    private val mutableState = MutableStateFlow(AppListState(
        emptySet(), repository.profileEnabledApps, emptyList(), "", repository.sortType,
        showSystemApps.value, false, true, null,
    ))
    val state: StateFlow<AppListState> = mutableState.asStateFlow()
    private var initialRefreshJob: Job? = null

    private data class Options(val showSystem: Boolean, val sort: SortType, val query: String)

    init {
        val options = combine(showSystemApps, repository.sortTypeFlow, query, ::Options)
        combine(repository.getApplicationList(), options, configs, repository.profileEnabledAppsFlow) {
                apps, selection, hooks, profiles ->
            projectAppListState(apps, selection.showSystem, selection.sort, selection.query,
                hooks, profiles, mutableState.value.dialogState, packageManager)
        }.flowOn(projectionDispatcher).onEach { mutableState.value = it }.launchIn(scope)
        updateInstalledAppList()
    }

    fun setQuery(value: String) {
        query.value = value
        mutableState.update { it.copy(query = value) }
    }

    fun clearQuery() = setQuery("")

    fun setSortType(value: SortType) {
        repository.sortType = value
        mutableState.update { it.copy(sortType = value) }
    }

    fun setDialog(value: AppListState.DialogState?) {
        mutableState.update { it.copy(dialogState = value) }
    }

    fun clearAppData(packageName: String) = runAppAction("Failed to clear data $packageName") {
        repository.clearAppData(packageName)
    }

    fun forceStop(packageName: String) = runAppAction("Failed to force stop $packageName") {
        repository.forceStop(packageName)
    }

    fun uninstall(packageName: String) = runAppAction("Failed to uninstall $packageName") {
        repository.uninstall(packageName)
    }

    private fun runAppAction(fallbackMessage: String, action: suspend () -> Unit): Job = scope.launch {
        // The recovered handlers use runCatching (including cancellation/errors),
        // retain the actual Throwable, and do not refresh or dismiss dialogs on success.
        runCatching { action() }.onFailure { error ->
            setDialog(AppListState.DialogState.Error("Error".asText(),
                (error.message ?: fallbackMessage).asText(), error))
        }
    }

    fun updateInstalledAppList() {
        initialRefreshJob?.cancel()
        initialRefreshJob = scope.launch {
            repository.updateApplicationList().collect { result ->
                if (result.isFailure) {
                    mutableState.update { it.copy(isLoading = false) }
                    setDialog(AppListState.DialogState.Error("Error".asText(),
                        result.exceptionOrNull()?.message.toString().asText(), null))
                }
                // Successful cache emissions clear loading through the projection.
            }
        }
    }

    fun refresh() {
        mutableState.update { it.copy(isRefreshing = true) }
        repository.updateApplicationList()
            .onEach { mutableState.update { it.copy(isRefreshing = false) } }
            .catch { mutableState.update { it.copy(isRefreshing = false) } }
            .launchIn(scope)
    }
}
