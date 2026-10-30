package com.android1500.androidfaker.ui.platform.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android1500.androidfaker.data.app.repository.LogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModel(private val repository: LogRepository) : ViewModel() {
    data class FilterState(
        val selectedPackage: String? = null,
        val selectedHookName: String? = null,
        val searchQuery: String = "",
    )

    private val mutableFilter = MutableStateFlow(FilterState())
    val filterState = mutableFilter.asStateFlow()
    private val sharing = SharingStarted.WhileSubscribed(5_000)
    val packageNames = repository.observePackageNames().stateIn(viewModelScope, sharing, emptyList())
    val hookNames = repository.observeHookNames().stateIn(viewModelScope, sharing, emptyList())
    val logs = mutableFilter.flatMapLatest { filter ->
        // Original selection gives package precedence rather than intersecting both filters.
        when {
            filter.selectedPackage != null -> repository.observeByPackage(filter.selectedPackage)
            filter.selectedHookName != null -> repository.observeByHook(filter.selectedHookName)
            else -> repository.observeRecent()
        }
    }.map { rows ->
        val query = mutableFilter.value.searchQuery
        if (query.isBlank()) rows else {
            val needle = query.lowercase(Locale.ROOT)
            rows.filter { row ->
                row.hookName.lowercase(Locale.ROOT).contains(needle) ||
                    row.packageName.lowercase(Locale.ROOT).contains(needle) ||
                    row.originalValue.lowercase(Locale.ROOT).contains(needle) ||
                    row.spoofedValue.lowercase(Locale.ROOT).contains(needle)
            }
        }
    }.stateIn(viewModelScope, sharing, emptyList())

    fun selectPackage(value: String?) { mutableFilter.update { it.copy(selectedPackage = value, selectedHookName = null) } }
    fun selectHook(value: String?) { mutableFilter.update { it.copy(selectedPackage = null, selectedHookName = value) } }
    fun updateSearchQuery(query: String) { mutableFilter.update { it.copy(searchQuery = query) } }
    fun resetFilters() { mutableFilter.update { FilterState() } }
    fun clearAllLogs() { viewModelScope.launch { repository.clearAll() } }
}
