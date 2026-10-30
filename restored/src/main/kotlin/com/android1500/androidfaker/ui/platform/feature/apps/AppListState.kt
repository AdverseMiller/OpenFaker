package com.android1500.androidfaker.ui.platform.feature.apps

import android.os.Parcelable
import com.android1500.androidfaker.data.app.repository.SortType
import com.androidfaker.ui.util.Text
import kotlinx.parcelize.Parcelize

data class AppListState(
    val enabledApps: Set<String>,
    val profileEnabledApps: Set<String>,
    val appList: List<AppItem>,
    val query: String,
    val sortType: SortType,
    val showSystemApps: Boolean,
    val isRefreshing: Boolean,
    val isLoading: Boolean,
    val dialogState: DialogState?,
) {
    sealed interface DialogState : Parcelable {
        @Parcelize
        data class ConfirmDisableProfile(
            val packageName: String,
            val title: Text,
            val message: Text,
        ) : DialogState

        @Parcelize
        data class Error(
            val title: Text?,
            val message: Text,
            val error: Throwable?,
        ) : DialogState
    }
}
