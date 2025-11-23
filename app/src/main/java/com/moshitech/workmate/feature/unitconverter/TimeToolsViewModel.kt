package com.moshitech.workmate.feature.unitconverter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TimeToolsViewModel : ViewModel() {

    // --- Date Calculator State ---
    private val _calcStartDate = MutableStateFlow(LocalDate.now())
    val calcStartDate: StateFlow<LocalDate> = _calcStartDate.asStateFlow()

    private val _calcYears = MutableStateFlow("0")
    val calcYears: StateFlow<String> = _calcYears.asStateFlow()

    private val _calcMonths = MutableStateFlow("0")
    val calcMonths: StateFlow<String> = _calcMonths.asStateFlow()

    private val _calcDays = MutableStateFlow("0")
    val calcDays: StateFlow<String> = _calcDays.asStateFlow()

    private val _calcOperation = MutableStateFlow(DateOperation.ADD)
    val calcOperation: StateFlow<DateOperation> = _calcOperation.asStateFlow()

    private val _calcResultDate = MutableStateFlow(LocalDate.now())
    val calcResultDate: StateFlow<LocalDate> = _calcResultDate.asStateFlow()

    // --- Time Difference State ---
    private val _diffStartDate = MutableStateFlow(LocalDate.now())
    val diffStartDate: StateFlow<LocalDate> = _diffStartDate.asStateFlow()

    private val _diffEndDate = MutableStateFlow(LocalDate.now())
    val diffEndDate: StateFlow<LocalDate> = _diffEndDate.asStateFlow()

    private val _diffResult = MutableStateFlow("")
    val diffResult: StateFlow<String> = _diffResult.asStateFlow()

    // --- Timestamp Converter State ---
    private val _timestampInput = MutableStateFlow(System.currentTimeMillis().toString())
    val timestampInput: StateFlow<String> = _timestampInput.asStateFlow()

    private val _timestampResult = MutableStateFlow("")
    val timestampResult: StateFlow<String> = _timestampResult.asStateFlow()

    // --- Date Calculator Logic ---
    fun onCalcStartDateChanged(date: LocalDate) {
        _calcStartDate.value = date
        calculateDate()
    }

    fun onCalcDurationChanged(years: String, months: String, days: String) {
        _calcYears.value = years
        _calcMonths.value = months
        _calcDays.value = days
        calculateDate()
    }

    fun onCalcOperationChanged(operation: DateOperation) {
        _calcOperation.value = operation
        calculateDate()
    }

    private fun calculateDate() {
        val years = _calcYears.value.toLongOrNull() ?: 0
        val months = _calcMonths.value.toLongOrNull() ?: 0
        val days = _calcDays.value.toLongOrNull() ?: 0

        val date = _calcStartDate.value
        _calcResultDate.value = if (_calcOperation.value == DateOperation.ADD) {
            date.plusYears(years).plusMonths(months).plusDays(days)
        } else {
            date.minusYears(years).minusMonths(months).minusDays(days)
        }
    }

    // --- Time Difference Logic ---
    fun onDiffStartDateChanged(date: LocalDate) {
        _diffStartDate.value = date
        calculateDifference()
    }

    fun onDiffEndDateChanged(date: LocalDate) {
        _diffEndDate.value = date
        calculateDifference()
    }

    private fun calculateDifference() {
        val start = _diffStartDate.value
        val end = _diffEndDate.value
        
        val p = java.time.Period.between(start, end)
        val daysBetween = ChronoUnit.DAYS.between(start, end)
        
        _diffResult.value = "${p.years} Years, ${p.months} Months, ${p.days} Days\n(Total: $daysBetween Days)"
    }

    // --- Timestamp Logic ---
    fun onTimestampInputChanged(input: String) {
        _timestampInput.value = input
        convertTimestamp()
    }

    private fun convertTimestamp() {
        val input = _timestampInput.value.toLongOrNull() ?: return
        try {
            // Assume millis if > 10 digits (approx year 2286 in seconds), else seconds
            // Simple heuristic: 10 digits is seconds (up to year 2286), 13 digits is millis.
            // Current millis is ~1.7e12 (13 digits). Current seconds ~1.7e9 (10 digits).
            val instant = if (input > 10000000000L) { // Likely millis
                Instant.ofEpochMilli(input)
            } else {
                Instant.ofEpochSecond(input)
            }
            
            val zoneId = ZoneId.systemDefault()
            val dateTime = LocalDateTime.ofInstant(instant, zoneId)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            _timestampResult.value = dateTime.format(formatter)
        } catch (e: Exception) {
            _timestampResult.value = "Invalid Timestamp"
        }
    }
    
    init {
        calculateDate()
        calculateDifference()
        convertTimestamp()
    }
}

enum class DateOperation {
    ADD, SUBTRACT
}
