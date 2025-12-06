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
import kotlin.math.pow

class UnitConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UnitConverterRepository by lazy { UnitConverterRepository(application) }
    private val currencyRateRepository: com.moshitech.workmate.feature.unitconverter.repository.CurrencyRateRepository by lazy {
        com.moshitech.workmate.feature.unitconverter.repository.CurrencyRateRepository(application)
    }
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
            val newMode = when (viewMode.value) {
                com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GRID -> 
                    com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.LIST
                com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.LIST -> 
                    com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GROUPED
                com.moshitech.workmate.repository.UserPreferencesRepository.ViewMode.GROUPED -> 
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

    private val internalCategories = setOf<UnitCategory>(
        // Empty - all time categories now visible under Time & Date group
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
    
    private val _baseFontSize = MutableStateFlow("16") // Default base font size (browser standard)
    val baseFontSize: StateFlow<String> = _baseFontSize.asStateFlow()
    
    private val _exchangeRate = MutableStateFlow("1.0") // Default exchange rate
    val exchangeRate: StateFlow<String> = _exchangeRate.asStateFlow()
    
    val currencyRates: StateFlow<Map<String, Double>> = currencyRateRepository.currencyRates

    private val _availableUnits = MutableStateFlow<List<ConversionUnit>>(emptyList())
    val availableUnits: StateFlow<List<ConversionUnit>> = _availableUnits.asStateFlow()

    // Grouping and Help State
    private val _showGroupedView = MutableStateFlow(false)
    val showGroupedView: StateFlow<Boolean> = _showGroupedView.asStateFlow()
    
    private val _expandedGroups = MutableStateFlow<Set<CategoryGroup>>(emptySet())
    val expandedGroups: StateFlow<Set<CategoryGroup>> = _expandedGroups.asStateFlow()
    
    private val _selectedHelpCategory = MutableStateFlow<UnitCategory?>(null)
    val selectedHelpCategory: StateFlow<UnitCategory?> = _selectedHelpCategory.asStateFlow()
    
    val groupedCategories: StateFlow<Map<CategoryGroup, List<UnitCategory>>> = 
        MutableStateFlow(CategoryGroup.getAllGroupedCategories()).asStateFlow()
    
    fun toggleGroupedView() {
        _showGroupedView.value = !_showGroupedView.value
    }
    
    fun toggleGroupExpansion(group: CategoryGroup) {
        val current = _expandedGroups.value.toMutableSet()
        if (group in current) {
            current.remove(group)
        } else {
            current.add(group)
        }
        _expandedGroups.value = current
    }
    
    fun showHelpDialog(category: UnitCategory) {
        _selectedHelpCategory.value = category
    }
    
    fun dismissHelpDialog() {
        _selectedHelpCategory.value = null
    }

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
    
    fun onBaseFontSizeChanged(value: String) {
        if (value.all { it.isDigit() || it == '.' }) {
            _baseFontSize.value = value
            calculateConversion()
        }
    }
    
    fun onExchangeRateChanged(value: String) {
        if (value.all { it.isDigit() || it == '.' }) {
            _exchangeRate.value = value
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
    
    fun updateCurrencyRate(currencyCode: String, rate: Double) {
        currencyRateRepository.updateRate(currencyCode, rate)
        if (_selectedCategory.value == UnitCategory.CURRENCY) {
            calculateConversion()
        }
    }
    
    fun resetCurrencyRatesToDefaults() {
        currencyRateRepository.resetToDefaults()
        if (_selectedCategory.value == UnitCategory.CURRENCY) {
            calculateConversion()
        }
    }
    
    fun addCustomCurrency(currencyCode: String, rate: Double) {
        currencyRateRepository.addCurrency(currencyCode, rate)
        if (_selectedCategory.value == UnitCategory.CURRENCY) {
            calculateConversion()
        }
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
        } else if (category == UnitCategory.POWER) {
            // Special case for Power (dBm is logarithmic)
            convertPower(input, source, target)
        } else if (category == UnitCategory.TYPOGRAPHY) {
            // Special case for Typography (em, rem, % are context-dependent)
            convertTypography(input, source, target)
        } else if (category == UnitCategory.MATH_HELPER) {
            // Special case for Math Helper (Percent/Decimal/Fraction)
            convertMathHelper(input, source, target)
        } else if (category == UnitCategory.CURRENCY) {
            // Special case for Currency (user-defined exchange rate)
            convertCurrency(input, source, target)
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
    
    private fun convertPower(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        // Handle dBm (logarithmic) separately
        val isDmFrom = from.symbol == "dBm"
        val isDbmTo = to.symbol == "dBm"
        
        return when {
            isDmFrom && isDbmTo -> value // dBm to dBm
            isDmFrom -> {
                // dBm to linear: Convert dBm -> mW -> W -> target
                val milliwatts = 10.0.pow(value / 10.0)
                val watts = milliwatts / 1000.0
                watts / to.factor
            }
            isDbmTo -> {
                // Linear to dBm: Convert source -> W -> mW -> dBm
                val watts = value * from.factor
                val milliwatts = watts * 1000.0
                10.0 * kotlin.math.log10(milliwatts)
            }
            else -> {
                // Standard linear conversion (W, kW, HP, etc.)
                val baseValue = value * from.factor
                baseValue / to.factor
            }
        }
    }
    
    private fun convertTypography(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        val baseFont = _baseFontSize.value.toDoubleOrNull() ?: 16.0
        
        // Convert everything to pixels first
        val pixels = when (from.symbol) {
            "px" -> value
            "pt" -> value * 1.333 // 1pt = 1.333px at 96 DPI
            "em", "rem" -> value * baseFont
            "%" -> (value / 100.0) * baseFont
            else -> value
        }
        
        // Convert pixels to target unit
        return when (to.symbol) {
            "px" -> pixels
            "pt" -> pixels / 1.333
            "em", "rem" -> pixels / baseFont
            "%" -> (pixels / baseFont) * 100.0
            else -> pixels
        }
    }
    
    private fun convertMathHelper(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        // Convert to percent first (base unit)
        val percent = when (from.symbol) {
            "%" -> value
            "dec" -> value * 100.0 // 0.5 decimal = 50%
            "frac" -> value * 100.0 // For now, treat fraction input as decimal
            else -> value
        }
        
        // Convert from percent to target
        return when (to.symbol) {
            "%" -> percent
            "dec" -> percent / 100.0 // 50% = 0.5 decimal
            "frac" -> percent / 100.0 // Display as decimal for now
            else -> percent
        }
    }
    
    private fun convertCurrency(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        // Use stored currency rates from repository
        return currencyRateRepository.convert(value, from.symbol, to.symbol)
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
        val category = _selectedCategory.value
        
        // For calculator categories (BMI, SCREEN_PPI), use category name as both source and target
        val isCalculatorCategory = category == UnitCategory.BMI || category == UnitCategory.SCREEN_PPI
        
        if (isCalculatorCategory) {
            viewModelScope.launch {
                _isCurrentFavorite.value = repository.isFavorite(category, category.name, category.name)
            }
            return
        }
        
        val source = _sourceUnit.value ?: return
        val target = _targetUnit.value ?: return

        viewModelScope.launch {
            _isCurrentFavorite.value = repository.isFavorite(category, source.name, target.name)
        }
    }

    fun toggleFavorite() {
        val category = _selectedCategory.value
        
        // For calculator categories (BMI, SCREEN_PPI), use category name as both source and target
        val isCalculatorCategory = category == UnitCategory.BMI || category == UnitCategory.SCREEN_PPI
        
        if (isCalculatorCategory) {
            viewModelScope.launch {
                repository.toggleFavorite(category, category.name, category.name)
                checkIsFavorite()
            }
            return
        }
        
        val source = _sourceUnit.value ?: return
        val target = _targetUnit.value ?: return

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
