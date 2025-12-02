package com.moshitech.workmate.feature.unitconverter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.unitconverter.data.local.ConversionFavoriteEntity
import com.moshitech.workmate.feature.unitconverter.repository.UnitConverterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnitConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UnitConverterRepository by lazy { UnitConverterRepository(application) }
    private val preferencesRepository: com.moshitech.workmate.repository.UserPreferencesRepository by lazy { 
        com.moshitech.workmate.repository.UserPreferencesRepository(application) 
    }
    
    val viewMode: StateFlow<com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode> = 
        preferencesRepository.viewMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GRID
        )
    
    val hapticFeedbackEnabled: StateFlow<Boolean> = 
        preferencesRepository.hapticFeedbackEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    fun toggleViewMode() {
        viewModelScope.launch {
            val newMode = if (viewMode.value == com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GRID) {
                com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.LIST
            } else {
                com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GRID
            }
            preferencesRepository.setViewMode(newMode)
        }
    }
    
    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedback(enabled)
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val internalCategories = setOf(
        UnitCategory.TIME_DATE_CALC,
        UnitCategory.TIME_DIFFERENCE,
        UnitCategory.TIME_TIMESTAMP,
        UnitCategory.TIME_ZONES,
        UnitCategory.TIME_BIZ_DAYS,
        UnitCategory.TIME_AGE
    )

    private val _categories = MutableStateFlow(UnitCategory.values().filter { !internalCategories.contains(it) })
    val categories: StateFlow<List<UnitCategory>> = _categories.asStateFlow()

    val favorites: StateFlow<List<ConversionFavoriteEntity>> = repository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<com.moshitech.workmate.feature.unitconverter.data.local.UnitConversionHistoryEntity>> = repository.getHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Conversion Details State
    private val _selectedCategory = MutableStateFlow(UnitCategory.LENGTH)
    val selectedCategory: StateFlow<UnitCategory> = _selectedCategory.asStateFlow()

    private val _inputValue = MutableStateFlow("1")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _sourceUnit = MutableStateFlow<ConversionUnit?>(null)
    val sourceUnit: StateFlow<ConversionUnit?> = _sourceUnit.asStateFlow()

    private val _targetUnit = MutableStateFlow<ConversionUnit?>(null)
    val targetUnit: StateFlow<ConversionUnit?> = _targetUnit.asStateFlow()

    private val _resultValue = MutableStateFlow("")
    val resultValue: StateFlow<String> = _resultValue.asStateFlow()
    
    private val _dpiValue = MutableStateFlow("72") // Default DPI
    val dpiValue: StateFlow<String> = _dpiValue.asStateFlow()

    private val _availableUnits = MutableStateFlow<List<ConversionUnit>>(emptyList())
    val availableUnits: StateFlow<List<ConversionUnit>> = _availableUnits.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _categories.value = UnitCategory.values().filter { !internalCategories.contains(it) }
        } else {
            _categories.value = UnitCategory.values().filter { category ->
                if (internalCategories.contains(category)) return@filter false
                
                // Search in category title
                if (category.title.contains(query, ignoreCase = true)) return@filter true
                
                // Search in units of this category
                val units = ConversionRepository.getUnitsForCategory(category)
                units.any { unit -> 
                    unit.name.contains(query, ignoreCase = true) || 
                    unit.symbol.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun selectCategory(category: UnitCategory) {
        _selectedCategory.value = category
        val units = ConversionRepository.getUnitsForCategory(category)
        _availableUnits.value = units
        if (units.isNotEmpty()) {
            _sourceUnit.value = units[0]
            _targetUnit.value = if (units.size > 1) units[1] else units[0]
        }
        calculateConversion()
    }

    fun onInputValueChanged(value: String) {
        if (value.all { it.isDigit() || it == '.' }) {
            _inputValue.value = value
            calculateConversion()
        }
    }
    
    fun onDpiValueChanged(value: String) {
        if (value.all { it.isDigit() }) {
            _dpiValue.value = value
            calculateConversion()
        }
    }

    fun onSourceUnitChanged(unit: ConversionUnit) {
        _sourceUnit.value = unit
        calculateConversion()
    }

    fun onTargetUnitChanged(unit: ConversionUnit) {
        _targetUnit.value = unit
        calculateConversion()
    }

    fun swapUnits() {
        val temp = _sourceUnit.value
        _sourceUnit.value = _targetUnit.value
        _targetUnit.value = temp
        calculateConversion()
    }

    private fun calculateConversion() {
        checkIsFavorite()
        val input = _inputValue.value.toDoubleOrNull() ?: 0.0
        val source = _sourceUnit.value
        val target = _targetUnit.value
        val category = _selectedCategory.value
        
        if (source == null || target == null) return

        val result = if (category == UnitCategory.TEMPERATURE) {
            // Special case for Temperature
            convertTemperature(input, source, target)
        } else if (category == UnitCategory.DIGITAL_IMAGE) {
            // Special case for Digital Image (Pixel Converter)
            convertDigitalImage(input, source, target)
        } else {
            // Standard Linear Conversion
            // Convert to base unit then to target unit
            val baseValue = input * source.factor
            baseValue / target.factor
        }

        val df = DecimalFormat("#.####")
        _resultValue.value = df.format(result)
        
        // Save to history if we have valid values
        if (input > 0 && source != null && target != null) {
            viewModelScope.launch {
                repository.saveConversion(
                    category = category.name,
                    fromUnit = source.name,
                    toUnit = target.name,
                    inputValue = _inputValue.value,
                    resultValue = _resultValue.value
                )
            }
        }
    }
    
    private fun convertTemperature(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        val baseValue = (value + from.offset) * from.factor
        return (baseValue / to.factor) - to.offset
    }
    
    private fun convertDigitalImage(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        val dpi = _dpiValue.value.toDoubleOrNull() ?: 72.0
        
        // Convert everything to Pixels (Base)
        val pixels = when (from.name) {
            "Pixel" -> value
            "Inch" -> value * dpi
            "Centimeter" -> (value / 2.54) * dpi
            "Millimeter" -> (value / 25.4) * dpi
            else -> 0.0
        }
        
        // Convert Pixels to Target
        return when (to.name) {
            "Pixel" -> pixels
            "Inch" -> pixels / dpi
            "Centimeter" -> (pixels / dpi) * 2.54
            "Millimeter" -> (pixels / dpi) * 25.4
            else -> 0.0
        }
    }


    private val _isCurrentFavorite = MutableStateFlow(false)
    val isCurrentFavorite: StateFlow<Boolean> = _isCurrentFavorite.asStateFlow()

    fun checkIsFavorite() {
        val source = _sourceUnit.value ?: return
        val target = _targetUnit.value ?: return
        val category = _selectedCategory.value

        viewModelScope.launch {
            _isCurrentFavorite.value = repository.isFavorite(category, source.name, target.name)
        }
    }

    fun toggleFavorite() {
        val source = _sourceUnit.value ?: return
        val target = _targetUnit.value ?: return
        val category = _selectedCategory.value

        viewModelScope.launch {
            repository.toggleFavorite(category, source.name, target.name)
            checkIsFavorite()
        }
    }

    fun removeFavorite(favorite: ConversionFavoriteEntity) {
        viewModelScope.launch {
            repository.removeFavorite(favorite)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun recalculateFromHistory(
        categoryName: String,
        fromUnitName: String,
        toUnitName: String,
        newInputValue: String
    ): QuickEditResult? {
        try {
            val category = UnitCategory.entries.find { it.name == categoryName } ?: return null
            val units = ConversionRepository.getUnitsForCategory(category)
            val fromUnit = units.find { it.name == fromUnitName } ?: return null
            val toUnit = units.find { it.name == toUnitName } ?: return null
            
            val inputDouble = newInputValue.toDoubleOrNull() ?: return null
            
            val result = if (category == UnitCategory.TEMPERATURE) {
                convertTemperature(inputDouble, fromUnit, toUnit)
            } else if (category == UnitCategory.DIGITAL_IMAGE) {
                convertDigitalImage(inputDouble, fromUnit, toUnit)
            } else {
                // Standard Linear Conversion
                val baseValue = inputDouble * fromUnit.factor
                baseValue / toUnit.factor
            }
            
            val formatter = DecimalFormat("#,##0.##########")
            val formattedResult = formatter.format(result)
            
            return QuickEditResult(
                inputValue = newInputValue,
                resultValue = formattedResult,
                fromUnit = fromUnit.symbol,
                toUnit = toUnit.symbol,
                category = categoryName
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun saveQuickEditToHistory(result: QuickEditResult) {
        viewModelScope.launch {
            repository.saveConversion(
                category = result.category,
                fromUnit = result.fromUnit,
                toUnit = result.toUnit,
                inputValue = result.inputValue,
                resultValue = result.resultValue
            )
        }
    }
}

// Quick edit from history
data class QuickEditResult(
    val inputValue: String,
    val resultValue: String,
    val fromUnit: String,
    val toUnit: String,
    val category: String
)
