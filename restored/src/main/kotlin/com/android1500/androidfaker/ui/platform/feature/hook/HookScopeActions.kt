package com.android1500.androidfaker.ui.platform.feature.hook

import com.android1500.androidfaker.data.platform.manager.LsposedServiceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Editor scope callbacks reconstructed separately from framework transport. */
internal class HookScopeActions(
    private val packageName: String,
    private val state: StateFlow<HookState>,
    private val update: ((HookState) -> HookState) -> Unit,
    private val scope: CoroutineScope,
    private val getScope: suspend () -> List<String>?,
    private val removeScope: suspend (String) -> String?,
    private val addScope: suspend (String, (List<String>) -> Unit, (String) -> Unit) -> Unit,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    constructor(packageName: String, manager: LsposedServiceManager, state: StateFlow<HookState>,
        update: ((HookState) -> HookState) -> Unit, scope: CoroutineScope) :
        this(packageName, state, update, scope, manager::getScope, manager::removeScope, manager::addScope)

    fun checkScope() = scope.launch(ioDispatcher) {
        val packages = getScope()
        update { it.copy(isInModuleScope = packages?.contains(packageName) == true) }
    }

    fun toggleScope() = scope.launch(ioDispatcher) {
        clearError()
        if (state.value.isInModuleScope) {
            val error = removeScope(packageName)
            update { it.copy(isInModuleScope = error != null, scopeErrorMessage = error,
                scopeSuccessMessage = if (error == null) "$packageName removed from scope." else null) }
        } else {
            addScope(packageName, {
                // The recovered callback accepts approval as success without testing the returned list.
                update { it.copy(isInModuleScope = true, scopeSuccessMessage = "$packageName are added in scope") }
            }, { message ->
                update { it.copy(isInModuleScope = false, scopeErrorMessage = message) }
            })
        }
    }

    fun clearError() = update { it.copy(scopeErrorMessage = null) }
    fun clearSuccess() = update { it.copy(scopeSuccessMessage = null) }
}
