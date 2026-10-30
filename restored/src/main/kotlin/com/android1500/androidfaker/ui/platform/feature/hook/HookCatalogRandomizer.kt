package com.android1500.androidfaker.ui.platform.feature.hook

import com.android1500.androidfaker.data.platform.repository.CarrierRepository
import com.android1500.androidfaker.data.platform.repository.DeviceRepository
import com.android1500.androidfaker.data.platform.repository.HookRepository
import com.androidfaker.data.repository.util.HookConfig
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Recovered randomizeAll/randomizeSim actions, without the original entitlement checks. */
internal class HookCatalogRandomizer(
    private val selections: HookSelectionController,
    private val hooks: HookRepository,
    private val devices: DeviceRepository,
    private val carriers: CarrierRepository,
    private val scope: CoroutineScope,
    private val random: Random = Random.Default,
    private val generator: HookValueGenerator = HookValueGenerator(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private data class SimSelection(val country: String, val operator: String, val code: String, val iso: String)

    private fun randomSim(lockedCountry: String?): SimSelection? {
        val country = lockedCountry ?: carriers.getCountries().randomOrNull(random) ?: return null
        val operator = carriers.getOperators(country).randomOrNull(random) ?: return null
        val code = carriers.getMccMncCodes(operator).randomOrNull(random) ?: return null
        return SimSelection(country, operator, code, carriers.getCountryIso(country) ?: "")
    }

    private fun HookConfig.withSim(sim: SimSelection) = copy(simCountryName = sim.country,
        simOperatorName = sim.operator, simMccMnc = sim.code, simCountryIso = sim.iso)

    fun randomizeSim() = scope.launch(ioDispatcher) {
        val lockedCountry = if (selections.currentConfig().simCountryLocked) selections.currentConfig().simCountryName else null
        val sim = randomSim(lockedCountry) ?: return@launch
        hooks.put(selections.currentConfig().withSim(sim).copy(simCountryEnabled = true))
        withContext(mainDispatcher) {
            selections.update { it.copy(selectedSimCountry = sim.country, selectedSimOperator = sim.operator,
                selectedSimMnc = sim.code, simMncCodes = carriers.getMccMncCodes(sim.operator)) }
        }
        carriers.loadOperators(sim.country)
    }

    fun randomizeAll() = scope.launch(ioDispatcher) {
        var config = randomizeEnabledValues(selections.currentConfig())
        if (config.fingerprintEnabled) {
            val manufacturer = devices.getManufacturers().randomOrNull(random)
            val model = manufacturer?.let { devices.getModels(it.manufacture).randomOrNull(random) }
            if (model != null) {
                val version = devices.getOsVersions(model.model).randomOrNull(random)?.osv ?: model.releaseVersion
                config = config.withDeviceModel(model, version, true)
                withContext(mainDispatcher) {
                    selections.update { it.copy(selectedManufacture = model.manufacture, selectedModel = model,
                        selectedOsVersion = version) }
                }
                devices.loadModels(model.manufacture)
                devices.loadOsVersions(model.model)
            }
        }
        if (config.simCountryEnabled) {
            val sim = randomSim(if (config.simCountryLocked) config.simCountryName else null)
            if (sim != null) {
                config = config.withSim(sim)
                withContext(mainDispatcher) {
                    // Unlike the standalone SIM action, bulk randomization does not refresh the code list.
                    selections.update { it.copy(selectedSimCountry = sim.country, selectedSimOperator = sim.operator,
                        selectedSimMnc = sim.code) }
                }
                carriers.loadOperators(sim.country)
            }
        }
        // The original persists a single captured configuration, after catalog/state updates.
        hooks.put(config)
    }

    internal fun randomizeEnabledValues(config: HookConfig): HookConfig = with(config) {
        copy(
            imei1 = if (imei1Enabled) generator.imei() else imei1,
            imei2 = if (imei2Enabled) generator.imei() else imei2,
            androidId = if (androidIdEnabled) generator.hex(16) else androidId,
            wifiMac = if (wifiMacEnabled) generator.mac() else wifiMac,
            wifiSsid = if (wifiSsidEnabled) "WiFi_${generator.hex(6)}" else wifiSsid,
            wifiBssid = if (wifiBssidEnabled) generator.mac() else wifiBssid,
            bluetoothMac = if (bluetoothMacEnabled) generator.mac() else bluetoothMac,
            simSubscriberId = if (simSubscriberIdEnabled) generator.decimal(15) else simSubscriberId,
            simSerial = if (simSerialEnabled) generator.decimal(20) else simSerial,
            mobileNo = if (mobileNoEnabled) generator.phoneNumber() else mobileNo,
            hwSerial = if (hwSerialEnabled) generator.hardwareSerial() else hwSerial,
            adsId = if (adsIdEnabled) generator.uuidShape() else adsId,
            mediaDrm = if (mediaDrmEnabled) generator.hex(32) else mediaDrm,
            gsfId = if (gsfIdEnabled) generator.gsfId() else gsfId,
            appSetId = if (appSetIdEnabled) generator.uuidShape() else appSetId,
            ipAddress = if (ipAddressEnabled) generator.ipv4() else ipAddress,
        )
    }
}
