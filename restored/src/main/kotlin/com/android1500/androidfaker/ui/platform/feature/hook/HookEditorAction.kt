package com.android1500.androidfaker.ui.platform.feature.hook

import android.net.Uri

/** Descriptive replacements for the recovered editor's obfuscated action variants. */
internal sealed interface HookEditorAction {
    data class ToggleField(val id: String, val enabled: Boolean) : HookEditorAction
    data class EditField(val id: String, val value: String) : HookEditorAction
    data class RandomField(val id: String) : HookEditorAction
    data object RandomizeAll : HookEditorAction
    data object ClearConfig : HookEditorAction
    data class ToggleFingerprint(val enabled: Boolean) : HookEditorAction
    data class SelectManufacturer(val value: String) : HookEditorAction
    data class SelectModel(val value: String) : HookEditorAction
    data class SelectOs(val value: String) : HookEditorAction
    data class SelectSimCountry(val value: String) : HookEditorAction
    data class SelectSimOperator(val value: String) : HookEditorAction
    data class SelectSimCode(val value: String) : HookEditorAction
    data class LockSimCountry(val locked: Boolean) : HookEditorAction
    data object SaveSim : HookEditorAction
    data object RandomSim : HookEditorAction
    data object ShowImport : HookEditorAction
    data object HideImport : HookEditorAction
    data object ShowExport : HookEditorAction
    data object HideExport : HookEditorAction
    data class ImportFile(val uri: Uri) : HookEditorAction
    data class ExportFile(val name: String) : HookEditorAction
    data object ClearImportResult : HookEditorAction
    data object ClearExportResult : HookEditorAction
    data object ToggleScope : HookEditorAction
    data object ClearScopeError : HookEditorAction
    data object ClearScopeSuccess : HookEditorAction
    data object DismissError : HookEditorAction
}

internal fun HookViewModel.handle(action: HookEditorAction) {
    when (action) {
        is HookEditorAction.ToggleField -> toggleField(action.id, action.enabled)
        is HookEditorAction.EditField -> editValue(action.id, action.value)
        is HookEditorAction.RandomField -> randomizeField(action.id)
        HookEditorAction.RandomizeAll -> randomizeAll()
        HookEditorAction.ClearConfig -> clearConfig()
        is HookEditorAction.ToggleFingerprint -> toggleFingerprint(action.enabled)
        is HookEditorAction.SelectManufacturer -> selectManufacturer(action.value)
        is HookEditorAction.SelectModel -> selectModel(action.value)
        is HookEditorAction.SelectOs -> selectOsVersion(action.value)
        is HookEditorAction.SelectSimCountry -> selectSimCountry(action.value)
        is HookEditorAction.SelectSimOperator -> selectSimOperator(action.value)
        is HookEditorAction.SelectSimCode -> selectSimMnc(action.value)
        is HookEditorAction.LockSimCountry -> setSimCountryLocked(action.locked)
        HookEditorAction.SaveSim -> saveSimInfo()
        HookEditorAction.RandomSim -> randomizeSim()
        HookEditorAction.ShowImport -> showImportDialog()
        HookEditorAction.HideImport -> hideImportDialog()
        HookEditorAction.ShowExport -> showExportDialog()
        HookEditorAction.HideExport -> hideExportDialog()
        is HookEditorAction.ImportFile -> importFromFile(action.uri)
        is HookEditorAction.ExportFile -> exportToFile(action.name)
        HookEditorAction.ClearImportResult -> clearImportResult()
        HookEditorAction.ClearExportResult -> clearExportResult()
        HookEditorAction.ToggleScope -> toggleScope()
        HookEditorAction.ClearScopeError -> clearScopeError()
        HookEditorAction.ClearScopeSuccess -> clearScopeSuccess()
        HookEditorAction.DismissError -> dismissError()
    }
}
