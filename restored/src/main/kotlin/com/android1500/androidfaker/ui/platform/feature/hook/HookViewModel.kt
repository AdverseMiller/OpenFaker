package com.android1500.androidfaker.ui.platform.feature.hook

import android.content.Context
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android1500.androidfaker.data.platform.manager.LsposedServiceManager
import com.android1500.androidfaker.data.platform.repository.CarrierRepository
import com.android1500.androidfaker.data.platform.repository.DeviceRepository
import com.android1500.androidfaker.data.platform.repository.HookRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Lifecycle owner for the recovered editor handlers. Account/entitlement state is intentionally absent. */
class HookViewModel(
    hookRepository: HookRepository,
    deviceRepository: DeviceRepository,
    carrierRepository: CarrierRepository,
    val packageName: String,
    simSlotCount: Int,
    lsposedServiceManager: LsposedServiceManager,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    constructor(hookRepository: HookRepository, deviceRepository: DeviceRepository,
        carrierRepository: CarrierRepository, packageName: String, context: Context,
        lsposedServiceManager: LsposedServiceManager) : this(hookRepository, deviceRepository,
        carrierRepository, packageName, readSimSlotCount(context), lsposedServiceManager)

    private val selections = HookSelectionController(packageName, hookRepository, deviceRepository,
        carrierRepository, viewModelScope, simSlotCount, ioDispatcher, mainDispatcher)
    private val documents = HookDocumentActions(packageName, hookRepository, deviceRepository,
        carrierRepository, selections::update, selections::restoreSavedSelections, viewModelScope,
        ioDispatcher, mainDispatcher)
    private val scopes = HookScopeActions(packageName, selections.state, selections::update,
        viewModelScope, lsposedServiceManager::getScope, lsposedServiceManager::removeScope,
        lsposedServiceManager::addScope, ioDispatcher)
    private val fields = HookFieldRandomizer(packageName, hookRepository, { selections.state.value.simSlotCount })
    private val catalogs = HookCatalogRandomizer(selections, hookRepository, deviceRepository,
        carrierRepository, viewModelScope, ioDispatcher = ioDispatcher, mainDispatcher = mainDispatcher)

    val state = selections.state

    init { scopes.checkScope() }

    fun toggleField(id: String, enabled: Boolean) = selections.toggleField(id, enabled)
    fun editValue(id: String, value: String) = selections.editValue(id, value)
    fun randomizeField(id: String) = fields.randomize(id)
    fun randomizeAll() = catalogs.randomizeAll()
    fun toggleFingerprint(enabled: Boolean) = selections.toggleFingerprint(enabled)
    fun selectManufacturer(value: String) = selections.selectManufacturer(value)
    fun selectModel(value: String) = selections.selectModel(value)
    fun selectOsVersion(value: String) = selections.selectOsVersion(value)
    fun applyDeviceSelection() = selections.applyDeviceSelection()
    fun selectSimCountry(value: String) = selections.selectSimCountry(value)
    fun selectSimOperator(value: String) = selections.selectSimOperator(value)
    fun selectSimMnc(value: String) = selections.selectSimMnc(value)
    fun setSimCountryLocked(locked: Boolean) = selections.setSimCountryLocked(locked)
    fun saveSimInfo() = selections.saveSimInfo()
    fun randomizeSim() = catalogs.randomizeSim()
    fun clearConfig() = selections.clearConfig()
    fun restoreSavedSelections() = selections.restoreSavedSelections()
    fun dismissError() = selections.dismissError()
    fun showExportDialog() = documents.showExportDialog()
    fun hideExportDialog() = documents.hideExportDialog()
    fun showImportDialog() = documents.showImportDialog()
    fun hideImportDialog() = documents.hideImportDialog()
    fun exportToFile(fileName: String) = documents.exportToFile(fileName)
    fun importFromFile(uri: Uri) = documents.importFromFile(uri)
    fun clearExportResult() = documents.clearExportResult()
    fun clearImportResult() = documents.clearImportResult()
    fun checkScope() = scopes.checkScope()
    fun toggleScope() = scopes.toggleScope()
    fun clearScopeError() = scopes.clearError()
    fun clearScopeSuccess() = scopes.clearSuccess()

    companion object {
        @Suppress("DEPRECATION")
        internal fun readSimSlotCount(context: Context): Int {
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return if (Build.VERSION.SDK_INT >= 30) manager.activeModemCount else manager.phoneCount
        }
    }
}
