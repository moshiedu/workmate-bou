package com.moshitech.workmate.feature.unitconverter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

class UnitConverterViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categories = MutableStateFlow(UnitCategory.values().toList())
    val categories: StateFlow<List<UnitCategory>> = _categories.asStateFlow()

    // Dummy favorites for now
    private val _favorites = MutableStateFlow(
        listOf(
            ConversionFavorite("1", UnitCategory.LENGTH, "Inch", "Centimeter"),
            ConversionFavorite("2", UnitCategory.TEMPERATURE, "Celsius", "Fahrenheit")
        )
    )
    val favorites: StateFlow<List<ConversionFavorite>> = _favorites.asStateFlow()

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
            _categories.value = UnitCategory.values().toList()
        } else {
            _categories.value = UnitCategory.values().filter {
                it.title.contains(query, ignoreCase = true)
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
}
