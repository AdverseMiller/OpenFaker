package com.android1500.androidfaker.data.platform.repository

import com.android1500.androidfaker.data.platform.datasource.disk.database.DeviceDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CarrierRepository {
    val countries: StateFlow<List<String>>
    val operators: StateFlow<List<String>>
    fun loadCountries()
    fun loadOperators(country: String)
    fun getCountries(): List<String>
    fun getOperators(country: String): List<String>
    fun getMccMncCodes(operator: String): List<String>
    fun getCountryIso(country: String): String?
}

class CarrierRepositoryImpl(private val database: DeviceDatabase) : CarrierRepository {
    private val mutableCountries = MutableStateFlow<List<String>>(emptyList())
    private val mutableOperators = MutableStateFlow<List<String>>(emptyList())
    override val countries = mutableCountries.asStateFlow()
    override val operators = mutableOperators.asStateFlow()
    override fun loadCountries() { mutableCountries.value = database.getCountries() }
    override fun loadOperators(country: String) { mutableOperators.value = database.getOperators(country) }
    override fun getCountries() = mutableCountries.value.ifEmpty { database.getCountries() }
    override fun getOperators(country: String) = database.getOperators(country)
    override fun getMccMncCodes(operator: String) = database.getMccMncCodes(operator)
    override fun getCountryIso(country: String) = database.getCountryIso(country)
}
