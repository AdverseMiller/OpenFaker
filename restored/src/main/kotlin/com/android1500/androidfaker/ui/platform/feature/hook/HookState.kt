package com.android1500.androidfaker.ui.platform.feature.hook

import android.os.Parcelable
import com.androidfaker.ui.util.Text
import kotlinx.parcelize.Parcelize

/** Defaults follow HookViewModel's initial state. Account/VIP state is deliberately absent. */
data class HookState(
    val hookData: List<HookData> = emptyList(),
    val manufacturers: List<String> = emptyList(),
    val models: List<Model> = emptyList(),
    val osVersions: List<String> = emptyList(),
    val selectedManufacture: String = "",
    val selectedModel: Model? = null,
    val selectedOsVersion: String = "",
    val isFingerprintSpoofEnabled: Boolean = false,
    val simCountries: List<String> = emptyList(),
    val simOperators: List<String> = emptyList(),
    val simMncCodes: List<String> = emptyList(),
    val selectedSimCountry: String = "",
    val selectedSimOperator: String = "",
    val selectedSimMnc: String = "",
    val isSimCountryLocked: Boolean = false,
    val exportDialogVisible: Boolean = false,
    val importDialogVisible: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportResultMessage: String? = null,
    val importResultMessage: String? = null,
    val dialogState: DialogState? = null,
    // Supplied by TelephonyManager at the lifecycle boundary; zero is a valid device value.
    val simSlotCount: Int,
    val isInModuleScope: Boolean = false,
    val scopeErrorMessage: String? = null,
    val scopeSuccessMessage: String? = null,
) {
    sealed interface DialogState : Parcelable {
        @Parcelize
        data class Error(val title: Text?, val message: Text, val throwable: Throwable?) : DialogState
    }
}
