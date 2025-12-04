package com.moshitech.workmate.feature.unitconverter.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class CurrencyRateRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("currency_rates", Context.MODE_PRIVATE)
    
    private val _currencyRates = MutableStateFlow<Map<String, Double>>(getDefaultRates())
    val currencyRates: StateFlow<Map<String, Double>> = _currencyRates.asStateFlow()
    
    init {
        loadRates()
    }
    
    private fun getDefaultRates(): Map<String, Double> {
        // Default rates relative to USD (1 USD = X currency)
        return mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "JPY" to 149.0,
            "CNY" to 7.24,
            "INR" to 83.12,
            "AUD" to 1.52,
            "CAD" to 1.36,
            "CHF" to 0.88,
            "HKD" to 7.83,
            "SGD" to 1.34,
            "SEK" to 10.35,
            "KRW" to 1305.0,
            "NOK" to 10.87,
            "MXN" to 17.15,
            "BRL" to 4.97,
            "ZAR" to 18.25,
            "RUB" to 92.5,
            "AED" to 3.67,
            "SAR" to 3.75
        )
    }
    
    private fun loadRates() {
        val ratesJson = prefs.getString("rates", null)
        if (ratesJson != null) {
            try {
                val jsonObject = JSONObject(ratesJson)
                val rates = mutableMapOf<String, Double>()
                jsonObject.keys().forEach { key ->
                    rates[key] = jsonObject.getDouble(key)
                }
                _currencyRates.value = rates
            } catch (e: Exception) {
                // If parsing fails, use default rates
                _currencyRates.value = getDefaultRates()
                saveRates(_currencyRates.value)
            }
        } else {
            // First time - save default rates
            _currencyRates.value = getDefaultRates()
            saveRates(_currencyRates.value)
        }
    }
    
    fun saveRates(rates: Map<String, Double>) {
        val jsonObject = JSONObject()
        rates.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        prefs.edit().putString("rates", jsonObject.toString()).apply()
        _currencyRates.value = rates
    }
    
    fun updateRate(currencyCode: String, rate: Double) {
        val updatedRates = _currencyRates.value.toMutableMap()
        updatedRates[currencyCode] = rate
        saveRates(updatedRates)
    }

    fun addCurrency(currencyCode: String, rate: Double) {
        val updatedRates = _currencyRates.value.toMutableMap()
        updatedRates[currencyCode] = rate
        saveRates(updatedRates)
    }

    fun removeCurrency(currencyCode: String) {
        val updatedRates = _currencyRates.value.toMutableMap()
        if (updatedRates.containsKey(currencyCode)) {
            updatedRates.remove(currencyCode)
            saveRates(updatedRates)
        }
    }
    
    fun getRate(currencyCode: String): Double {
        return _currencyRates.value[currencyCode] ?: 1.0
    }
    
    fun resetToDefaults() {
        saveRates(getDefaultRates())
    }
    
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        
        val fromRate = getRate(fromCurrency)
        val toRate = getRate(toCurrency)
        
        // Convert: amount in FROM currency -> USD -> TO currency
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }
}
