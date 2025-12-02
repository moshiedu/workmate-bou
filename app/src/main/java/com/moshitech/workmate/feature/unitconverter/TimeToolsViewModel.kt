package com.moshitech.workmate.feature.unitconverter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.feature.unitconverter.repository.UnitConverterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TimeToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnitConverterRepository(application)

    // --- History & Favorites ---
    val history = repository.getHistory()
    val favorites = repository.getFavorites()

    fun saveHistory(category: UnitCategory, from: String, to: String, input: String, result: String) {
        viewModelScope.launch {
            repository.saveConversion(category.name, from, to, input, result)
        }
    }

    fun toggleFavorite(category: UnitCategory, from: String, to: String) {
        viewModelScope.launch {
            repository.toggleFavorite(category, from, to)
        }
    }

    fun isFavorite(category: UnitCategory, from: String, to: String): kotlinx.coroutines.flow.Flow<Boolean> {
        return favorites.map { list ->
            list.any { it.category == category && it.fromUnit == from && it.toUnit == to }
        }
    }


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
    
    private val _isTimestampToDate = MutableStateFlow(true)
    val isTimestampToDate: StateFlow<Boolean> = _isTimestampToDate.asStateFlow()
    
    private val _timestampFormat = MutableStateFlow("yyyy-MM-dd HH:mm:ss")
    val timestampFormat: StateFlow<String> = _timestampFormat.asStateFlow()
    
    private val _timestampZoneId = MutableStateFlow(ZoneId.systemDefault().id)
    val timestampZoneId: StateFlow<String> = _timestampZoneId.asStateFlow()

    // --- Timestamp Logic ---
    fun onTimestampInputChanged(input: String) {
        _timestampInput.value = input
        convertTimestamp()
    }
    
    fun onTimestampDirectionChanged(isToDate: Boolean) {
        _isTimestampToDate.value = isToDate
        convertTimestamp()
    }
    
    fun onTimestampFormatChanged(format: String) {
        _timestampFormat.value = format
        convertTimestamp()
    }
    
    fun onTimestampZoneIdChanged(id: String) {
        _timestampZoneId.value = id
        convertTimestamp()
    }

    private fun convertTimestamp() {
        if (_isTimestampToDate.value) {
            // Timestamp -> Date
            val input = _timestampInput.value.toLongOrNull() ?: return
            try {
                // Heuristic for Timestamp Precision
                val instant = when (_timestampInput.value.length) {
                    in 0..11 -> Instant.ofEpochSecond(input)
                    in 12..14 -> Instant.ofEpochMilli(input)
                    in 15..17 -> Instant.ofEpochSecond(input / 1_000_000, (input % 1_000_000) * 1000)
                    else -> Instant.ofEpochSecond(input / 1_000_000_000, input % 1_000_000_000)
                }
                
                val zoneId = try { ZoneId.of(_timestampZoneId.value) } catch (e: Exception) { ZoneId.systemDefault() }
                val dateTime = LocalDateTime.ofInstant(instant, zoneId)
                val formatter = DateTimeFormatter.ofPattern(_timestampFormat.value)
                _timestampResult.value = dateTime.format(formatter)
            } catch (e: Exception) {
                _timestampResult.value = "Invalid Timestamp"
            }
        } else {
            // Date -> Timestamp
            // Input expected in selected format
            try {
                val formatter = DateTimeFormatter.ofPattern(_timestampFormat.value)
                val dateTime = LocalDateTime.parse(_timestampInput.value, formatter)
                val zoneId = try { ZoneId.of(_timestampZoneId.value) } catch (e: Exception) { ZoneId.systemDefault() }
                val instant = dateTime.atZone(zoneId).toInstant()
                _timestampResult.value = instant.toEpochMilli().toString()
            } catch (e: Exception) {
                _timestampResult.value = "Invalid Date Format"
            }
        }
    }


    
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
        val result = if (_calcOperation.value == DateOperation.ADD) {
            date.plusYears(years).plusMonths(months).plusDays(days)
        } else {
            date.minusYears(years).minusMonths(months).minusDays(days)
        }
        _calcResultDate.value = result
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
    
    // --- Time Zone State ---
    private val _timeZoneSourceTime = MutableStateFlow(LocalDateTime.now())
    val timeZoneSourceTime: StateFlow<LocalDateTime> = _timeZoneSourceTime.asStateFlow()

    private val _timeZoneSourceId = MutableStateFlow(ZoneId.systemDefault().id)
    val timeZoneSourceId: StateFlow<String> = _timeZoneSourceId.asStateFlow()

    private val _timeZoneTargetId = MutableStateFlow("UTC")
    val timeZoneTargetId: StateFlow<String> = _timeZoneTargetId.asStateFlow()

    private val _timeZoneResult = MutableStateFlow("")
    val timeZoneResult: StateFlow<String> = _timeZoneResult.asStateFlow()

    // --- Business Day State ---
    private val _bizStartDate = MutableStateFlow(LocalDate.now())
    val bizStartDate: StateFlow<LocalDate> = _bizStartDate.asStateFlow()

    private val _bizDays = MutableStateFlow("0")
    val bizDays: StateFlow<String> = _bizDays.asStateFlow()

    private val _bizOperation = MutableStateFlow(DateOperation.ADD)
    val bizOperation: StateFlow<DateOperation> = _bizOperation.asStateFlow()

    private val _bizResultDate = MutableStateFlow(LocalDate.now())
    val bizResultDate: StateFlow<LocalDate> = _bizResultDate.asStateFlow()

    // --- Age Calculator State ---
    private val _ageBirthDate = MutableStateFlow(LocalDate.now().minusYears(20))
    val ageBirthDate: StateFlow<LocalDate> = _ageBirthDate.asStateFlow()

    private val _ageResult = MutableStateFlow("")
    val ageResult: StateFlow<String> = _ageResult.asStateFlow()

    // --- Time Zone Logic ---
    fun onTimeZoneSourceTimeChanged(time: LocalDateTime) {
        _timeZoneSourceTime.value = time
        calculateTimeZone()
    }

    fun onTimeZoneSourceIdChanged(id: String) {
        _timeZoneSourceId.value = id
        calculateTimeZone()
    }

    fun onTimeZoneTargetIdChanged(id: String) {
        _timeZoneTargetId.value = id
        calculateTimeZone()
    }

    private fun calculateTimeZone() {
        try {
            val sourceTime = _timeZoneSourceTime.value
            val sourceId = ZoneId.of(_timeZoneSourceId.value)
            val targetId = ZoneId.of(_timeZoneTargetId.value)
            
            // sourceTime is already LocalDateTime, we treat it as being in sourceId zone
            val sourceZoned = sourceTime.atZone(sourceId)
            val targetZoned = sourceZoned.withZoneSameInstant(targetId)
            
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            _timeZoneResult.value = targetZoned.format(formatter)
        } catch (e: Exception) {
            _timeZoneResult.value = "Invalid Zone"
        }
    }



    // --- Business Day Logic ---
    fun onBizStartDateChanged(date: LocalDate) {
        _bizStartDate.value = date
        calculateBizDate()
    }

    fun onBizDaysChanged(days: String) {
        _bizDays.value = days
        calculateBizDate()
    }

    fun onBizOperationChanged(op: DateOperation) {
        _bizOperation.value = op
        calculateBizDate()
    }

    private fun calculateBizDate() {
        val daysToAdd = _bizDays.value.toLongOrNull() ?: 0L
        var date = _bizStartDate.value
        var added = 0L
        val direction = if (_bizOperation.value == DateOperation.ADD) 1 else -1
        
        while (added < daysToAdd) {
            date = date.plusDays(direction.toLong())
            val dayOfWeek = date.dayOfWeek
            if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                added++
            }
        }
        _bizResultDate.value = date
    }

    fun saveBizDaysHistory() {
        val days = _bizDays.value
        val op = if (_bizOperation.value == DateOperation.ADD) "+" else "-"
        val input = "${_bizStartDate.value} $op ${days} business days"
        val result = _bizResultDate.value.toString()
        saveHistory(UnitCategory.TIME_BIZ_DAYS, "Date", "Date", input, result)
    }

    // --- Age Calculator Logic ---
    fun onAgeBirthDateChanged(date: LocalDate) {
        _ageBirthDate.value = date
        calculateAge()
    }

    private fun calculateAge() {
        val birth = _ageBirthDate.value
        val now = LocalDate.now()
        
        if (birth.isAfter(now)) {
            _ageResult.value = "Not born yet!"
            return
        }

        val p = java.time.Period.between(birth, now)
        val totalDays = ChronoUnit.DAYS.between(birth, now)
        val nextBirthday = birth.plusYears((p.years + 1).toLong())
        val daysToBirthday = ChronoUnit.DAYS.between(now, nextBirthday)

        _ageResult.value = """
            ${p.years} Years, ${p.months} Months, ${p.days} Days
            
            Total Days: $totalDays
            Next Birthday in: $daysToBirthday Days
        """.trimIndent()
    }

    fun saveAgeHistory() {
        val input = _ageBirthDate.value.toString()
        val result = _ageResult.value
        saveHistory(UnitCategory.TIME_AGE, "Birth Date", "Age", input, result)
    }

    fun saveDateCalcHistory() {
        val years = _calcYears.value
        val months = _calcMonths.value
        val days = _calcDays.value
        val op = if (_calcOperation.value == DateOperation.ADD) "+" else "-"
        val input = "${_calcStartDate.value} $op $years Y, $months M, $days D"
        val result = _calcResultDate.value.toString()
        saveHistory(UnitCategory.TIME_DATE_CALC, "Date", "Date", input, result)
    }

    fun saveDiffHistory() {
        val input = "${_diffStartDate.value} to ${_diffEndDate.value}"
        val result = _diffResult.value
        saveHistory(UnitCategory.TIME_DIFFERENCE, "Start", "End", input, result)
    }

    fun saveTimestampHistory() {
        val input = _timestampInput.value
        val result = _timestampResult.value
        val from = if (_isTimestampToDate.value) "Timestamp" else "Date"
        val to = if (_isTimestampToDate.value) "Date" else "Timestamp"
        saveHistory(UnitCategory.TIME_TIMESTAMP, from, to, input, result)
    }

    fun saveTimeZoneHistory() {
        val input = "${_timeZoneSourceTime.value} (${_timeZoneSourceId.value})"
        val result = "${_timeZoneResult.value} (${_timeZoneTargetId.value})"
        saveHistory(UnitCategory.TIME_ZONES, _timeZoneSourceId.value, _timeZoneTargetId.value, input, result)
    }

    // --- Favorite Helpers for Tabs ---
    fun isFavoriteForTab(index: Int): Flow<Boolean> {
        return when (index) {
            1 -> isFavorite(UnitCategory.TIME_DATE_CALC, "Date", "Date")
            2 -> isFavorite(UnitCategory.TIME_DIFFERENCE, "Start", "End")
            3 -> {
                val from = if (_isTimestampToDate.value) "Timestamp" else "Date"
                val to = if (_isTimestampToDate.value) "Date" else "Timestamp"
                isFavorite(UnitCategory.TIME_TIMESTAMP, from, to)
            }
            4 -> isFavorite(UnitCategory.TIME_ZONES, _timeZoneSourceId.value, _timeZoneTargetId.value)
            5 -> isFavorite(UnitCategory.TIME_BIZ_DAYS, "Date", "Date")
            6 -> isFavorite(UnitCategory.TIME_AGE, "Birth Date", "Age")
            else -> kotlinx.coroutines.flow.flowOf(false)
        }
    }

    fun toggleFavoriteForTab(index: Int) {
        when (index) {
            1 -> toggleFavorite(UnitCategory.TIME_DATE_CALC, "Date", "Date")
            2 -> toggleFavorite(UnitCategory.TIME_DIFFERENCE, "Start", "End")
            3 -> {
                val from = if (_isTimestampToDate.value) "Timestamp" else "Date"
                val to = if (_isTimestampToDate.value) "Date" else "Timestamp"
                toggleFavorite(UnitCategory.TIME_TIMESTAMP, from, to)
            }
            4 -> toggleFavorite(UnitCategory.TIME_ZONES, _timeZoneSourceId.value, _timeZoneTargetId.value)
            5 -> toggleFavorite(UnitCategory.TIME_BIZ_DAYS, "Date", "Date")
            6 -> toggleFavorite(UnitCategory.TIME_AGE, "Birth Date", "Age")
        }
    }

    init {
        calculateDate()
        calculateDifference()
        convertTimestamp()
        calculateTimeZone()
        calculateBizDate()
        calculateAge()
    }
}

enum class DateOperation {
    ADD, SUBTRACT
}
