package com.android1500.androidfaker.ui.platform.feature.hook

import com.android1500.androidfaker.data.platform.repository.CarrierRepository
import com.android1500.androidfaker.data.platform.repository.DeviceRepository
import com.android1500.androidfaker.data.platform.repository.HookRepository
import com.androidfaker.data.repository.util.HookConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Recovered observation, initialization and explicit catalog-selection handlers from HookViewModel. */
internal class HookSelectionController(
    private val packageName: String,
    private val hooks: HookRepository,
    private val devices: DeviceRepository,
    private val carriers: CarrierRepository,
    private val scope: CoroutineScope,
    simSlotCount: Int,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val mutableState = MutableStateFlow(HookState(simSlotCount = simSlotCount))
    val state = mutableState.asStateFlow()

    init {
        hooks.configs.onEach { configs -> update { it.withObservedConfig(configs[packageName]) } }.launchIn(scope)
        devices.manufacturers.onEach { values -> update { it.copy(manufacturers = values.map { row -> row.manufacture }) } }.launchIn(scope)
        devices.models.onEach { values -> update { it.copy(models = values) } }.launchIn(scope)
        devices.osVersions.onEach { values -> update { it.copy(osVersions = values.map { row -> row.osv }) } }.launchIn(scope)
        carriers.countries.onEach { values -> update { it.copy(simCountries = values) } }.launchIn(scope)
        carriers.operators.onEach { values -> update { it.copy(simOperators = values) } }.launchIn(scope)
        scope.launch(ioDispatcher) {
            devices.loadManufacturers()
            carriers.loadCountries()
            withContext(mainDispatcher) { restoreSavedSelections() }
        }
    }

    internal fun update(transform: (HookState) -> HookState) = mutableState.update(transform)
    internal fun currentConfig(): HookConfig = hooks.get(packageName) ?: HookConfig(pkgName = packageName)

    fun editValue(id: String, value: String) { hooks.put(currentConfig().withHookValue(id, value)) }
    fun clearConfig() { hooks.remove(packageName) }
    fun dismissError() { update { it.copy(dialogState = null) } }

    fun toggleField(id: String, enabled: Boolean): Job? {
        val initialization = if (enabled && id == "sim_country") {
            scope.launch(ioDispatcher) { initializeSimSelection() }
        } else null
        hooks.put(currentConfig().withHookEnabled(id, enabled))
        return initialization
    }

    fun toggleFingerprint(enabled: Boolean): Job = scope.launch(ioDispatcher) {
        if (enabled && state.value.selectedModel == null) {
            initializeDeviceSelection()
        } else {
            hooks.put(currentConfig().copy(fingerprintEnabled = enabled))
        }
        // The original updates the visible toggle even if an empty catalog prevented initialization.
        withContext(mainDispatcher) { update { it.copy(isFingerprintSpoofEnabled = enabled) } }
    }

    /** Original default selection uses the first catalog rows, even when a saved config exists. */
    internal suspend fun initializeDeviceSelection() {
        val manufacturer = devices.getManufacturers().firstOrNull() ?: return
        val model = devices.getModels(manufacturer.manufacture).firstOrNull() ?: return
        val version = devices.getOsVersions(model.model).firstOrNull()?.osv ?: model.releaseVersion
        hooks.put(currentConfig().withDeviceModel(model, version, true))
        withContext(mainDispatcher) {
            update { it.copy(selectedManufacture = manufacturer.manufacture, selectedModel = model,
                selectedOsVersion = version) }
        }
        devices.loadModels(manufacturer.manufacture)
        devices.loadOsVersions(model.model)
    }

    internal suspend fun initializeSimSelection() {
        val country = carriers.getCountries().firstOrNull() ?: return
        val operator = carriers.getOperators(country).firstOrNull() ?: return
        val code = carriers.getMccMncCodes(operator).firstOrNull() ?: return
        val iso = carriers.getCountryIso(country) ?: ""
        hooks.put(currentConfig().copy(simCountryIso = iso, simCountryName = country,
            simOperatorName = operator, simMccMnc = code, simCountryEnabled = true))
        withContext(mainDispatcher) {
            update { it.copy(selectedSimCountry = country, selectedSimOperator = operator,
                selectedSimMnc = code, simMncCodes = carriers.getMccMncCodes(operator)) }
        }
        carriers.loadOperators(country)
    }

    fun selectManufacturer(manufacturer: String): Job {
        update { it.copy(selectedManufacture = manufacturer, models = emptyList(), osVersions = emptyList(),
            selectedModel = null, selectedOsVersion = "") }
        return scope.launch(ioDispatcher) {
            devices.loadModels(manufacturer)
            val model = devices.getModels(manufacturer).firstOrNull() ?: return@launch
            devices.loadOsVersions(model.model)
            val version = devices.getOsVersions(model.model).firstOrNull()?.osv ?: model.releaseVersion
            withContext(mainDispatcher) { update { it.copy(selectedModel = model, selectedOsVersion = version) } }
            hooks.put(currentConfig().withDeviceModel(model, version, true))
        }
    }

    fun selectModel(name: String): Job? {
        val model = state.value.models.firstOrNull { it.model == name } ?: return null
        update { it.copy(selectedModel = model, selectedOsVersion = "") }
        return scope.launch(ioDispatcher) {
            devices.loadOsVersions(name)
            val version = devices.getOsVersions(name).firstOrNull()?.osv ?: model.releaseVersion
            withContext(mainDispatcher) { update { it.copy(selectedOsVersion = version) } }
            hooks.put(currentConfig().withDeviceModel(model, version, true))
        }
    }

    fun selectOsVersion(version: String) {
        update { it.copy(selectedOsVersion = version) }
        applyDeviceSelection()
    }

    fun applyDeviceSelection() {
        val selected = state.value
        val model = selected.selectedModel ?: return
        hooks.put(currentConfig().withDeviceModel(model,
            selected.selectedOsVersion.ifEmpty { model.releaseVersion }, selected.isFingerprintSpoofEnabled))
    }

    fun selectSimCountry(country: String): Job {
        update { it.copy(selectedSimCountry = country, simOperators = emptyList(), simMncCodes = emptyList(),
            selectedSimOperator = "", selectedSimMnc = "") }
        return scope.launch(ioDispatcher) {
            carriers.loadOperators(country)
            val operator = carriers.getOperators(country).firstOrNull() ?: return@launch
            val codes = carriers.getMccMncCodes(operator)
            withContext(mainDispatcher) {
                update { it.copy(selectedSimOperator = operator, simMncCodes = codes, selectedSimMnc = codes.firstOrNull() ?: "") }
            }
        }
    }

    fun selectSimOperator(operator: String): Job {
        update { it.copy(selectedSimOperator = operator, simMncCodes = emptyList(), selectedSimMnc = "") }
        return scope.launch(ioDispatcher) {
            val codes = carriers.getMccMncCodes(operator)
            withContext(mainDispatcher) { update { it.copy(simMncCodes = codes, selectedSimMnc = codes.firstOrNull() ?: "") } }
        }
    }

    fun selectSimMnc(code: String) { update { it.copy(selectedSimMnc = code) } }

    fun setSimCountryLocked(locked: Boolean) {
        hooks.put(currentConfig().copy(simCountryLocked = locked))
        update { it.copy(isSimCountryLocked = locked) }
    }

    fun saveSimInfo(): Job? {
        val selected = state.value
        if (selected.selectedSimCountry.isBlank() || selected.selectedSimOperator.isBlank() || selected.selectedSimMnc.isBlank()) return null
        return scope.launch(ioDispatcher) {
            val config = currentConfig()
            val iso = carriers.getCountryIso(selected.selectedSimCountry) ?: ""
            hooks.put(config.copy(simCountryIso = iso, simCountryName = selected.selectedSimCountry,
                simOperatorName = selected.selectedSimOperator, simMccMnc = selected.selectedSimMnc,
                simCountryEnabled = true, simCountryLocked = state.value.isSimCountryLocked))
        }
    }

    /** The original schedules model loading but checks the current catalog immediately, without awaiting it. */
    internal fun restoreSavedSelections() {
        val config = hooks.get(packageName) ?: return
        config.manufacturer?.let { manufacturer ->
            update { it.copy(selectedManufacture = manufacturer) }
            scope.launch(ioDispatcher) { devices.loadModels(manufacturer) }
            config.model?.let { name ->
                devices.models.value.firstOrNull { it.model == name }?.let { model ->
                    update { it.copy(selectedModel = model, selectedOsVersion = config.releaseVersion ?: model.releaseVersion) }
                    scope.launch(ioDispatcher) { devices.loadOsVersions(name) }
                }
            }
        }
        config.simCountryName?.let { country ->
            update { it.copy(selectedSimCountry = country, selectedSimOperator = config.simOperatorName ?: "",
                selectedSimMnc = config.simMccMnc ?: "", isSimCountryLocked = config.simCountryLocked,
                simMncCodes = config.simOperatorName?.let(carriers::getMccMncCodes) ?: emptyList()) }
            scope.launch(ioDispatcher) { carriers.loadOperators(country) }
        }
    }
}
