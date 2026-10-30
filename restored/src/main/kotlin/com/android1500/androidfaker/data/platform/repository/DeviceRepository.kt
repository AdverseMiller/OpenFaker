package com.android1500.androidfaker.data.platform.repository

import com.android1500.androidfaker.data.platform.datasource.disk.database.DeviceDatabase
import com.android1500.androidfaker.data.platform.datasource.disk.database.Manufacture
import com.android1500.androidfaker.data.platform.datasource.disk.database.OS
import com.android1500.androidfaker.ui.platform.feature.hook.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DeviceRepository {
    val manufacturers: StateFlow<List<Manufacture>>
    val models: StateFlow<List<Model>>
    val osVersions: StateFlow<List<OS>>
    fun loadManufacturers()
    fun loadModels(manufacturer: String)
    fun loadOsVersions(model: String)
    fun getManufacturers(): List<Manufacture>
    fun getModels(manufacturer: String): List<Model>
    fun getOsVersions(model: String): List<OS>
}

/** Synchronous queries and explicit flow refreshes match DeviceRepositoryImpl/pe1. */
class DeviceRepositoryImpl(private val database: DeviceDatabase) : DeviceRepository {
    private val mutableManufacturers = MutableStateFlow<List<Manufacture>>(emptyList())
    private val mutableModels = MutableStateFlow<List<Model>>(emptyList())
    private val mutableOsVersions = MutableStateFlow<List<OS>>(emptyList())
    override val manufacturers = mutableManufacturers.asStateFlow()
    override val models = mutableModels.asStateFlow()
    override val osVersions = mutableOsVersions.asStateFlow()
    override fun loadManufacturers() { mutableManufacturers.value = database.getManufacturers() }
    override fun loadModels(manufacturer: String) { mutableModels.value = database.getModels(manufacturer) }
    override fun loadOsVersions(model: String) { mutableOsVersions.value = database.getOsVersions(model) }
    override fun getManufacturers() = mutableManufacturers.value.ifEmpty { database.getManufacturers() }
    override fun getModels(manufacturer: String) = database.getModels(manufacturer)
    override fun getOsVersions(model: String) = database.getOsVersions(model)
}
